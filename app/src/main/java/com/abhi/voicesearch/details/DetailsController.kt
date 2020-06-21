package com.abhi.voicesearch.details

import android.graphics.Color
import com.abhi.voicesearch.Injector
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.Typed2EpoxyController
import com.abhi.voicesearch.core.AppManager
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.detailsText
import com.orhanobut.logger.Logger


internal class DetailsController : Typed2EpoxyController<List<String>, App>() {

    override fun buildModels(apps: List<String>, appDetails:App) {

        apps.forEach { app ->
            detailsText {
                id(app)
                this.title(app)
                this.subtitle(app)
                this.color(if(Injector.get().isLightTheme().get()) Color.argb(128,0,0, 0) else Color.argb(128,255,255, 255))
                onClick{ _ ->
                    Logger.d(app)
                    if(AppManager.appInstalledOrNot(appDetails.packageName)){
                        AppManager.launchIntentForPackage(appDetails, app)
                    }else{
                        AppManager.launchBrowser(appDetails, app)
                    }
                    DetailsDialog.dismissDialog()
                }

            }
        }

//        textSeparator {
//            id("separator")
//            this.label(Injector.get().appContext().getString(R.string.target_history))
//        }

        val historyModels = mutableListOf<com.abhi.voicesearch.TestAppsItemBindingModel_>()

//        versions.forEach {
//            historyModels.add(
//                SdkHistoryBindingModel_()
//                    .id(it.targetSdk)
//                    .targetSDKVersion(it.targetSdk.toString())
//                    .title(it.lastUpdateTime.convertTimestampToDate())
//                    .version("V. Code: ${it.version}")
//                    .versionName("V. Name: ${it.versionName}")
//            )
//        }

        CarouselModel_()
            .id("carousel")
            .models(historyModels)
            .addTo(this)
    }



}
