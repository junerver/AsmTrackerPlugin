package xyz.junerver.analytics_plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AnalyticsPlugin :Plugin<Project> {

    //插件的配置参数
    private var params = AnalyticsExtension()

    override fun apply(project: Project) {
        project.extensions.create(PLUGIN_NAME, AnalyticsExtension::class.java)
        //读取参数
        params = project.extensions.findByType(AnalyticsExtension::class.java)
            ?: AnalyticsExtension()
        //插件注册Transform,固定写法
        project.extensions.getByType(AppExtension::class.java).registerTransform(AnalyticsTransform())
    }
    companion object {
        //用于注册的插件名称
        const val PLUGIN_NAME = "analytics"
    }
}

open class AnalyticsExtension {

}