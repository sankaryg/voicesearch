package com.abhi.voicesearch.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.abhi.voicesearch.data.App
import io.reactivex.Observable

/**
 * Data Access Object for the sites table.
 * Inspired from Architecture Components MVVM sample app
 */
@Dao
interface AppsDao {

    @Query("SELECT * FROM apps WHERE (showOnlyVoiceEnabled = :hasKnownOrigin) ORDER BY CASE WHEN `order` in(1,2,3,4,5) THEN -1 END DESC, title COLLATE NOCASE ASC")
    fun getAppsListFlowableFiltered(hasKnownOrigin: Boolean = true): Observable<List<App>>

    @Query("SELECT * FROM apps ORDER BY title COLLATE NOCASE ASC")
    fun getAppsListFlowable(): Observable<List<App>>

    @Query("SELECT * FROM apps where showOnlyVoiceEnabled =  :filterFixedApp ORDER BY CASE WHEN `order` in(1,2,3,4,5) THEN -1 END DESC, title COLLATE NOCASE ASC")
    fun getAppsList(filterFixedApp:Boolean = true): List<App>

    @Query("SELECT * FROM apps where title LIKE '%' || :search || '%'")
    fun getAppsListByTitle(search:String?): List<App>

    /**
     * Insert a app in the database. If the app already exists, replace it.
     *
     * @param app the app to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApp(app: App)

    @Query("SELECT packageName FROM apps WHERE packageName =:packageName LIMIT 1")
    fun getAppString(packageName: String): String?

    @Query("SELECT * FROM apps WHERE packageName =:packageName LIMIT 1")
    fun getApp(packageName: String): App?

    /**
     * Delete all snaps.
     */
    @Query("DELETE FROM apps WHERE packageName = :packageName")
    fun deleteApp(packageName: String)

    /**
     * Delete all snaps.
     */
    @Query("DELETE FROM apps")
    fun deleteTasks()
}
