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

@EpoxyModelClass(layout = com.abhi.voicesearch.R.layout.epoxy_layout_logs_item)
abstract class LogsItemModel : DataBindingEpoxyModel(), CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private var drawable: Drawable? = null

    @EpoxyAttribute
    @StringRes
    var packageName: String = ""

    @EpoxyAttribute
    @StringRes
    var title: String = ""

    @EpoxyAttribute
    @StringRes
    var subtitle: String = ""

    @EpoxyAttribute
    @StringRes
    var targetSDKVersion: String = ""

    @EpoxyAttribute
    @StringRes
    var targetSDKDescription: String = ""

    @EpoxyAttribute
    @StringRes
    var apiColor: Int = 0

    @EpoxyAttribute
    var image: Drawable? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onClick: View.OnClickListener? = null

    override fun bind(holder: DataBindingHolder) {
        super.bind(holder)

        job = Job()
//        launch {
//            updateDrawable()
//            holder.dataBinding.setVariable(com.abhi.voicesearch.BR.image, drawable)
//        }
    }

    private suspend inline fun updateDrawable() {
        if (drawable == null) {
            drawable = withContext(Dispatchers.IO) {
                var t1 = AppManager.getIconFromId(packageName)
                t1
            }
        }
    }

    override fun unbind(holder: DataBindingHolder) {
        job.cancel()
        super.unbind(holder)
    }
}