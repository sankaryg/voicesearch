@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.abhi.voicesearch.core

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.abhi.voicesearch.Injector
import com.abhi.voicesearch.R
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.data.FixedApp
import com.abhi.voicesearch.extensions.darken
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.ArrayList


object AppManager {

    private lateinit var context: Context
    private const val PACKAGE_ANDROID_VENDING = "com.android.vending"
    private const val OUTSIDE_STORE = "com.google.android.packageinstaller"
    private const val PREF_DISABLED_PACKAGES = "disabled_packages"

    private lateinit var packageManager: PackageManager
    var forceRefresh = true
    var listOfFixedApp:List<FixedApp> = mutableListOf()

    fun init(context: Context) {
        this.context = context
        packageManager = context.packageManager
    }



    fun launchIntentForPackage(app: App, query: String?, default:Boolean = false) {
        try {
            var intent: Intent? = null
            var queryBool: Boolean = true
            if(!default) {
                when (app.order) {
                    1 -> intent = Intent(Intent.ACTION_SEARCH)
                    2 -> intent = Intent(Intent.ACTION_WEB_SEARCH)
                    0, 3 ->
                        intent = packageManager.getLaunchIntentForPackage(app.packageName)
                    4 ->
                        Intent(Intent.ACTION_VIEW, Uri.parse("http://images.google.com")).also { intent = it }
                    5, 6, 7, 8, 10, 11, 12, 17, 19, 99, 26 -> {
                        intent = Intent("com.google.android.gms.actions.SEARCH_ACTION")
                    }
                    20 -> {
                        intent = Intent("android.intent.action.VIEW").setData(Uri.parse(("https://play.google.com/store/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString()) + "")));
                        queryBool = false
                    }
                    9 -> {
                        intent = Intent("android.intent.action.SEARCH", ContactsContract.Contacts.CONTENT_URI);
                    }
                    23 -> {
                        intent = Intent("android.intent.action.SEARCH", CalendarContract.Calendars.CONTENT_URI);
                    }
                    13, 15, 16, 18, 24, 25, 28 -> {
                        intent = sendIntent(query)
                        queryBool = false
                    }

                    else ->
                        query?.let { launchBrowser(app, it) }
                }
            }else{
                intent = packageManager.getLaunchIntentForPackage(app.packageName)
            }
            if (app.order!=9 && app.order!=19 &&  app.order != 20)
            intent?.setPackage(app.packageName)
            if(app.order == 19){
                intent?.component = ComponentName(app.packageName, app.packageName+".ui.search.ClusteredSearchActivity")
            }
            if (query != null && queryBool)
                intent?.putExtra(SearchManager.QUERY, query)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)
        }catch (e:ActivityNotFoundException){
            if (query != null) {
                launchBrowser(app,query)
            }
        }catch (e:SecurityException){

        }
    }

    private fun sendIntent(query:String?): Intent {
        var intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, query)
        intent.type = "text/plain"
        return intent
    }


    fun appInstalledOrNot(uri: String): Boolean {
        val pm: PackageManager = packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }
    private fun isUserApp(ai: ApplicationInfo?): Boolean {
        // https://stackoverflow.com/a/14665381/4418073
        if (ai == null) return false
        val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        return ai.flags and mask == 0
    }

    // verifies if app came from Play Store or was installed manually
    fun doesAppHasOrigin(packageName: String): Boolean {
        return isUserApp(getApplicationInfo(packageName))
    }

    fun getPackages(): MutableList<ApplicationInfo>? =
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    fun getPackagesWithAction(){

    }

    suspend fun removePackageName(packageName: String) = withContext(Dispatchers.IO) {
        Injector.get().appsDao().deleteApp(packageName)
    }


    // there are apps with extra space on the name
    private fun getAppLabel(packageInfo: ApplicationInfo) =
        packageManager.getApplicationLabel(packageInfo).toString().trim()

    fun resourceJson(): ArrayList<FixedApp> {
        var jsonString:String = ""
        var fixedapp:ArrayList<FixedApp> = ArrayList()
        try {
            jsonString = context.resources.openRawResource(R.raw.ai).bufferedReader().use { it.readText() }
            val gson = Gson()
            val listApp = object : TypeToken<List<FixedApp>>() {}.type
            fixedapp = gson.fromJson(jsonString, listApp)
            Log.d("t1", jsonString+"_"+listApp)
            fixedapp.forEachIndexed{idx, fixed ->
                Log.d("t1", "> Item $idx:\n$fixed"+"_"+ context)
                Logger.i("data", "> Item $idx:\n$fixed")
            }
        }catch (ioException:IOException){
            ioException.printStackTrace()
            null
        }
        return fixedapp
    }


    fun insertFixedApp(){
        var fixedapp:ArrayList<FixedApp> = resourceJson();
        var intent = Intent("com.google.android.gms.actions.SEARCH_ACTION")
        var li = packageManager.queryIntentActivities(intent, 0)
        if(li.size > 0) {
            for (ri in li) {
                try {
                val package_name = ri.activityInfo.packageName;
                val app_name:String = packageManager.getApplicationLabel(packageManager.getApplicationInfo(package_name, PackageManager.GET_META_DATA)) as String
                var fa = FixedApp(package_name,99,app_name, false)
                fixedapp.add(fa)
                }catch (e:Exception){
                    Logger.d(e)
                }
            }
        }
        if(fixedapp.isNotEmpty()){
            fixedapp.forEachIndexed { _, app ->
                run {
                    if(!app.inserted && !appInstalledOrNot(app.pname)){

                    }
                    else if (Injector.get().appsDao().getAppString(app.pname) == null) {
                        Injector.get().appsDao().insertApp(
                                App(
                                        packageName = app.pname,
                                        title = app.name,
                                        backgroundColor = 0,
                                        isFromPlayStore = false,
                                        showOnlyVoiceEnabled = true,
                                        order = app.order
                                )
                        )
                    }
                }
            }
        }
    }

    fun insertNewApp(packageInfo: ApplicationInfo) {

        if (Injector.get().appsDao().getAppString(packageInfo.packageName) != null) return

        val icon = packageManager.getApplicationIcon(packageInfo).toBitmap()
        val backgroundColor = getPaletteColor(Palette.from(icon).generate())
        val label = getAppLabel(packageInfo)

        Injector.get().appsDao().insertApp(
            App(
                packageName = packageInfo.packageName,
                title = label,
                backgroundColor = backgroundColor,
                isFromPlayStore = isUserApp(packageInfo),
                showOnlyVoiceEnabled = false,
                    order = 0
            )
        )
    }

    private fun getPaletteColor(palette: Palette?, defaultColor: Int = 0) = when {
        palette?.darkVibrantSwatch != null -> palette.getDarkVibrantColor(defaultColor)
        palette?.vibrantSwatch != null -> palette.getVibrantColor(defaultColor)
        palette?.mutedSwatch != null -> palette.getMutedColor(defaultColor)
        palette?.darkMutedSwatch != null -> palette.getDarkMutedColor(defaultColor)
        palette?.lightMutedSwatch != null -> palette.getMutedColor(defaultColor).darken
        palette?.lightVibrantSwatch != null -> palette.getLightVibrantColor(defaultColor).darken
        else -> defaultColor
    }

    fun getPackagesWithUserPrefs(): MutableList<ApplicationInfo>? {
        /*return if (Injector.get().showSystemApps().get()) {
            getPackages()
        } else {
            getPackagesWithOrigin()
        }*/
        return getPackages()
    }

    /*private fun getPackagesWithOrigin(): List<PackageInfo> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { isUserApp(it.applicationInfo) }
    }*/

    fun getPackageInfo(packageName: String): ApplicationInfo? {
        return try {
            packageManager.getPackageInfo(packageName, 0).applicationInfo
        } catch (e: PackageManager.NameNotFoundException) {

            null
        }
    }

    fun getApplicationInfo(packageName: String): ApplicationInfo? {
        return getPackageInfo(packageName)
    }

    fun getIconFromId(packageName: String, order:Int=3): Drawable? {
        return try {
            packageManager.getApplicationIcon(getApplicationInfo(packageName))
        } catch (e: Exception) {
            when(order) {
                1 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_youtube, context.theme)
                2 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_google_search, context.theme)
                3 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_iconfinder_android_317758, context.theme)
                4 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_baseline_image_search_24, context.theme)
                5 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_facebook, context.theme)
                6 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_icons8_amazon, context.theme)
                7 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_bing, context.theme)
                8 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_icons8_duckduckgo, context.theme)
                11 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_icons8_ebay, context.theme)
                14 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_imdb, context.theme)
                21 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_reddit, context.theme)
                22 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_tomato__1_, context.theme)
                25 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_icons8_google_translate, context.theme)
                26 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_twitter__1_, context.theme)
                27 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_wikipedia__1_, context.theme)
                29 -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_yahoo__1_, context.theme)
                else-> null
            }
        }
    }

    fun removeDuplicateWordFromString(s:String): String {
       val strWordsBySpace = s.split("\\s+".toRegex())
       val teat = strWordsBySpace.distinct()
       /*val lhSetWords:LinkedHashSet<String> = linkedSetOf<String>(listOf(strWordsBySpace).toString())
        val sbTemp = StringBuilder()
        for ((index, s) in lhSetWords.withIndex()){
            if(index > 0){
                sbTemp.append(" ")
            }
            sbTemp.append(s)
        }*/
        return  teat[0]
    }

    fun launchBrowser(app: App, query: String) {
        var intent:Intent? = null
        when(app.order){
            4-> {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?tbm=isch&q="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString()))).also { intent = it }
            }
            5->{
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/search/top/?q="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            6->{
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/s/ref=nb_sb_noss_2?field-keywords="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            7->{
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bing.com/search?q="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            8->{
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://duckduckgo.com/?q="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            9->{
                intent = Intent("android.intent.action.SEARCH", ContactsContract.Contacts.CONTENT_URI);
            }
            11->{
                    var url = "https://www.ebay.com/sch/items/?_nkw="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
            14->{
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/find?q="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            20->{
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            21->{
                intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/search?q="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            22->{
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.rottentomatoes.com/search/?search="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            25->{
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://translate.google.com/#auto/"+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            27->{
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/w/index.php?search="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }
            29 ->{
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://search.yahoo.com/search?p="+URLEncoder.encode(query, StandardCharsets.UTF_8.toString())))
            }

            else->
                intent = Intent()
        }
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

}