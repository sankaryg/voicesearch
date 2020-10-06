package com.abhi.voicesearch.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.franmontiel.attributionpresenter.AttributionPresenter
import com.franmontiel.attributionpresenter.entities.Attribution
import com.franmontiel.attributionpresenter.entities.Library
import com.franmontiel.attributionpresenter.entities.License
import javax.inject.Inject


class SettingsFragment : DaggerBaseRecyclerFragment() {

    private val viewModel: SettingsViewModel by fragmentViewModel()
    @Inject
    lateinit var settingsViewModelFactory: SettingsViewModel.Factory
    private lateinit var sharingShortcutsManager: SharingShortcutsManager

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
//                    val shareIntent = Intent().apply {
//                        action = Intent.ACTION_SEND_MULTIPLE
//                        putExtra(Intent.EXTRA_TEXT, getString(R.string.share)+" https://play.google.com/store/apps/details?id=${context?.packageName}")
//                        type = "text/plain"
//                    }
//                    startActivity(Intent.createChooser(shareIntent, "Share with"))
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share)+" https://play.google.com/store/apps/details?id=${context?.packageName}")

                    startActivity(Intent.createChooser(sharingIntent, "Share with"))

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

            com.abhi.voicesearch.SettingsSwitchBindingModel_()
                    .id("attribution")
                    .title("Attribution")
                    .icon(R.drawable.ic_info)
                    .clickListener { v ->
                        create(requireActivity())?.showDialog("Open Source Licenses")
                    }
                    .addTo(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharingShortcutsManager = SharingShortcutsManager().also {
            context?.let { it1 -> it.pushDirectShareTargets(it1) }
        }
    }

    companion object{
        private fun createBaseAttributions(context: Context): AttributionPresenter.Builder? {
            return AttributionPresenter.Builder(context)
                    .addAttributions(
                            Attribution.Builder("EPOXY")
                                    .addCopyrightNotice("Copyright 2016 Airbnb, Inc.")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/airbnb/epoxy")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Indicator Fast Scroll")
                                    .addCopyrightNotice("Copyright (c) 2018 Reddit, Inc.")
                                    .addLicense(License.MIT)
                                    .setWebsite("https://github.com/reddit/IndicatorFastScroll")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Logger")
                                    .addCopyrightNotice("Copyright 2018 Orhan Obut")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/orhanobut/logger")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Material-Dialogs")
                                    .addCopyrightNotice("Copyright 2018 Aidan Follestad")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/afollestad/material-dialogs")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("rxkprefs")
                                    .addCopyrightNotice("Copyright 2016 Aidan Follestad")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/afollestad/rxkprefs")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("RxRelay")
                                    .addCopyrightNotice("Copyright 2015 Jake Wharton")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/JakeWharton/RxRelay")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("AttributionPresenter")
                                    .addCopyrightNotice("Copyright 2017 Francisco José Montiel Navarro")
                                    .addLicense(License.APACHE)
                                    .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                                    .build()
                    )
                    .addAttributions(
                            Library.DAGGER_2,
                            Library.GSON)
                    .addAttributions(
                            Attribution.Builder("Facebook Icon")
                                    .addCopyrightNotice("Icon made by Pixel perfect(https://www.flaticon.com/authors/pixel-perfect) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/facebook_2111392?term=facebook&page=1&position=3")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("IMDB Icon")
                                    .addCopyrightNotice("Icon made by Pixel perfect(https://www.flaticon.com/authors/pixel-perfect) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/imdb_889199?term=imdb&page=1&position=1")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Rotten Tomatoes Icon")
                                    .addCopyrightNotice("Icon made by Pixel perfect(https://www.flaticon.com/authors/pixel-perfect) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/tomato_1202125?term=tomatoes&page=1&position=4")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Twitter Icon")
                                    .addCopyrightNotice("Icon made by Pixel perfect(https://www.flaticon.com/authors/pixel-perfect) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/twitter_733579?term=twitter&page=1&position=1")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Wikipedia Icon")
                                    .addCopyrightNotice("Icon made by Freepik(https://www.flaticon.com/authors/freepik) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/wikipedia_226240?term=wikipedia&page=1&position=25")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Yahoo Icon")
                                    .addCopyrightNotice("Icon made by Pixel perfect(https://www.flaticon.com/authors/pixel-perfect) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/yahoo_226262?term=yahoo&page=1&position=1")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Bing Icon")
                                    .addCopyrightNotice("Icon made by Freepik(https://www.flaticon.com/authors/freepik) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/bing_356066?term=bing&page=1&position=20")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Reddit Icon")
                                    .addCopyrightNotice("Icon made by Freepik(https://www.flaticon.com/authors/freepik) from Flaticon(Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/reddit_1409938?term=reddit&page=1&position=2")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Amazon Icon")
                                    .addCopyrightNotice("Icon made by Icons8(https://icons8.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://icons8.com/icons/set/amazon-icon")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("DuckDuckGo Icon")
                                    .addCopyrightNotice("Icon made by Icons8(https://icons8.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://icons8.com/icons/set/duckduckgo")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("EBay Icon")
                                    .addCopyrightNotice("Icon made by Icons8(https://icons8.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://icons8.com/icons/set/ebay")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Google Translate Icon")
                                    .addCopyrightNotice("Icon made by Icons8(https://icons8.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://icons8.com/icons/set/google-translate-icon")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("Google Icon")
                                    .addCopyrightNotice("Icons made by Freepik (Freepik.com) from Flaticon (Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/search_281764?term=google&page=1&position=3")
                                    .build()
                    )
                    .addAttributions(
                            Attribution.Builder("YouTube Icon")
                                    .addCopyrightNotice("Icons made by Freepik (Freepik.com) from Flaticon (Flaticon.com)")
                                    .addLicense(License.CREATIVE_COMMEON)
                                    .setWebsite("https://www.flaticon.com/free-icon/youtube_1384060?term=youtube&page=1&position=2")
                                    .build()
                    )
        }

        fun create(context: Context?): AttributionPresenter? {
            return context?.let { createBaseAttributions(it)?.build() }
        }
    }


}
