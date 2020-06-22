package com.abhi.voicesearch.details

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.abhi.voicesearch.MainActivity
import com.abhi.voicesearch.R
import com.abhi.voicesearch.core.AppManager
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.extensions.darken
import com.abhi.ui.extras.BaseDaggerMvRxDialogFragment
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.util.toast
import com.airbnb.mvrx.fragmentViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.details_fragment.view.*
import kotlinx.coroutines.runBlocking
import java.util.ArrayList
import javax.inject.Inject

class DetailsDialog : BaseDaggerMvRxDialogFragment() {

    private val viewModel: DetailsViewModel by fragmentViewModel()
    @Inject
    lateinit var detailsViewModelFactory: DetailsViewModel.Factory



    companion object {
        private const val TAG = "[DetailsDialog]"
        private const val KEY_APP = "app"
        private const val KEY_RESULT  = "result"
        private lateinit var dialog:DetailsDialog
        fun <T> show(
                fragment: MainActivity,
                app: App,
                result: ArrayList<String>
        ) where T : MainActivity {
             dialog = DetailsDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_APP, app)
                    putStringArrayList(KEY_RESULT, result)
                }
            }

            val ft = fragment.supportFragmentManager
                .beginTransaction()
                .addToBackStack(TAG)

            dialog.show(ft, TAG)
        }

        fun dismissDialog(){
            dialog.dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: blowUp()

        val args = arguments ?: blowUp()
        val app = args.getParcelable(KEY_APP) as? App ?: blowUp()
        val list = args.getStringArrayList(KEY_RESULT)
        return MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT))
            .customView(R.layout.details_fragment, noVerticalPadding = true)
            .also { it.getCustomView().setUpViews(app, list) }
    }
    var mapOfApps: List<App>? = null
    private fun View.setUpViews(app: App, list: ArrayList<String>?) {

        titlecontent.text = resources.getString(R.string.results)

        //title_bar.background = ColorDrawable(app.backgroundColor.darken.darken)

        if(Injector.get().isLightTheme().get()){
            closecontent.setImageResource(R.drawable.ic_close_dark)
        }else{
            closecontent.setImageResource(R.drawable.ic_close)
        }

        closecontent.setOnClickListener { dismiss() }

        //recycler.background = ColorDrawable(app.backgroundColor.darken)
        if(app.order == 3) {
            val appController = AppDetailsController(false)
            recycler.setController(appController)
            runBlocking {
                    val search = AppManager.removeDuplicateWordFromString(list!!.get(0))
                    mapOfApps = viewModel.fetchApps(search)
                    if(mapOfApps!!.isEmpty()){
                        activity?.toast(getString(R.string.empty_search))
                        dismissDialog()
                    }else {
                        appController.setData(mapOfApps)
                    }

            }

        }

        else{
            val detailsController = DetailsController()
            recycler.setController(detailsController)

            runBlocking {
                //val packageName = app.packageName
                detailsController.setData(list, app)
                //val data = viewModel.fetchAppDetails(packageName)
                //if (data.isEmpty())
                //{
                    //AppManager.removePackageName(packageName)
                    //this@DetailsDialog.dismiss()
               // } else {
                    //val versions = viewModel.fetchAllVersions(packageName)

                //}
            }
        }
        /*play_store.also {
            it.isVisible = app.isFromPlayStore
            it.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${app.packageName}")
                )

                startActivity(intent)
            }
        }

        info.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + app.packageName)
            startActivity(intent)
        }*/


    }

    override fun invalidate() {

    }

    private fun <T> blowUp(): T {
        throw IllegalStateException("Oh no!")
    }

    override fun onStart() {
        super.onStart()
        // This ensures that invalidate() is called for static screens that don't
        // subscribe to a ViewModel.
        postInvalidate()
    }
}
