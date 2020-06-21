package com.abhi.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.abhi.base.misc.normalizeString
import com.abhi.base.misc.onTextChanged
import com.abhi.base.misc.showKeyboardOnView
import com.abhi.base.view.onScroll
import com.abhi.ui.R
import com.abhi.ui.base.SharedBaseFrag
import com.abhi.ui.extensions.hideKeyboardWhenNecessary
import kotlinx.android.synthetic.main.frag_search.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel


/**
 * Simple fragment with a search box, a toolbar and a recyclerview.
 */
abstract class BaseSearchFragment : SharedBaseFrag(), CoroutineScope {

    lateinit var viewContainer: FrameLayout

    lateinit var progressBar: ProgressBar

    lateinit var noResultLayout: LinearLayout

    open val showKeyboardWhenLoaded = true

    open val sidePadding = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_search, container, false).apply {
        recyclerView = findViewById(R.id.recycler)
        languageSelector = findViewById(R.id.languageSelector)
        progressBar = findViewById(R.id.progress_circular)
        viewContainer = findViewById(R.id.baseContainer)
        noResultLayout = findViewById(R.id.no_result)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // app might crash if user is scrolling fast and quickly switching screens,
        // so the nullable seems necessary.
        recyclerView.onScroll { _, dy ->
            // this will take care of titleElevation
            // recycler might be null when back is pressed
            val raiseTitleBar = dy > 0 || recyclerView.computeVerticalScrollOffset() != 0
            title_bar?.isActivated = raiseTitleBar // animated via a StateListAnimator
        }

        recyclerView.updatePadding(left = sidePadding, right = sidePadding)


        toolbarMenu.isVisible = showMenu

        if (showMenu) {
            (activity as? AppCompatActivity)?.setSupportActionBar(toolbarMenu)
            toolbarMenu.title = null
        }

        queryInput.onTextChanged { search ->
            queryClear.isInvisible = search.isEmpty()
            recyclerView.smoothScrollToPosition(0)
            onTextChanged(search.toString().normalizeString())
        }

        searchIcon.setOnClickListener {
            queryInput.showKeyboardOnView()
        }

        if (showKeyboardWhenLoaded) {
            queryInput.showKeyboardOnView()
        }

        hideKeyboardWhenNecessary(recyclerView, queryInput)

        queryClear.setOnClickListener { queryInput.setText("") }

        if (closeIconRes == null) {
            close.visibility = View.GONE
        } else {
            val closeIcon = closeIconRes ?: 0
            close.setImageResource(closeIcon)
            close.setOnClickListener { dismiss() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(recyclerView.adapter?.itemCount!! > 0){
            progressBar.visibility = View.GONE
        }
    }
    abstract fun onTextChanged(searchText: String)

    fun setInputHint(hint: String) {
        queryInput?.hint = hint
    }

    fun getInputText(): String = queryInput.text.toString()

    fun scrollToPosition(pos: Int) = recyclerView.scrollToPosition(pos)

    override fun onDestroy() {
        coroutineContext.cancel()
        disposableManager.clear()
        super.onDestroy()
    }
}
