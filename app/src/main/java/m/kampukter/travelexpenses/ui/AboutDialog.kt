package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.R

class AboutDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val titleView = TextView(activity)
        titleView.gravity = Gravity.CENTER
        titleView.text = getString(R.string.about_title)
        titleView.textSize = 24F
        val textView = TextView(activity)
        textView.gravity = Gravity.CENTER
        textView.text = getString(R.string.about_message)
        builder.setView(textView)
            .setCustomTitle(titleView)
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton(android.R.string.ok ) { _, _ ->
            }

        return builder.create()
    }

    companion object {
        const val TAG = "AboutDialog"
        fun create(): AboutDialog = AboutDialog()
    }
}