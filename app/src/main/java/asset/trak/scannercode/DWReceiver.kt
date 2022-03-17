package asset.trak.scannercode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.darryncampbell.datawedgekotlin.ObservableObject

class DWReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //  This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        //  Notify registered observers
        ObservableObject.instance.updateValue(intent)
    }
}
