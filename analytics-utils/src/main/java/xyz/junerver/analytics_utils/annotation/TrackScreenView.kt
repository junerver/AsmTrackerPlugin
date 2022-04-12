package xyz.junerver.analytics_utils.annotation

/**
 * @Author Junerver
 * @Date 2022/4/7-15:48
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description 跟踪页面访问
 * @Since 1.1
 * @param path 页面路由必填参数
 * @param moduleName 界面所属模块名称
 * @param pageName 界面名称
 * @param onTrack 需要追踪为曝光的函数，一般默认为onCreate
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class TrackScreenView(
    val path: String,
    val moduleName: String = "undefined",
    val pageName: String = "undefined",
    val onTrack:String = "onCreate"
)
