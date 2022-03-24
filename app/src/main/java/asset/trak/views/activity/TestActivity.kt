package asset.trak.views.activity

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import asset.trak.scannercode.DWInterface
import asset.trak.scannercode.DWReceiver
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_INTENT_ACTION
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_INTENT_START_ACTIVITY
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_NAME
import asset.trak.views.module.InventoryViewModel
import com.bumptech.glide.Glide
import com.darryncampbell.datawedgekotlin.ObservableObject
import com.markss.rfidtemplate.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.android.synthetic.main.fragment_view_inventory.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class TestActivity : AppCompatActivity(), Observer, View.OnTouchListener {


    private val dwInterface = DWInterface();
    private val receiver = DWReceiver()
    private var initialized = false;
    private var version65OrOver = false

    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        ObservableObject.instance.addObserver(this)
        Glide.with(this).asGif().load("file:///android_asset/bar_code1.gif").into(imageView)

        val intentFilter = IntentFilter()
        intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
        intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)
        registerReceiver(receiver, intentFilter)

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            barCodeValue.requestFocus()
        }

        imageView.setOnClickListener {
//            val intent = Intent()
//            intent.putExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING, "1000024")
//            onNewIntent(intent)
        }

        if (intent.getStringExtra("type") == "2") {
            tvTitle.text = "Map RFID Location"
            tvInventoryReport.visibility = View.INVISIBLE
            tvILastRecord.visibility = View.INVISIBLE
            registered.visibility = View.INVISIBLE
        }

        barCodeValue.doOnTextChanged { text, start, before, count ->
            if (text.toString().isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("barCode", text.toString().trim())
                resultIntent.putExtra("type", getIntent().getStringExtra("type"))
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun update(p0: Observable?, p1: Any?) {
        val receivedIntent = p1 as Intent
        if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_RETURN_VERSION)) {
            val version = receivedIntent.getBundleExtra(DWInterface.DATAWEDGE_RETURN_VERSION);
            val dataWedgeVersion =
                version?.getString(DWInterface.DATAWEDGE_RETURN_VERSION_DATAWEDGE);
            if (dataWedgeVersion != null && dataWedgeVersion >= "6.5" && !version65OrOver) {
                version65OrOver = true
                createDataWedgeProfile()
            }
        }
    }


    private fun createDataWedgeProfile() {
        dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_CREATE_PROFILE, PROFILE_NAME)
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", PROFILE_NAME)
        profileConfig.putString("PROFILE_ENABLED", "true") //  These are all strings
        profileConfig.putString("CONFIG_MODE", "UPDATE")
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString(
            "RESET_CONFIG",
            "true"
        ) //  This is the default but never hurts to specify
        val barcodeProps = Bundle()
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
        val appConfig = Bundle()
        appConfig.putString(
            "PACKAGE_NAME",
            packageName
        )      //  Associate the profile with this app
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
        dwInterface.sendCommandBundle(this, DWInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)
        //  You can only configure one plugin at a time in some versions of DW, now do the intent output
        profileConfig.remove("PLUGIN_CONFIG")
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", PROFILE_INTENT_ACTION)
        intentProps.putString("intent_delivery", PROFILE_INTENT_START_ACTIVITY)  //  "0"
        intentConfig.putBundle("PARAM_LIST", intentProps)
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
        dwInterface.sendCommandBundle(this, DWInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)
    }

    override fun onStart() {
        super.onStart()
        dwInterface.sendCommandString(
            applicationContext, DWInterface.DATAWEDGE_SEND_SET_SOFT_SCAN,
            "START_SCANNING"
        )
    }

    override fun onStop() {
        super.onStop()
        dwInterface.sendCommandString(
            applicationContext, DWInterface.DATAWEDGE_SEND_SET_SOFT_SCAN,
            "STOP_SCANNING"
        )
    }

    override fun onResume() {
        super.onResume()
        if (!initialized) {
            //  Create profile to be associated with this application
            dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_GET_VERSION, "")
            initialized = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)) {
            var scanData = intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
            barCodeValue.setText(scanData)
        }
    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        when (buttonScan?.id) {
            R.id.buttonScan -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dwInterface.sendCommandString(
                            applicationContext, DWInterface.DATAWEDGE_SEND_SET_SOFT_SCAN,
                            "START_SCANNING"
                        )
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        dwInterface.sendCommandString(
                            applicationContext, DWInterface.DATAWEDGE_SEND_SET_SOFT_SCAN,
                            "STOP_SCANNING"
                        )
                        return true
                    }
                }
            }
        }
        return false
    }
}