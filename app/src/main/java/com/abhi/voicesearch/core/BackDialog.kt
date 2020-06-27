package com.abhi.voicesearch.core

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.R
import com.afollestad.materialdialogs.MaterialDialog
import java.lang.IllegalStateException

class BackDialog: DialogFragment() {

    companion object{
        private const val TAG = "[BACK_DIALOG]"

        fun show(activity:FragmentActivity){
            val dialog = BackDialog()
            dialog.show(activity.supportFragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity?: throw IllegalStateException("Oh no!")
        var title = R.string.back_title
        if(Injector.get().showBackDialog().get() <= 3) {
            title = R.string.back_title1

            return MaterialDialog(context)
                    .title(title)
                    .message(R.string.back_message)
                    .positiveButton(R.string.back_positive) {
                        val appPackageName = context.packageName
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                        } catch (e: ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                        }
                        Injector.get().showBackDialog().set(-1)
                        dismiss()
                    }
                    .negativeButton(R.string.back_negative) {
                        dismiss()
                        var count = Injector.get().showBackDialog().get()
                        Injector.get().showBackDialog().set(count + 1)
                        activity?.finish()
                    }

        }else{
            return MaterialDialog(context)
                    .title(title)
                    .message(R.string.back_message)
                    .positiveButton(R.string.back_positive) {
                        val appPackageName = context.packageName
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                        } catch (e: ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                        }
                        Injector.get().showBackDialog().set(-1)
                        dismiss()
                    }
                    .negativeButton(R.string.back_negative) {
                        dismiss()
                        activity?.finish()
                    }
                    .neutralButton(R.string.back_neutral) {
                        dismiss()
                        var count = Injector.get().showBackDialog().get()
                        Injector.get().showBackDialog().set(count + 1)
                        activity?.finish()
                    }
        }
    }
}