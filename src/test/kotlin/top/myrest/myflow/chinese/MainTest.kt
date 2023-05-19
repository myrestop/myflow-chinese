package top.myrest.myflow.chinese

import top.myrest.myflow.AppInfo
import top.myrest.myflow.baseimpl.App
import top.myrest.myflow.baseimpl.FlowApp
import top.myrest.myflow.baseimpl.enableDevEnv

fun main() {
    enableDevEnv()
    FlowApp().configApp()
    App(AppInfo.appName + "Chinese")
}