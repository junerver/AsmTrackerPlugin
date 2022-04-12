package xyz.junerver.analytics_plugin.config


object AnnotationReflect {
    //注解字节码名称
    val trackScreenView = "Lxyz/junerver/analytics_utils/annotation/TrackScreenView;"
    val trackEvent = "Lxyz/junerver/analytics_utils/annotation/TrackEvent;"
    val trackDimension = "Lxyz/junerver/analytics_utils/annotation/TrackDimension;"

    val trackAnnotation = arrayOf(trackScreenView, trackEvent, trackDimension)

    //插桩生命周期
    val registerLifecycles = listOf(
        ActivityLifecycle.onCreate,
        ActivityLifecycle.onStart,
        ActivityLifecycle.onResume,
        ActivityLifecycle.onPause,
        ActivityLifecycle.onStop,
        ActivityLifecycle.onDestroy
    )
}