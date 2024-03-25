package top.myrest.myflow.chinese

import cn.hutool.core.util.ArrayUtil
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import top.myrest.myflow.AppInfo
import top.myrest.myflow.action.ActionResultTitle
import top.myrest.myflow.action.Actions
import top.myrest.myflow.action.highlight
import top.myrest.myflow.action.plain
import top.myrest.myflow.language.LocalizationMatchResult
import top.myrest.myflow.language.LocalizationMatcher
import top.myrest.myflow.language.LocalizedMatching

class ChineseLocalizationMatcher : LocalizationMatcher {

    private val cacheSize = 256

    private val format = HanyuPinyinOutputFormat()

    init {
        format.caseType = HanyuPinyinCaseType.LOWERCASE
        format.toneType = HanyuPinyinToneType.WITHOUT_TONE
        format.vCharType = HanyuPinyinVCharType.WITH_V
    }

    private val pinyinCache = object : LinkedHashMap<String, List<String>>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<String>>): Boolean {
            return size > cacheSize
        }
    }

    override fun isSupport(content: String): Boolean {
        return AppInfo.runtimeProps.language.isChinese() && getPinyin(content).isNotEmpty()
    }

    override fun match(keyword: String, content: String): LocalizationMatchResult {
        if (keyword.isBlank() || content.isBlank() || !Actions.isKeywordValid(keyword)) {
            return LocalizedMatching.notMatched
        }

        var score = 60
        var match = false
        var titles = listOf<ActionResultTitle>()

        var idx = content.indexOf(keyword)
        if (idx == 0) {
            titles = listOf(keyword.highlight, content.substring(keyword.length).plain)
            score += 30 + keyword.length - content.length
            match = true
        } else if (idx > 0) {
            titles = listOf(content.substring(0, idx).plain, keyword.highlight, content.substring(idx + keyword.length).plain)
            score += 20 + keyword.length - content.length
            match = true
        }

        val pinyin = getPinyin(content)
        val withoutSpace = keyword.filter { !it.isWhitespace() }.lowercase()
        var len = withoutSpace.length
        if (!match && len > 1) {
            val full = pinyin.joinToString(separator = "")
            idx = full.indexOf(withoutSpace)
            if (idx == 0) {
                for ((i, s) in pinyin.withIndex()) {
                    if (len <= 0) {
                        break
                    }
                    idx = i
                    len -= s.length
                }
                titles = listOf(content.substring(0, idx + 1).highlight, content.substring(idx + 1).plain)
                score += 30 + keyword.length - full.length
                match = true
            } else if (idx > 0) {
                var acc = 0
                var start = 0
                var end = content.lastIndex
                val stop = idx + len
                for ((i, s) in pinyin.withIndex()) {
                    acc += s.length
                    if (acc > idx && start == 0) {
                        start = i
                    }
                    if (acc >= stop) {
                        end = i
                        break
                    }
                }
                titles = listOf(content.substring(0, start).plain, content.substring(start, end + 1).highlight, content.substring(end + 1).plain)
                score += 20 + keyword.length - full.length
                match = true
            }
        }

        if (!match) {
            val prefix = pinyin.joinToString(separator = "") { it.first().toString() }
            idx = prefix.indexOf(keyword)
            if (idx == 0) {
                titles = listOf(content.substring(0, keyword.length).highlight, content.substring(keyword.length).plain)
                score += 40 + (keyword.length - prefix.length) * 3
                match = true
            } else if (idx > 0) {
                titles = listOf(content.substring(0, idx).plain, content.substring(idx, idx + keyword.length).highlight, content.substring(idx + keyword.length).plain)
                score += 20 + (keyword.length - prefix.length) * 3
                match = true
            }
        }
        if (match) {
            return LocalizationMatchResult(true, titles.filter { it.value.isNotEmpty() }, score)
        }
        return LocalizedMatching.notMatched
    }

    private fun getPinyin(str: String): List<String> {
        return pinyinCache.computeIfAbsent(str) {
            val list = mutableListOf<String>()
            it.forEach { c ->
                val pinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(c, format)
                if (ArrayUtil.isNotEmpty(pinyinStringArray)) {
                    list.add(pinyinStringArray[0])
                }
            }
            list
        }
    }
}
