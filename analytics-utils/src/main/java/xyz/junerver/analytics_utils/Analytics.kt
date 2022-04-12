package xyz.junerver.analytics_utils

import android.app.Application
import xyz.junerver.analytics_utils.common.IAnalytics

/**
 * @Author Junerver
 * @Date 2022/4/12-11:12
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description Analytics实现类，注入时直接注入该函数，具体业务逻辑由用户自行编写
 */
object Analytics : IAnalytics {
    private var mAnalytics: IAnalytics? = null

    //注册实现类
    fun Application.register(iAnalytics: IAnalytics) {
        mAnalytics = iAnalytics
        initConfig(this)
    }

    //注册实现类
    fun register(iAnalytics: IAnalytics, application: Application) {
        mAnalytics = iAnalytics
        initConfig(application)
    }

    override fun initConfig(context: Application) {
        mAnalytics?.initConfig(context)
    }

    override fun trackEvent(category: String, action: String, name: String, path: String) {
        mAnalytics?.trackEvent(category, action, name, path)
    }

    override fun trackScreenView(path: String, moduleName: String, pageName: String) {
        mAnalytics?.trackScreenView(path, moduleName, pageName)
    }

    override fun trackDimension(dimension: String, value: String, path: String) {
        mAnalytics?.trackDimension(dimension, value, path)
    }
}