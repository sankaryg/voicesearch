package com.abhi.voicesearch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.abhi.voicesearch.core.AppManager
import com.abhi.voicesearch.core.BackDialog
import com.airbnb.mvrx.BaseMvRxActivity
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.data.source.local.AppsDao
import com.abhi.voicesearch.details.DetailsDialog
import com.abhi.voicesearch.main.SpeechRecognizerandler
import com.abhi.voicesearch.settings.SharingShortcutsManager
import com.abhi.voicesearch.util.toast
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : BaseMvRxActivity(), SpeechRecognizerandler {
    private lateinit var requireActivity: FragmentActivity
    lateinit var item: App
    lateinit var mAppsDao:AppsDao

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Injector.get().isLightTheme().get()) {
            setTheme(R.style.AppThemeLight)
        } else {
            setTheme(R.style.AppThemeDark)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NavigationUI.setupWithNavController(
            bottom_nav,
            nav_host_fragment.findNavController()
        )
        mAppsDao = MainApplication.get().component.appsDao()
        if(Injector.get().showBackDialog().get() == 12){
            BackDialog.show(this, true)
        }

    }

    suspend fun fetchApps(search:String): List<App> = withContext(Dispatchers.IO) {
        mAppsDao.getAppsListByTitle(search)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.d("debug ="+requestCode+"_"+resultCode);
        when (requestCode) {
            99 -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                  var result =  data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Logger.d(result)
                    if(result.size > 0){
                        if(!Injector.get().showChoices().get()){
                            var search = result[0]
                            if(item.order ==3){
                                search = AppManager.removeDuplicateWordFromString(result[0])
                                runBlocking {
                                    val apps = fetchApps(search)
                                    if(apps.isEmpty()){
                                        toast(getString(R.string.empty_search))
                                    }else{
                                        if(AppManager.appInstalledOrNot(apps[0].packageName)) {
                                            AppManager.launchIntentForPackage(apps[0], null, true)
                                        }else{
                                            if(AppManager.appInstalledOrNot(apps[1].packageName)) {
                                                AppManager.launchIntentForPackage(apps[1], null, true)
                                            }
                                        }
                                    }
                                }

                            }
                            else if(AppManager.appInstalledOrNot(item.packageName)){
                                AppManager.launchIntentForPackage(item, search)
                            }else{
                                AppManager.launchBrowser(item, result[0])
                            }
                        }else
                        DetailsDialog.show<MainActivity>(this, item, result);
                    }else{
                        toast(getString(R.string.empty_search))
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(Injector.get().showBackDialog().get() != -1){
                if(Injector.get().showBackDialog().get() == 12){
                    Injector.get().showBackDialog().set(4)
                }else if(Injector.get().showBackDialog().get() > 4){
                    var count = Injector.get().showBackDialog().get()
                    Injector.get().showBackDialog().set(count + 1)
                }else {
                    BackDialog.show(this)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun selectedAppForSpeech(item: App, requireActivity: FragmentActivity) {
        Logger.d(item)
        this.item = item
        this.requireActivity = requireActivity
    }
}
