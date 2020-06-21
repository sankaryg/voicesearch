package com.abhi.voicesearch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.abhi.voicesearch.core.AppManager
import com.airbnb.mvrx.BaseMvRxActivity
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.details.DetailsDialog
import com.abhi.voicesearch.main.MainDataSource
import com.abhi.voicesearch.main.SpeechRecognizerandler
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseMvRxActivity(), SpeechRecognizerandler {
    private lateinit var requireActivity: FragmentActivity
    lateinit var item: App
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
                                AppManager.launchIntentForPackage(item, search)
                            }
                            else if(AppManager.appInstalledOrNot(item.packageName)){
                                AppManager.launchIntentForPackage(item, search)
                            }else{
                                AppManager.launchBrowser(item, result[0])
                            }
                        }else
                        DetailsDialog.show<MainActivity>(this, item, result);
                    }
                }
            }
        }
    }

    override fun selectedAppForSpeech(item: App, requireActivity: FragmentActivity) {
        Logger.d(item)
        this.item = item
        this.requireActivity = requireActivity
    }
}
