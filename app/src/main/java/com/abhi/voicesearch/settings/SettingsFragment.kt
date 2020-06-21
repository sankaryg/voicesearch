package com.abhi.voicesearch.settings

import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Loading
import com.abhi.base.mvrx.simpleController
import com.abhi.voicesearch.*
import com.abhi.voicesearch.core.AboutDialog
import com.abhi.voicesearch.core.AppManager
import com.abhi.ui.dagger.DaggerBaseRecyclerFragment
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
