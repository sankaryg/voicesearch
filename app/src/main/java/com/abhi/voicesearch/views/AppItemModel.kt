package com.abhi.voicesearch.views

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.R
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.abhi.voicesearch.core.AppManager
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@EpoxyModelClass(layout = com.abhi.voicesearch.R.layout.epoxy_layout_test_apps_item)
abstract class AppItemModel:DataBindingEpoxyModel(), CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private var drawable: Drawable? = null

    @EpoxyAttribute
    @StringRes
    var title: String = ""

    @EpoxyAttribute
    @StringRes
    var packageName: String = ""

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onClick: View.OnClickListener? = null

    override fun bind(holder: DataBindingHolder) {
        super.bind(holder)

        job = Job()
        launch {
            updateDrawable()
            //holder.dataBinding.setVariable(BR.image2, drawable)
        }
    }

    private suspend inline fun updateDrawable() {
        if (drawable == null) {
            drawable = withContext(Dispatchers.IO) {
                var t1 = AppManager.getIconFromId(packageName)
                if(t1 == null){
                    val app = Injector.get().appsDao().getApp(packageName)
                    when(app?.order){
                        3-> t1 = ContextCompat.getDrawable(Injector.get().appContext(), R.drawable.ic_baseline_apps_24)
                    }
                }
                t1
            }
        }
    }

    override fun unbind(holder: DataBindingHolder) {
        job.cancel()
        super.unbind(holder)
    }

}