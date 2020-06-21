package com.abhi.voicesearch.details

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.abhi.base.mvrx.MvRxViewModel
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.data.source.local.AppsDao
import com.abhi.voicesearch.main.MainState
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetailsViewModel @AssistedInject constructor(
    @Assisted initialState: MainState,
    private val mAppsDao: AppsDao
) : MvRxViewModel<MainState>(initialState) {

    suspend fun fetchApps(search:String): List<App> = withContext(Dispatchers.IO) {
        mAppsDao.getAppsListByTitle(search)
    }


    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: MainState): DetailsViewModel
    }

    companion object : MvRxViewModelFactory<DetailsViewModel, MainState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: MainState
        ): DetailsViewModel? {
            val fragment: DetailsDialog = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.detailsViewModelFactory.create(state)
        }
    }
}
