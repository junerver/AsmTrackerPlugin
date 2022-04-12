package xyz.junerver.analytics_utils.annotation

/**
 * @Author Junerver
 * @Date 2022/4/7-15:59
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description 追踪自定义维度
 * @param dimension 维度
 * @param value     值
 * @param path      路径
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class TrackDimension(val dimension: String, val value: String, val path: String="/")
