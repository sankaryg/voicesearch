package com.abhi.ui.base

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.BaseMvRxFragment
import com.abhi.ui.R
import com.abhi.ui.extras.GridSpacingItemDecoration


/**
 * Really basic fragment, inspired from TiVi, with a lazy RecyclerView and most MvRx methods,
 * to reduce overall boilerplate.
 */
abstract class TiviMvRxFragment : BaseMvRxFragment() {

    lateinit var recyclerView: EpoxyRecyclerView

    lateinit var languageSelector:ImageView

    var spanCount = 3

    private val epoxyController by lazy { epoxyController() }

    abstract fun epoxyController(): EpoxyController

    /** Define the layoutManager to be used, by default Linear */
    open fun layoutManager(): RecyclerView.LayoutManager = GridLayoutManager(context,spanCount)

    private lateinit var mvrxPersistedViewId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = layoutManager().apply {
            (this as? GridLayoutManager)?.recycleChildrenOnDetach = true
        }
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_layout_margin)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, spacingInPixels, true, 0))
        recyclerView.setController(epoxyController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        epoxyController.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        epoxyController.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {

        epoxyController.cancelPendingModelBuild()
        // this kills animations, but was the only way I found to avoid crashes when
        // switching and coming back to the same fragment (via back button) fast.
//        recyclerView.adapter = null

//        recyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//            override fun onViewAttachedToWindow(v: View) = Unit // no-op
//
//            override fun onViewDetachedFromWindow(v: View) {
//                recyclerView.adapter = null
//            }
//        })

        super.onDestroyView()
    }

    override fun invalidate() {
        recyclerView.requestModelBuild()
    }

    fun getModelAtPos(pos: Int): EpoxyModel<*>? {
        return try {
            epoxyController.adapter.getModelAtPosition(pos)
        } catch (e: IllegalStateException) {
            null
        }
    }
}

const val PERSISTED_VIEW_ID_KEY = "mvrx:persisted_view_id"
