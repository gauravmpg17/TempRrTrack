package asset.trak.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import kotlinx.android.synthetic.main.custom_dialog_layout.*

class CommonAlertDialog(
    mContext: Context,
    private val message: String,
    private val labelPositive: String,
    private val labelNegative: String,
    private val onButtonClickListener:OnButtonClickListener,
): Dialog(mContext, R.style.TransparentDilaog)  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_dialog_layout)
        try {
            setCancelable(false)
            val tvMessage = findViewById<View>(R.id.tvMessage) as TextView
            val tvPositive = findViewById<View>(R.id.tvPositive) as TextView
            val tvNegative = findViewById<View>(R.id.tvNegative) as TextView
            tvPositive.text = labelPositive
            tvMessage.text = message
            tvPositive.setOnClickListener {
                onButtonClickListener.onPositiveButtonClicked()
                dismiss()
            }

            if (labelNegative != "" && labelPositive != "") {
                tvNegative.visibility = View.VISIBLE
                line.visibility = View.VISIBLE
                tvNegative.text = labelNegative
                tvNegative.setOnClickListener {
                    onButtonClickListener.onNegativeButtonClicked()
                    dismiss()
                }
            } else if (labelPositive.isNullOrEmpty()) {
                line.visibility = View.GONE
                tvPositive.visibility = View.GONE
                tvNegative.visibility = View.VISIBLE
                tvNegative.text = labelNegative
                tvNegative.setOnClickListener {
                    onButtonClickListener.onNegativeButtonClicked()
                    dismiss()
                }
            } else {

                line.visibility = View.GONE
                tvNegative.visibility = View.GONE

            }

        } catch (e: Exception) {

        }
    }
    interface OnButtonClickListener {
        fun onPositiveButtonClicked()
        fun onNegativeButtonClicked()
    }
}