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
        // 调用初始化函数，传递application实例
        initConfig(this)
    }

    //注册实现类
    fun register(iAnalytics: IAnalytics, application: Application) {
        mAnalytics = iAnalytics
        initConfig(application)
    }

    override fun initConfig(context: Application) {
        // 有的埋点上报工具可能需要context进行初始化，我们可以在这个接口函数中来初始化
        mAnalytics?.initConfig(context)
    }

    override fun trackEvent(category: String, action: String, name: String, path: String) {
        // 插桩埋点时执行的是这个↑函数，这个函数又通过接口，
        // 调用了真正的埋点业务函数，这里看起来很乱其实是为了统一函数名称
        // 这个object中的函数才是最终字节码真正调用的
        mAnalytics?.trackEvent(category, action, name, path)
    }

    override fun trackScreenView(path: String, moduleName: String, pageName: String) {
        mAnalytics?.trackScreenView(path, moduleName, pageName)
    }

    override fun trackDimension(dimension: String, value: String, path: String) {
        mAnalytics?.trackDimension(dimension, value, path)
    }
}