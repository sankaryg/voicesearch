package com.abhi.voicesearch.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.*
import com.abhi.base.misc.toDp
import com.abhi.base.mvrx.simpleController
import com.abhi.base.rx.plusAssign
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.R
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.details.Language.LanguageDialog
import com.abhi.voicesearch.emptyContent
import com.abhi.voicesearch.loadingRow
import com.abhi.voicesearch.util.InsetDecoration
import com.abhi.ui.dagger.DaggerBaseSearchFragment
import com.google.firebase.messaging.FirebaseMessaging
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject


data class AppDetails(val title: String, val subtitle: String)

data class MainState(val listOfItems: Async<List<App>> = Loading()) : MvRxState

class MainFragment : DaggerBaseSearchFragment() {

    private val TOPIC = "voice-search"

    private val viewModel: MainViewModel by fragmentViewModel()
    @Inject
    lateinit var mainViewModelFactory: MainViewModel.Factory

    lateinit var fastScroller: View

    lateinit var speechHandler: SpeechRecognizerandler

    override val showKeyboardWhenLoaded = false

    var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        speechHandler = activity as SpeechRecognizerandler
        createChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_name))
        subscribeTopic()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun createChannel(channelId: String, channelName: String) {
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    // TODO: Step 2.4 change importance
                    NotificationManager.IMPORTANCE_HIGH
            )
                    // TODO: Step 2.6 disable badges for this channel
                    .apply {
                        setShowBadge(false)
                    }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_channel_name)

            val notificationManager = requireActivity().getSystemService(
                    NotificationManager::class.java
            )

            notificationManager.createNotificationChannel(notificationChannel)

        }
        // TODO: Step 1.6 END create channel
    }

    // TODO: Step 3.3 subscribe to breakfast topic
    private fun subscribeTopic() {
        // [START subscribe_topic]
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                .addOnCompleteListener { task ->

                    if (!task.isSuccessful) {

                    }
                    //Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
        // [END subscribe_topics]
    }

    override fun onTextChanged(searchText: String) {
        viewModel.inputRelay.accept(searchText)
        if(viewModel.itemsList.isEmpty()){
            noResultLayout.visibility = View.VISIBLE
        }else if(viewModel.itemsList.size > 0){
            if(noResultLayout.isVisible){
                noResultLayout.visibility = View.GONE
            }
        }
    }

    private val standardItemDecorator by lazy {
        val isRightToLeft =
            TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL

        // the padding should be on right side, but because of RTL layouts, it can change.
        InsetDecoration(
            resources.getDimensionPixelSize(R.dimen.right_padding_for_fast_scroller),
            isRightToLeft,
            !isRightToLeft
        )
    }

    override fun epoxyController() = simpleController(viewModel) { state ->

        when (state.listOfItems) {
            is Loading -> {
                print("loading...")
                //loadingRow { id("loading") }
                progressBar.visibility = View.VISIBLE
            }
            else -> {
                if (state.listOfItems()?.isEmpty() == true) {
                    val label = if (state.listOfItems is Fail) {
                        state.listOfItems.error.localizedMessage
                    } else {
                        getString(R.string.empty_search)
                    }

//                    emptyContent {
//                        this.id("empty")
//                        this.label(label)
//                    }
                }
            }
        }

        state.listOfItems()?.forEach {
            val item = it

            com.abhi.voicesearch.views.LogsItemModel_()
                .id(item.packageName)
                .title(item.title)
                .packageName(item.packageName)
                .onClick { v ->
                    Logger.d(it.title)
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Injector.get().selectLanguage().get())
                    if(it.order == 3)
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search for an "+it.title)
                    else
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search "+it.title)
                    try {
                        speechHandler.selectedAppForSpeech(it, requireActivity())
                        activity?.startActivityForResult(intent, 99)
                    }catch (e:ActivityNotFoundException){
                        Logger.d("Your device not supported")
                    }
                }
                .addTo(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.updatePadding(
            left = 8.toDp(resources),
            bottom = 8.toDp(resources),
            right = 8.toDp(resources),
            top = 8.toDp(resources)
        )

        viewModel.inputRelay.accept(getInputText())

        fastScroller = viewContainer.inflateFastScroll()

        fastScroller.setupFastScroller(recyclerView, activity) {
            if (getModelAtPos(it) is com.abhi.voicesearch.views.LogsItemModel_) {

                viewModel.itemsList.getOrNull(it)
            }
            else {

                null
            }
        }


        setInputHint("Loading...")

        languageSelector.setOnClickListener {
           LanguageDialog.show(requireActivity())
        }

        disposableManager += viewModel.maxListSize.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setInputHint(resources.getQuantityString(R.plurals.searchApps, it, it))
                if(progressBar.isVisible){
                    progressBar.visibility = View.GONE
                }
            }

        disposableManager += Injector.get().showSystemApps().observe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{quickSearch ->
                    val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                    if (navHostFragment != null) {
                       val fr =  navHostFragment.childFragmentManager.fragments[0]
                       Logger.d(fr)

                    if(quickSearch && fr is MainFragment) {
                        runBlocking {
                            val app = viewModel.fetchAppsByPN(Injector.get().syncInterval().get().split("_")[0])
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Injector.get().selectLanguage().get())
                            if (app != null) {
                                if (app.order == 3)
                                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search for an " + app.title)
                                else
                                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search " + app.title)
                                try {
                                    speechHandler.selectedAppForSpeech(app, requireActivity())
                                    activity?.startActivityForResult(intent, 99)
                                } catch (e: ActivityNotFoundException) {
                                    Logger.d("Your device not supported")
                                }
                            }
                        }
                    }
                    }
                }
        // observe when order changes
        disposableManager += Injector.get().orderBySdk().observe()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { orderBySdk ->
                fastScroller.isVisible = !orderBySdk

                if (orderBySdk) {
                    recyclerView.removeItemDecoration(standardItemDecorator)
                } else {
                    recyclerView.addItemDecoration(standardItemDecorator)
                }
            }
    }

    override val closeIconRes: Int? = null
}
