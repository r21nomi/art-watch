package com.nomi.artwatch.di.module

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import com.nomi.artwatch.data.DbOpenHelper
import com.squareup.okhttp.OkHttpClient
import com.squareup.sqlbrite.BriteDatabase
import com.squareup.sqlbrite.SqlBrite
import dagger.Module
import dagger.Provides
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Singleton

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Module
class ApplicationModule(protected var mApplication: Application) {

    @Provides
    @Singleton
    internal fun provideApplication(): Application {
        return mApplication
    }

    @Provides
    @Singleton
    internal fun provideApplicationContext(): Context {
        return mApplication.applicationContext
    }

    @Provides
    @Singleton
    internal fun provideHandler(): Handler {
        return Handler()
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient()
    }

    @Provides
    @Singleton
    internal fun provideOpenHelper(application: Application): SQLiteOpenHelper {
        return DbOpenHelper(application)
    }

    @Provides
    @Singleton
    internal fun provideSqlBrite(): SqlBrite {
        return SqlBrite.create(SqlBrite.Logger { message -> Timber.tag("Database").v(message) })
    }

    @Provides
    @Singleton
    internal fun provideDatabase(sqlBrite: SqlBrite, helper: SQLiteOpenHelper): BriteDatabase {
        val db = sqlBrite.wrapDatabaseHelper(helper, Schedulers.io())
        db.setLoggingEnabled(true)
        return db
    }
}
