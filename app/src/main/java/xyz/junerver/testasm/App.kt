package xyz.junerver.testasm

import android.app.Application
import android.util.Log
import xyz.junerver.analytics_utils.Analytics
import xyz.junerver.analytics_utils.Analytics.register
import xyz.junerver.analytics_utils.common.IAnalytics

/**
 * @Author Junerver
 * @Date 2022/4/12-11:19
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description
 */
class App: Application() {
    private val TAG = "App注入的函数："
    override fun onCreate() {
        super.onCreate()
        register(object :IAnalytics{
            override fun initConfig(context: Application) {
                Log.d(TAG, "initConfig: ")
            }

            override fun trackEvent(category: String, action: String, name: String, path: String) {
                Log.d(TAG, "trackEvent: $category $action $name $path")
            }

            override fun trackScreenView(moduleName: String, pageName: String, path: String) {
                Log.d(TAG, "trackScreenView: $moduleName $pageName $path")
            }

            override fun trackDimension(dimension: String, value: String, path: String) {
                Log.d(TAG, "trackDimension: $dimension $value $path")
            }
        })
    }
}