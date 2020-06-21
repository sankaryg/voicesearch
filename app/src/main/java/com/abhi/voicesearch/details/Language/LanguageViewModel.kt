package com.abhi.voicesearch.details.Language

import android.content.Context
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.abhi.base.mvrx.MvRxViewModel
import com.abhi.voicesearch.R
import com.abhi.voicesearch.main.AppDetails
import com.abhi.voicesearch.main.MainState
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class LanguageViewModel @AssistedInject constructor(
        @Assisted initialState:MainState
) :MvRxViewModel<MainState>(initialState){

    fun getListOfLanguages(context: Context): MutableList<AppDetails> {
        var list = mutableListOf<AppDetails>()
        var inputStream = context.resources.openRawResource(R.raw.stats)
        var reader = BufferedReader(InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.toString())))
        for (line in reader.readLines()){
            var tokens = line.split(",")
            var lname = tokens[0]
            var lcode = tokens[1]
            list.add(AppDetails(lname, lcode))
        }
        return list
    }

    @AssistedInject.Factory
    interface Factory{
        fun create(initialState: MainState):LanguageViewModel
    }

    companion object: MvRxViewModelFactory<LanguageViewModel, MainState>{
        override fun create(viewModelContext: ViewModelContext, state: MainState): LanguageViewModel? {
            val fragment:LanguageDialog = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.languageViewModelFactory.create(state)
        }
    }
}