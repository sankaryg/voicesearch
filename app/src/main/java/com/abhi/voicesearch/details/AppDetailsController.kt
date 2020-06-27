package com.abhi.voicesearch.details

import com.abhi.voicesearch.Injector
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.TypedEpoxyController
import com.abhi.voicesearch.core.AppManager
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.settings.DialogShowApps
import com.abhi.voicesearch.testAppsItem
import com.orhanobut.logger.Logger

internal class AppDetailsController(fromSettings:Boolean): TypedEpoxyController<List<App>>() {

    private var fromSettings: Boolean = fromSettings

    override fun buildModels(data: List<App>?) {
        val appModels = mutableListOf<com.abhi.voicesearch.SdkHistoryBindingModel_>()

        data?.forEach { app ->
            testAppsItem {
                id(app.packageName)
                this.title(app.title)
                this.image2(AppManager.getIconFromId(app.packageName, app.order))
                onClick{ _ ->
                    Logger.d(app)
                    if(!fromSettings) {
                        AppManager.launchIntentForPackage(app, null,  true)
                        DetailsDialog.dismissDialog()
                    }else{
                        if(Injector.get().showSystemApps().get()){
                            Injector.get().syncInterval().set(app.packageName+"_"+app.title)
                        }
                        DialogShowApps.dismissDialog()
                    }
                }

            }
        }

        CarouselModel_()
                .id("carousel")
                .models(appModels)
                .addTo(this)
    }

}