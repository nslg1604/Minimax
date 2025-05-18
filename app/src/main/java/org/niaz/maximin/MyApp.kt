package org.niaz.maximin

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.niaz.maximin.utils.MyDebugTree
import timber.log.Timber

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        myApp = this
        Timber.plant(MyDebugTree())
        Timber.d("MyApp - onCreate")
    }


//    companion object {
//        lateinit var myApp: MyApp
//        fun getInstance(): MyApp {
//            return myApp
//        }
//    }
}