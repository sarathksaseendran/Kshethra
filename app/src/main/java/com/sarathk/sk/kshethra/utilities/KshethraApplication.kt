package com.sarathk.sk.kshethra.utilities

import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication

class KshethraApplication : MultiDexApplication() {

    init { instance = this }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //val configuration = Configuration(newConfig)
        //adjustFontScale(applicationContext, configuration)
    }

    companion object {
        var instance: KshethraApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

//    private fun adjustFontScale(context: Context, configuration: Configuration) {
//        if (configuration.fontScale != 1f) {
//            configuration.fontScale = 1f
//            val metrics = context.resources.displayMetrics
//            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            wm.defaultDisplay.getMetrics(metrics)
//            metrics.scaledDensity = configuration.fontScale * metrics.density
//            context.resources.updateConfiguration(configuration, metrics)
//        }
//    }

}