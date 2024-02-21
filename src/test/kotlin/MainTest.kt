import com.fasterxml.jackson.databind.DeserializationFeature
import top.myrest.myflow.AppInfo
import top.myrest.myflow.baseimpl.App
import top.myrest.myflow.baseimpl.FlowApp
import top.myrest.myflow.baseimpl.enableDevEnv
import top.myrest.myflow.dev.DevProps
import top.myrest.myflow.util.Jackson

fun main() {
    enableDevEnv()
    DevProps.disableNativeListener = true
    DevProps.compiledClassDir = "D:\\MyProjects\\myflow-chinese\\build\\classes\\kotlin\\main"
    Jackson.yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    FlowApp().configApp()
    App(AppInfo.APP_NAME + "Chinese")
}