package xyz.junerver.analytics_utils.common

import android.app.Application

/**
 * @Author Junerver
 * @Date 2022/4/12-10:56
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description
 */
interface IAnalytics {

    /**
     * 初始化
     *
     */
    fun initConfig(context: Application)
    /**
     * @Description 追踪事件
     * @Author Junerver
     * Created at 2022/4/12 11:11
     * @param
     * @return
     */
    fun trackEvent(category: String, action: String, name: String, path: String = "/")

    /**
     * @Description 追踪屏幕曝光
     * @Author Junerver
     * Created at 2022/4/12 11:11
     * @param
     * @return
     */
    fun trackScreenView(
        path: String,
        moduleName: String = "undefined",
        pageName: String = "undefined"
    )

    /**
     * @Description 追踪自定义维度
     * @Author Junerver
     * Created at 2022/4/12 11:11
     * @param
     * @return
     */
    fun trackDimension(dimension: String, value: String, path: String = "/")
}