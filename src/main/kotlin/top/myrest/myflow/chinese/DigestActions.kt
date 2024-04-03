package top.myrest.myflow.chinese

import java.io.File
import cn.hutool.core.util.ArrayUtil
import cn.hutool.core.util.StrUtil
import com.github.houbb.opencc4j.util.ZhConverterUtil
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import top.myrest.myflow.action.ActionResult
import top.myrest.myflow.action.BaseDigestActionHandler
import top.myrest.myflow.action.asSaveFileResult
import top.myrest.myflow.action.basicCopyResult
import top.myrest.myflow.enumeration.LanguageType
import top.myrest.myflow.language.Translator
import top.myrest.myflow.util.FileUtils

class PinyinActionHandler : BaseDigestActionHandler() {

    private val format = HanyuPinyinOutputFormat()

    init {
        format.caseType = HanyuPinyinCaseType.LOWERCASE
        format.toneType = HanyuPinyinToneType.WITH_TONE_MARK
        format.vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    private fun getPinyin(str: String): String {
        val result = StrUtil.strBuilder()
        var isFirst = true
        val strLen = str.length
        for (i in 0..<strLen) {
            if (isFirst) {
                isFirst = false
            } else {
                result.append(" ")
            }
            val pinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(str[i], format)
            if (ArrayUtil.isEmpty(pinyinStringArray)) {
                result.append(str[i])
            } else {
                result.append(pinyinStringArray[0])
            }
        }
        return result.toString()
    }

    override fun queryDigestAction(content: String) = basicCopyResult(actionId = "pinyin", logo = "./logos/pinyin.png", result = getPinyin(content))

    override fun queryFileDigestAction(file: File): ActionResult = basicCopyResult(actionId = "pinyin", logo = "./logos/pinyin.png", result = getPinyin(FileUtils.readByFileCharset(file)).asSaveFileResult())
}

class ToTraditionalChinese : BaseDigestActionHandler() {
    override fun queryDigestAction(content: String): ActionResult = basicCopyResult(actionId = "tradition", logo = "./logos/traditional.jpg", result = ZhConverterUtil.toTraditional(content))
    override fun queryFileDigestAction(file: File): ActionResult = basicCopyResult(actionId = "tradition", logo = "./logos/traditional.jpg", result = ZhConverterUtil.toTraditional(FileUtils.readByFileCharset(file)).asSaveFileResult())
}

class ToSimplifiedChinese : BaseDigestActionHandler() {
    override fun queryDigestAction(content: String): ActionResult = basicCopyResult(actionId = "simple", logo = "./logos/simplified.jpg", result = ZhConverterUtil.toSimple(content))
    override fun queryFileDigestAction(file: File): ActionResult = basicCopyResult(actionId = "simple", logo = "./logos/simplified.jpg", result = ZhConverterUtil.toSimple(FileUtils.readByFileCharset(file)).asSaveFileResult())
}

class ChineseTranslator : Translator {

    private val types = listOf(LanguageType.ZH_CN, LanguageType.ZH_TW)

    override fun getSupportLanguages(): List<LanguageType> = types

    override fun translate(text: String, sourceLanguage: LanguageType, targetLanguage: LanguageType): String {
        if (text.isBlank()) {
            return text
        }
        if (sourceLanguage == LanguageType.ZH_CN && targetLanguage == LanguageType.ZH_TW) {
            return ZhConverterUtil.toTraditional(text)
        }
        if (sourceLanguage == LanguageType.ZH_TW && targetLanguage == LanguageType.ZH_CN) {
            return ZhConverterUtil.toSimple(text)
        }
        return text
    }
}
