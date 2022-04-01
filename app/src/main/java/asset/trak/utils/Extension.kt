package asset.trak.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import asset.trak.modelsrrtrack.AssetData
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.utils.compressimage.Compressor
import asset.trak.utils.compressimage.constraint.format
import asset.trak.utils.compressimage.constraint.quality
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.rfid.RFIDController.mConnectedReader
import com.zebra.rfid.api3.Antennas
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.OperationFailureException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun IntRange.random() =
    Random().nextInt((endInclusive + 1) - start) + start

suspend fun compressImage(activity: Activity?, actualImageFile: File?): File {
    try {
        val valueOf1mb = 1048576.0
        Log.e("File Size", (actualImageFile?.length()?.div(valueOf1mb)).toString())

        if (actualImageFile?.length() ?: 0 < (1 * valueOf1mb))
            return actualImageFile!!

        val qualityNeeded = when {
            actualImageFile?.length() ?: 0 < (1 * valueOf1mb) -> 100
            actualImageFile?.length() ?: 0 < (2 * valueOf1mb) -> 70
            actualImageFile?.length() ?: 0 < (3 * valueOf1mb) -> 60
            actualImageFile?.length() ?: 0 < (4 * valueOf1mb) -> 50
            actualImageFile?.length() ?: 0 < (5 * valueOf1mb) -> 45
            actualImageFile?.length() ?: 0 < (6 * valueOf1mb) -> 40
            actualImageFile?.length() ?: 0 < (10 * valueOf1mb) -> 15
            actualImageFile?.length() ?: 0 > (10 * valueOf1mb) -> 10
            else -> 100
        }

        //mb approximate
        val resultFile: File = Compressor.compress(activity as Context, actualImageFile!!) {
            quality(qualityNeeded)
            format(Bitmap.CompressFormat.JPEG)
        }

        Log.e("File Size New", (resultFile.length().div(valueOf1mb)).toString())

        return resultFile ?: File("")
    } catch (e: Exception) {
        return actualImageFile!!
    }
}


fun getFormattedDate(
    originalFormat: SimpleDateFormat,
    targetFormat: SimpleDateFormat,
    inputDate: String
): String {
    return try {
        targetFormat.format(originalFormat.parse(inputDate))
    } catch (e: Exception) {
        ""
    }
}

fun isAvailableData(list: List<AssetMain>, rfid: String, assertId: String): Boolean {
    val updateList = list.filter { it.AssetRFID == rfid && it.AssetID == assertId }
    return updateList.isEmpty()
}

//fun disconnectRFIDReader() {
//    if (RFIDController.mConnectedReader.isConnected) {
//        RFIDController.is_disconnection_requested = true
//        try {
//            Toast.makeText(Application.context, "RFID Reader DisConnected ", Toast.LENGTH_SHORT)
//                .show()
//            Log.d("tag1231", "RFID Reader DisConnected ")
//            RFIDController.mConnectedReader.disconnect()
//        } catch (e: InvalidUsageException) {
//            e.printStackTrace()
//        } catch (e: OperationFailureException) {
//            e.printStackTrace()
//        }
//    }
//
//}

fun decreaseRangeToThirty(value: Int) {
    if (mConnectedReader != null && mConnectedReader.isConnected()) {
        if (!(Application.mIsInventoryRunning || Application.isLocatingTag) && mConnectedReader.Config.Antennas != null) {
            try {
                val antennaRfConfig: Antennas.AntennaRfConfig
                antennaRfConfig = mConnectedReader.Config.Antennas.getAntennaRfConfig(1)
                antennaRfConfig.setTransmitPowerIndex(value)
                antennaRfConfig.setTari(0)
                //                    antennaRfConfig.setrfModeTableIndex(LinkProfileUtil.getInstance().getSimpleProfileModeIndex(item.LinkProfileIndex));
                mConnectedReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig)
                Application.antennaRfConfig = antennaRfConfig
                  Toast.makeText(
                      Application.context,
                      "RFID Reader Connected Range set to ${value}",
                      Toast.LENGTH_SHORT
                  ).show()
            } catch (e: InvalidUsageException) {
                /* Log.d(
                     "decreaseRangeToThirty",
                     " Antenna configuration failed invalid usage EX " + e.vendorMessage
                 )
                 Toast.makeText(
                     Application.context,
                     "decreaseRangeToThirty Error1: ${e.message}",
                     Toast.LENGTH_SHORT
                 ).show()*/
            } catch (e: OperationFailureException) {
                /*  Log.d(
                      "decreaseRangeToThirty",
                      " Antenna configuration failed operation failure EX " + e.vendorMessage
                  )
                  Toast.makeText(
                      Application.context,
                      "decreaseRangeToThirty Error2: ${e.message}",
                      Toast.LENGTH_SHORT
                  ).show()*/
            }
        } else Toast.makeText(
            Application.context,
            Application.context.getResources().getString(R.string.settings_error_progress),
            Toast.LENGTH_SHORT
        ).show()
    } else Toast.makeText(
        Application.context,
        Application.context.getString(R.string.reader_not_conneted),
        Toast.LENGTH_SHORT
    ).show()
}

//fun connectRFIDReader() {
//    try {
//        val sharedPreferences: SharedPreferences =
//            Application.context.getSharedPreferences(Constants.READER_PASSWORDS, 0)
//        sharedPreferences.getString(RFIDController.LAST_CONNECTED_READER, null)
//        Toast.makeText(
//            Application.context,
//            "RFID Reader Connected ${
//                sharedPreferences.getString(
//                    RFIDController.LAST_CONNECTED_READER,
//                    null
//                )
//            }",
//            Toast.LENGTH_SHORT
//        ).show()
//
//        if (!RFIDController.mConnectedReader.isConnected) {
//            RFIDController.is_disconnection_requested = false
//            try {
//                RFIDController.mConnectedReader.connect()
//                Toast.makeText(Application.context, "RFID Reader Connected ", Toast.LENGTH_SHORT)
//                    .show()
//                Log.d("tag1231", "RFID Reader Connected ")
//            } catch (e: InvalidUsageException) {
//                e.printStackTrace()
//            } catch (e: OperationFailureException) {
//                e.printStackTrace()
//            }
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}

fun getThumb(progress: Int, resources: Resources, thumbView: View): Drawable? {
    (thumbView.findViewById(R.id.tvProgress) as TextView).text = when (progress) {
        in 18..24 -> {
            "L"
        }
        in 25..29 -> {
            "M"
        }
        else -> {
            "H"
        }
    }
    (thumbView.findViewById(R.id.tvProgresstv) as TextView).text = (progress).toString() + ""
    thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val bitmap = Bitmap.createBitmap(
        thumbView.measuredWidth,
        thumbView.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    thumbView.layout(0, 0, thumbView.measuredWidth, thumbView.measuredHeight)
    thumbView.draw(canvas)
    return BitmapDrawable(resources, bitmap)
}

fun Context.getThumbView(): View {
    return LayoutInflater.from(this).inflate(R.layout.layout_seekbar_thumb, null, false)

}

fun showYesNoAlert(
    context: Context,
    message: String,
    listener: CommonAlertDialog.OnButtonClickListener
) {
    CommonAlertDialog(context, message, "Yes", "No", listener).show()
}

fun getRFIDDistinct(list: List<AssetData>): List<AssetData> {
    return list.distinctBy { it.assetRFID }
}


fun ioCoroutines(
    block: suspend CoroutineScope.() -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        block(this)
    }
}

fun mainCoroutines(
    block: suspend CoroutineScope.() -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        block(this)
    }
}