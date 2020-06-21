package com.abhi.voicesearch.core

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.abhi.voicesearch.BuildConfig.VERSION_NAME
import com.abhi.voicesearch.R

// inspired from mnml
class AboutDialog : DialogFragment() {

    companion object {
        private const val TAG = "[ABOUT_DIALOG]"

        /** Shows the about dialog inside of [activity]. */
        fun show(activity: FragmentActivity) {
            val dialog = AboutDialog()
            dialog.show(
                activity.supportFragmentManager,
                TAG
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: throw IllegalStateException("Oh no!")
        return MaterialDialog(context)
            .title(text = getString(R.string.about_title, VERSION_NAME))
            .message(res = R.string.about_body) {
                html()
                lineSpacing(1.4f)
            }
            .positiveButton(R.string.contact) {
                val email = "ygsankar.87@gmail.com"
                val emailIntent = Intent("android.intent.action.SENDTO", Uri.parse("mailto:$email"))
                emailIntent.putExtra("android.intent.extra.SUBJECT", "Voice Search help")
                context.startActivity(Intent.createChooser(emailIntent, "Contact"))
            }
            .negativeButton(R.string.dismiss)
    }
}
