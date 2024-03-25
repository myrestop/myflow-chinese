import top.myrest.myflow.chinese.ChineseLocalizationMatcher
import top.myrest.myflow.util.Jackson.toJsonString

fun main() {
    val content = "中文匹配"
    val matcher = ChineseLocalizationMatcher()

    println(matcher.match("中文", content).toJsonString())
    println(matcher.match("文匹", content).toJsonString())
    println(matcher.match("匹配", content).toJsonString())
    println(matcher.match("中文匹配", content).toJsonString())

    println()
    println(matcher.match("zhongwen", content).toJsonString())
    println(matcher.match("wenpi", content).toJsonString())
    println(matcher.match("pipei", content).toJsonString())
    println(matcher.match("zhongw", content).toJsonString())
    println(matcher.match("wenp", content).toJsonString())
    println(matcher.match("ipei", content).toJsonString())
    println(matcher.match("zhongwenpipei", content).toJsonString())

    println()
    println(matcher.match("z", content).toJsonString())
    println(matcher.match("w", content).toJsonString())
    println(matcher.match("p", content).toJsonString())
    println(matcher.match("zw", content).toJsonString())
    println(matcher.match("wp", content).toJsonString())
    println(matcher.match("pp", content).toJsonString())
    println(matcher.match("zwpp", content).toJsonString())
}