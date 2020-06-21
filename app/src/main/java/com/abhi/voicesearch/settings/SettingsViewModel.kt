package com.abhi.voicesearch.settings

import com.airbnb.mvrx.*
import com.abhi.base.mvrx.MvRxViewModel
import com.abhi.voicesearch.Injector
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables

data class SettingsData(
    val lightMode: Boolean,
    val showSystemApps: Boolean,
    val backgroundSync: Boolean,
    val orderBySdk: Boolean,
    val showChoices:Boolean,
    val app : String
) : MvRxState

data class SettingsState(
    val data: Async<SettingsData> = Loading()
) : MvRxState

class SettingsViewModel @AssistedInject constructor(
    @Assisted initialState: SettingsState,
    @Assisted private val sources: Observable<SettingsData>
) : MvRxViewModel<SettingsState>(initialState) {

    init {
        fetchData()
    }

    private fun fetchData() = withState {
        sources.execute { copy(data = it) }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(
            initialState: SettingsState,
            sources: Observable<SettingsData>
        ): SettingsViewModel
    }

    companion object : MvRxViewModelFactory<SettingsViewModel, SettingsState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: SettingsState
        ): SettingsViewModel? {

            val source = Observables.combineLatest(
                Injector.get().isLightTheme().observe(),
                Injector.get().showSystemApps().observe(),
                Injector.get().backgroundSync().observe(),
                Injector.get().orderBySdk().observe(),
                    Injector.get().showChoices().observe(),
                    Injector.get().syncInterval().observe()
            ) { dark, system, backgroundSync, orderBySdk,showChoices, app ->
                SettingsData(dark, system, backgroundSync, orderBySdk,showChoices, app)
            }

            val fragment: SettingsFragment =
                (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.settingsViewModelFactory.create(state, source)
        }
    }
}
