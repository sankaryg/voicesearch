package com.abhi.voicesearch.settings

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.abhi.ui.extras.BaseDaggerMvRxDialogFragment
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.R
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.details.AppDetailsController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.airbnb.mvrx.fragmentViewModel
import kotlinx.android.synthetic.main.dialog_show_apps.view.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class DialogShowApps: BaseDaggerMvRxDialogFragment() {

    companion object {
        private const val TAG = "[SHOW_DIALOG]"
        private lateinit var dialog:DialogShowApps
        /** Shows the about dialog inside of [activity]. */
        fun show(activity: FragmentActivity) {
            dialog = DialogShowApps()
            dialog.show(activity.supportFragmentManager, TAG)
        }
        fun dismissDialog(){
            dialog?.dismiss()
        }
    }
    var mapOfApps: List<App>? = null
    private val viewModel: DialogShowViewModel by fragmentViewModel()
    @Inject
    lateinit var detailsViewModelFactory: DialogShowViewModel.Factory
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: throw IllegalStateException("Oh no!")

        return MaterialDialog(context)
                .customView(R.layout.dialog_show_apps, noVerticalPadding = true)
                .also { it.getCustomView().setUpViews() }
    }

    override fun invalidate() {

    }

    private fun View.setUpViews() {

        item_switch.isChecked = Injector.get().showSystemApps().get()
        val appController = AppDetailsController(true)
        recycler.setController(appController)
        runBlocking {
            mapOfApps = viewModel.fetchAppsByVoice()
            appController.setData(mapOfApps)
        }
        item_switch.setOnCheckedChangeListener { buttonView, isChecked ->

                Injector.get().showSystemApps().set(isChecked)

        }

    }
}