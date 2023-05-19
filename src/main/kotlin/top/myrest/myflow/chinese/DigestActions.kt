package top.myrest.myflow.chinese

import cn.hutool.extra.pinyin.PinyinUtil
import com.github.houbb.opencc4j.util.ZhConverterUtil
import top.myrest.myflow.action.ActionResult
import top.myrest.myflow.action.BaseDigestActionHandler
import top.myrest.myflow.action.basicCopyResult

class PinyinActionHandler : BaseDigestActionHandler() {
    override fun queryDigestAction(content: String) = basicCopyResult(logo = "./logos/pinyin.png", result = PinyinUtil.getPinyin(content))
}

class ToTraditionalChinese : BaseDigestActionHandler() {
    override fun queryDigestAction(content: String): ActionResult = basicCopyResult(logo = "./logos/traditional.jpg", result = ZhConverterUtil.toTraditional(content))
}

class ToSimplifiedChinese : BaseDigestActionHandler() {
    override fun queryDigestAction(content: String): ActionResult = basicCopyResult(logo = "./logos/simplified.jpg", result = ZhConverterUtil.toSimple(content))
}
