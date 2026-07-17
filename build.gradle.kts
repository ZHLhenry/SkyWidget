buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.android.library) apply false
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.

extra["skyBuild.compileSdk"] = 36
extra["skyBuild.minSdk"] = 24
extra["skyBuild.targetSdk"] = 35
extra["skyBuild.applicationId"] = "com.sky.widget.sample"
extra["skyBuild.appName"] = "SkyWidget"
extra["skyBuild.versionCode"] = 100
extra["skyBuild.versionName"] = "1.0.0"
extra["skyBuild.enableViewBinding"] = true
extra["skyBuild.enableDataBinding"] = true
extra["skyBuild.enableBuildConfig"] = true
extra["skyBuild.enableCompose"] = false