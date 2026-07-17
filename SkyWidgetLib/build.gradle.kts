plugins {
    alias(libs.plugins.sky.android.library)
    alias(libs.plugins.sky.android.publish)
}

android {
    namespace = "com.sky.widget"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.iconics.core)
}
