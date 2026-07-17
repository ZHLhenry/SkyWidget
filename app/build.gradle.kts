plugins {
    alias(libs.plugins.sky.android.application)
    alias(libs.plugins.sky.android.application.flavors)
    alias(libs.plugins.sky.android.hilt)
}

android {
    namespace = "com.sky.widget.sample"
    flavorDimensions += "contentType"
    productFlavors {
        create("dev") {
            dimension = "contentType"
            manifestPlaceholders["app_icon"] = "@mipmap/ic_launcher"
        }
        create("uat") {
            dimension = "contentType"
            manifestPlaceholders["app_icon"] = "@mipmap/ic_launcher"
        }
        create("prod") {
            dimension = "contentType"
            manifestPlaceholders["app_icon"] = "@mipmap/ic_launcher"
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    api(libs.skymvvm)
    api(libs.skywidget)
//    api(project(":SkyWidgetLib"))
    // hilt-noop-processor(编译消除警告)
    annotationProcessor(libs.hilt.noop.processor)
}