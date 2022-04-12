package xyz.junerver.analytics_utils.annotation

/**
 * @Author Junerver
 * @Date 2022/4/7-15:52
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description 追踪事件的注解
 * @param category 事件分类
 * @param action   事件动作
 * @param name     事件名称
 * @param path     路径 (可选)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class TrackEvent(val category: String, val action: String, val name: String, val path: String="/")
