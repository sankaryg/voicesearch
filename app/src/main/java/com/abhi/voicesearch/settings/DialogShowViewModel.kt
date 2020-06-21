package com.abhi.voicesearch.settings

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.abhi.base.mvrx.MvRxViewModel
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.data.source.local.AppsDao
import com.abhi.voicesearch.details.DetailsViewModel
import com.abhi.voicesearch.main.MainState
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DialogShowViewModel @AssistedInject constructor(
    @Assisted initialState: MainState,
    private val mAppsDao: AppsDao
) : MvRxViewModel<MainState>(initialState) {


    suspend fun fetchAppsByVoice(): List<App> = withContext(Dispatchers.IO) {
        mAppsDao.getAppsList()
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: MainState): DialogShowViewModel
    }

    companion object : MvRxViewModelFactory<DialogShowViewModel, MainState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: MainState
        ): DialogShowViewModel? {
            val fragment: DialogShowApps = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.detailsViewModelFactory.create(state)
        }
    }
}
