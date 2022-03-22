package asset.trak.views.activity

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import asset.trak.scannercode.DWInterface
import asset.trak.scannercode.DWReceiver
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_INTENT_ACTION
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_INTENT_START_ACTIVITY
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_NAME
import com.darryncampbell.datawedgekotlin.ObservableObject
import com.darryncampbell.datawedgekotlin.Scan
import com.markss.rfidtemplate.R
import kotlinx.android.synthetic.main.activity_test.*
import java.text.SimpleDateFormat
import java.util.*

class TestActivity : AppCompatActivity(), Observer, View.OnTouchListener {


    private val dwInterface = DWInterface();
    private val receiver = DWReceiver()
    private var initialized = false;
    private var version65OrOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        ObservableObject.instance.addObserver(this)
        buttonScan.setOnTouchListener(this)

        val intentFilter = IntentFilter()
        intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
        intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)
        registerReceiver(receiver, intentFilter)
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

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        when (buttonScan?.id) {
            R.id.btnScan -> {
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
        //  DataWedge intents received here
        if (intent.hasExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)) {
            //  Handle scan intent received from DataWedge, add it to the list of scans
            var scanData = intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
            var symbology = intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
            var date = Calendar.getInstance().getTime()
            var df = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            var dateTimeString = df.format(date)

            editTextTextEmailAddress.setText(scanData.toString())
        }
    }
}