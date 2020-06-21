package com.abhi.voicesearch.main

import com.abhi.voicesearch.data.App
import io.reactivex.Observable


interface MainDataSource {

    fun setShouldShowSystemApps(value: Boolean)

    fun shouldOrderBySdk(): Observable<Boolean>

    fun getAppsList(): Observable<List<App>>

    fun getAppByPackageName(packageName:String):App?

    fun getAppsListByTitle(search:String): List<App>

}
