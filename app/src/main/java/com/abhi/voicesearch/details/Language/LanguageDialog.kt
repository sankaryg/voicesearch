package com.abhi.voicesearch.details.Language

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.R
import com.abhi.voicesearch.util.toast
import com.abhi.ui.extras.BaseDaggerMvRxDialogFragment
import com.airbnb.mvrx.fragmentViewModel
import javax.inject.Inject


class LanguageDialog: BaseDaggerMvRxDialogFragment() {

    private var initialSelection: Int = 0
    private val viewModel: LanguageViewModel by fragmentViewModel()
    @Inject
    lateinit var languageViewModelFactory: LanguageViewModel.Factory


    companion object{
        private const val TAG = "[LanguageDialog]"
        private lateinit var dialog:LanguageDialog
        fun <T> show(fragment:T) where T: FragmentActivity {
            dialog = LanguageDialog().apply {  }
            val ft = fragment.supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(TAG)
            dialog.show(ft, TAG)
        }

        fun dismissDialog(){
            dialog.dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: blowUp()
        val list1 = viewModel.getListOfLanguages(context)
        var list2  = list1.sortedBy { it.title }
        var charArray = arrayListOf<CharSequence>()
        var language  = Injector.get().selectLanguage().get()
        list2.forEachIndexed{ index, element->
            charArray.add(index, element.title)
            if(language == element.subtitle){
                initialSelection = index
            }
        }
        return MaterialDialog(context)
                .title(R.string.languages)
                .listItemsSingleChoice(items = charArray, initialSelection = initialSelection){ _, index, text ->
                    //activity?.toast("selected item $text at index $index")
                    Injector.get().selectLanguage().set(list2[index].subtitle)
                }
    }

    override fun invalidate() {

    }

    private fun <T> blowUp(): T {
        throw IllegalStateException("Oh no!")
    }

    override fun onStart() {
        super.onStart()
        // This ensures that invalidate() is called for static screens that don't
        // subscribe to a ViewModel.
        postInvalidate()
    }
}