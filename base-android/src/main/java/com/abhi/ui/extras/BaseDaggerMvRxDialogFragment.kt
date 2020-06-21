package com.abhi.ui.extras

import android.os.Bundle
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.MvRxViewModelStore
import com.abhi.ui.base.PERSISTED_VIEW_ID_KEY
import dagger.android.support.DaggerDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Make your base Fragment class extend this to get MvRx functionality.
 *
 * This is necessary for the view model delegates and persistence to work correctly.
 */
abstract class BaseDaggerMvRxDialogFragment : DaggerDialogFragment(), MvRxView , CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()
    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    final override val mvrxViewId: String by lazy { mvrxPersistedViewId }
    private lateinit var mvrxPersistedViewId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        mvrxViewModelStore.restoreViewModels(this, savedInstanceState)
        mvrxPersistedViewId =
            savedInstanceState?.getString(PERSISTED_VIEW_ID_KEY)
                    ?: "${this::class.java.simpleName}_${UUID.randomUUID()}"
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvrxViewModelStore.saveViewModels(outState)
        outState.putString(PERSISTED_VIEW_ID_KEY, mvrxViewId)
    }

    override fun onStart() {
        super.onStart()
        // This ensures that invalidate() is called for static screens that don't
        // subscribe to a ViewModel.
        postInvalidate()
    }

    override fun onDestroy() {
        coroutineContext.cancel()
        super.onDestroy()
    }
}