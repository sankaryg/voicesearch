package com.abhi.voicesearch

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import com.abhi.voicesearch.data.source.local.AppDatabase
import com.abhi.voicesearch.data.source.local.AppsDao
import com.abhi.voicesearch.details.DetailsDialog
import com.abhi.voicesearch.details.Language.LanguageDialog
import com.abhi.voicesearch.main.DatabaseDataSource
import com.abhi.voicesearch.main.MainDataSource
import com.abhi.voicesearch.main.MainFragment
import com.abhi.voicesearch.settings.DialogShowApps
import com.abhi.voicesearch.settings.SettingsFragment
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Named
import javax.inject.Singleton

@AssistedModule
@Module(includes = [com.abhi.voicesearch.AssistedInject_AppModule::class])
abstract class AppModule

@Module
class AppContextModule {

    @Provides
    fun provideContext(application: MainApplication): Context = application.applicationContext

    @Provides
    fun sharedPrefs(application: MainApplication): SharedPreferences {
        return application.getSharedPreferences("workerPreferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun rxPrefs(application: MainApplication): RxkPrefs {
        return rxkPrefs(sharedPrefs(application))
    }
}


@Module
class RxPrefsModule {

    @Provides
    @Named("lightMode")
    fun isLightTheme(rxPrefs: RxkPrefs): Pref<Boolean> {
        return rxPrefs.boolean("lightMode", true)
    }

    @Provides
    @Named("showSystemApps")
    fun showSystemApps(rxPrefs: RxkPrefs): Pref<Boolean> {
        return rxPrefs.boolean("showSystemApps", false)
    }

    @Provides
    @Named("backgroundSync")
    fun backgroundSync(rxPrefs: RxkPrefs): Pref<Boolean> {
        return rxPrefs.boolean("backgroundSync", false)
    }

    @Provides
    @Named("quckStartPackageName")
    fun syncInterval(rxPrefs: RxkPrefs): Pref<String> {
        return rxPrefs.string("quckStartPackageName", "com.apps_Apps")
    }

    @Provides
    @Named("orderBySdk")
    fun orderBySdk(rxPrefs: RxkPrefs): Pref<Boolean> {
        return rxPrefs.boolean("orderBySdk", false)
    }

    @Provides
    @Named("showChoices")
    fun showChoices(rxPrefs: RxkPrefs): Pref<Boolean> {
        return rxPrefs.boolean("showChoices", true)
    }

    @Provides
    @Named("selectedLanguage")
    fun  selectedLanguage(rxPrefs: RxkPrefs): Pref<String>{
        return rxPrefs.string("selectedLanguage", "en-IN")
    }

    @Provides
    @Named("showBackDialog")
    fun  showBackDialog(rxPrefs: RxkPrefs): Pref<Int>{
        return rxPrefs.integer("showBackDialog", 0)
    }

}


@Module
class SnapsRepositoryModule {

    @Singleton
    @Provides
    internal fun provideAppsDao(db: AppDatabase): AppsDao = db.snapsDao()


    @Provides
    fun provideDictRepository(
        appsDao: AppsDao,
        @Named(value = "orderBySdk") orderBySdk: Pref<Boolean>,
        @Named(value = "showSystemApps") showSystemApps: Pref<Boolean>
    ): MainDataSource = DatabaseDataSource(appsDao, orderBySdk, showSystemApps)
}


@Module
class RepositoriesMutualDependenciesModule {

    @Singleton
    @Provides
    internal fun provideDb(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "Apps.db"
        )
            .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
            .build()
    }
}


@Module
abstract class SdkInjectorsModule {

    @ContributesAndroidInjector
    abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun detailsDialog(): DetailsDialog

    @ContributesAndroidInjector
    abstract fun showDialog(): DialogShowApps

    @ContributesAndroidInjector
    abstract fun languageDialog():LanguageDialog

    @ContributesAndroidInjector
    abstract fun settingsFragment(): SettingsFragment

}


@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppContextModule::class,
        AppModule::class,
        RxPrefsModule::class,
        SnapsRepositoryModule::class,
        RepositoriesMutualDependenciesModule::class,
        SdkInjectorsModule::class]
)
@Singleton
interface SingletonComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: MainApplication): Builder

        fun build(): SingletonComponent
    }

    fun inject(app: MainApplication)

    fun appContext(): Context
    fun sharedPrefs(): SharedPreferences
    fun appsDao(): AppsDao

    @Named("lightMode")
    fun isLightTheme(): Pref<Boolean>

    @Named("showSystemApps")
    fun showSystemApps(): Pref<Boolean>

    @Named("backgroundSync")
    fun backgroundSync(): Pref<Boolean>


    @Named("showChoices")
    fun showChoices(): Pref<Boolean>

    @Named("quckStartPackageName")
    fun syncInterval(): Pref<String>

    @Named("selectedLanguage")
    fun selectLanguage(): Pref<String>

    @Named("orderBySdk")
    fun orderBySdk(): Pref<Boolean>

    @Named("showBackDialog")
    fun showBackDialog(): Pref<Int>

    fun dictRepository(): MainDataSource
}

class Injector private constructor() {
    companion object {
        fun get(): SingletonComponent = MainApplication.get().component
    }
}
