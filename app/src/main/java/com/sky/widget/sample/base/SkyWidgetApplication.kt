package com.sky.widget.sample.base

import android.app.Application
import com.sky.widget.iconfont.SkyIconFontsLib

/**
 * @Class: SkyWidgetApplication
 * @Author: Henry
 * @Date: 2026/7/17 08:44
 * @Description: 文件描述
 */
class SkyWidgetApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        SkyIconFontsLib.initRegister(this, listOf(
            "fonts/testSky_iconfont.ttf",
            "fonts/adb_iconfont.ttf"
        ))
    }
}