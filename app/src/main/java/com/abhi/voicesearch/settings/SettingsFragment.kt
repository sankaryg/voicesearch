package com.abhi.voicesearch.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Parcelable
import android.text.TextUtils
import com.abhi.base.mvrx.simpleController
import com.abhi.ui.dagger.DaggerBaseRecyclerFragment
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.R
import com.abhi.voicesearch.core.AboutDialog
import com.abhi.voicesearch.loadingRow
import com.abhi.voicesearch.marquee
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.fragmentViewModel
import javax.inject.Inject


class SettingsFragment : DaggerBaseRecyclerFragment() {

    private val viewModel: SettingsViewModel by fragmentViewModel()
    @Inject
    lateinit var settingsViewModelFactory: SettingsViewModel.Factory

    override fun epoxyController(): EpoxyController = simpleController(viewModel) { state ->

        println("state is: ${state.data}")
        if (state.data is Loading) {
            loadingRow { id("loading") }
        }

        if (state.data.complete) {

            marquee {
                id("header")
                title("Settings")
                subtitle("Version ${com.abhi.voicesearch.BuildConfig.VERSION_NAME}")
                clickListenerShare{v->
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND_MULTIPLE
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.share)+" https://play.google.com/store/apps/details?id=${context?.packageName}")
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share with"))

//                    val targetedShareIntents: MutableList<Intent> = ArrayList()
//                    val shareIntent = Intent(Intent.ACTION_SEND)
//                    shareIntent.type = "text/plain";
//
//                    val resInfo: List<ResolveInfo> = context?.packageManager?.queryIntentActivities(shareIntent, 0) as List<ResolveInfo>
//                    for (resolveInfo in resInfo) {
//                        val packageName = resolveInfo.activityInfo.packageName
//                        val targetedShareIntent = Intent(Intent.ACTION_SEND)
//                        targetedShareIntent.type = "text/plain";
//                        targetedShareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share)+" https://play.google.com/store/apps/details?id=${context?.packageName}")
//                        if (TextUtils.equals(packageName, "com.facebook.katana") or TextUtils.equals(packageName, "com.instagram.android")
//                                or TextUtils.equals(packageName, "com.tumblr")) {
//                            targetedShareIntent.setPackage(packageName)
//                            targetedShareIntents.add(targetedShareIntent)
//                        }
//                        if (TextUtils.equals(resolveInfo.activityInfo.name, "com.twitter.android.composer.ComposerActivity")) {
//                            targetedShareIntent.setPackage(packageName)
//                            targetedShareIntent.setClassName(
//                                    resolveInfo.activityInfo.packageName,
//                                    resolveInfo.activityInfo.name)
//                            targetedShareIntents.add(targetedShareIntent)
//                        }
//                    }
//                    val chooserIntent = Intent.createChooser(targetedShareIntents.removeAt(0), "Select app to share")
//                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toTypedArray(arrayOf<Parcelable>()))
//                    startActivity(chooserIntent)

                }
                clickListenerRate{v->
                    val appPackageName = context?.packageName
                    try {
                        context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                    } catch (e: ActivityNotFoundException) {
                        context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                    }
                }
            }

            val lightMode = state.data()?.lightMode ?: true

            com.abhi.voicesearch.SettingsSwitchBindingModel_()
                .id("light mode")
                .title("Light mode")
                .icon(R.drawable.ic_sunny)
                .switchIsVisible(true)
                .switchIsOn(lightMode)
                .clickListener { v ->
                    Injector.get().isLightTheme().set(!lightMode)
                    activity?.recreate()
                }
                .addTo(this)

            val showSystemApps = state.data()?.showSystemApps ?: true
            val app = state.data()?.app?.split("_")?.get(1)
            com.abhi.voicesearch.SettingsSwitchBindingModel_()
                .id("system apps")
                .title(if(showSystemApps) "Quick Search on $app" else "Quick Search")
                .icon(R.drawable.ic_search)
                .subtitle(if (showSystemApps) "Search on start Enabled" else "Search on start Disabled")
                .clickListener { v ->
                        DialogShowApps.show(requireActivity())
                }
                .addTo(this)

            val orderBySdk = state.data()?.showChoices ?: true

            com.abhi.voicesearch.SettingsSwitchBindingModel_()
                .id("order by")
                .title("Voice Recognition")
                .icon(R.drawable.ic_sort)
                .subtitle("Show recognition choices")
                .switchIsVisible(true)
                .switchIsOn(orderBySdk)
                .clickListener { v ->
                    Injector.get().showChoices().set(!orderBySdk)
                }
                .addTo(this)

            com.abhi.voicesearch.SettingsSwitchBindingModel_()
                .id("about")
                .title("About")
                .icon(R.drawable.ic_info)
                .clickListener { v ->
                    AboutDialog.show(requireActivity())
                }
                .addTo(this)
        }
    }
}
