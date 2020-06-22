package com.abhi.voicesearch.main

import com.afollestad.rxkprefs.Pref
import com.abhi.voicesearch.data.App
import com.abhi.voicesearch.data.source.local.AppsDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseDataSource @Inject constructor(
    private val mAppsDao: AppsDao,
    private val orderBySdk: Pref<Boolean>,
    private val showSystemApps: Pref<Boolean>
) : MainDataSource {

    override fun setShouldShowSystemApps(value: Boolean) {
        showSystemApps.set(value)
    }

    override fun shouldOrderBySdk(): Observable<Boolean> = orderBySdk.observe()

    override fun getAppsList(): Observable<List<App>> {

        return showSystemApps.observe().switchMap {
            mAppsDao.getAppsListFlowableFiltered()
        }
    }

    override fun getAppByPackageName(packageName: String): App? {
        return mAppsDao.getApp(packageName)
    }

    override fun getAppsListByTitle(search: String): List<App> {
       return mAppsDao.getAppsListByTitle(search)
    }


}
