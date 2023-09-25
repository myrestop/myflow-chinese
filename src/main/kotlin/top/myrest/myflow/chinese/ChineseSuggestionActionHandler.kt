package top.myrest.myflow.chinese

import cn.hutool.core.util.ArrayUtil
import cn.hutool.core.util.StrUtil
import net.sourceforge.pinyin4j.PinyinHelper
import top.myrest.myflow.AppInfo
import top.myrest.myflow.action.ActionKeywordHandler
import top.myrest.myflow.action.ActionParam
import top.myrest.myflow.action.ActionRequireArgHandler
import top.myrest.myflow.action.ActionResult
import top.myrest.myflow.action.ActionResultSelectionWrapper
import top.myrest.myflow.action.Actions
import top.myrest.myflow.action.asSuggestionResult
import top.myrest.myflow.action.plain
import top.myrest.myflow.action.singleCallback
import top.myrest.myflow.component.Composes
import top.myrest.myflow.constant.AppConsts
import top.myrest.myflow.enumeration.ActionArgMode
import top.myrest.myflow.enumeration.ActionArgType
import top.myrest.myflow.enumeration.ActionMethod
import top.myrest.myflow.enumeration.ActionWindowBehavior
import top.myrest.myflow.enumeration.LanguageType
import top.myrest.myflow.language.LanguageBundle
import top.myrest.myflow.plugin.ParsedPluginInfo
import top.myrest.myflow.plugin.PluginSpecification
import top.myrest.myflow.plugin.Plugins
import top.myrest.myflow.plugin.Plugins.runWithLoader

class ChineseSuggestionActionHandler : ActionRequireArgHandler() {

    override val argRequireMode = ActionArgMode.REQUIRE_NOT_EMPTY to ActionArgType.allTypes

    override fun queryArgAction(param: ActionParam): List<ActionResult> {
        return when (AppInfo.runtimeProps.language) {
            LanguageType.ZH_CN -> suggest(param)
            LanguageType.ZH_TW -> suggest(param)
            else -> emptyList()
        }
    }

    private fun suggest(param: ActionParam): List<ActionResult> {
        val firstArg = param.args.first()
        if (!firstArg.type.isString()) {
            return emptyList()
        }

        val key = firstArg.strValue.lowercase()
        if (key == AppConsts.ANY_KEYWORD || !Actions.isKeywordValid(key)) {
            return emptyList()
        }

        val args = param.args.drop(1)
        return Plugins.listPluginInfo(true).flatMap { p ->
            p.specification.actions.flatMap { k ->
                if (AppInfo.runtimeProps.disabledActionHandlers.contains(k.handler) || k.nameBundleId.isEmpty() || k.getUserKeywords().any { it == AppConsts.ANY_KEYWORD }) {
                    // disabled, or no name bundle id, or has any keyword
                    emptyList<ActionResult>()
                } else {
                    val name = LanguageBundle.getById(p.languageBundle, k.nameBundleId)
                    if (name == k.nameBundleId) {
                        // no language file
                        emptyList<ActionResult>()
                    } else {
                        val pinyin = getPinyin(name)
                        if (pinyin.isEmpty()) {
                            // no chinese character
                            emptyList<ActionResult>()
                        } else {
                            val (match, score) = isMatch(key, name, pinyin)
                            val handler = p.actionHandlerMap[k.handler]
                            if (match && handler != null && handler !is ChineseSuggestionActionHandler) {
                                val result = mapResult(p, k, name, handler, param, k.getUserKeywords().firstOrNull() ?: "", args, score)
                                listOf(result)
                            } else {
                                emptyList<ActionResult>()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mapResult(
        info: ParsedPluginInfo,
        action: PluginSpecification.ActionKeywordProps,
        name: String,
        handler: ActionKeywordHandler,
        param: ActionParam,
        keyword: String,
        args: List<ActionParam.Arg>,
        score: Int,
    ): ActionResult {
        val actionParam = param.copy(keyword = keyword, args = args, limit = 1)
        val actionResults = info.runWithLoader {
            handler.queryAction(actionParam)
        } ?: emptyList()
        return if (actionResults.isEmpty()) {
            val logo = StrUtil.emptyToDefault(action.logo, "logos/suggestion.png")
            ActionResult(
                actionId = "suggestion:$keyword",
                pluginId = info.specification.id,
                logo = logo,
                title = listOf(name.plain),
                subtitle = LanguageBundle.getById(info.languageBundle, action.descriptionBundleId),
                result = name,
                score = score,
                callbacks = singleCallback(
                    result = name,
                    actionWindowBehavior = ActionWindowBehavior.NOTHING,
                    actionMethod = ActionMethod.SET_TO_ACTION_TEXT,
                    showNotify = false,
                ),
            )
        } else {
            var head = actionResults.first()
            head.pluginId = info.specification.id
            head = head.copy(
                logo = Composes.resolveLogo(info.specification.id, action.handler, head.logo).first,
                result = head.result.asSuggestionResult(name + " " + args.joinToString(separator = " ") { g -> g.strValue }),
            )
            ActionResultSelectionWrapper(
                actionId = head.actionId,
                pluginId = info.specification.id,
                logo = head.logo,
                title = listOf(name.plain),
                subtitle = head.subtitle.ifEmpty { head.getTitleString() },
                resultOnSelect = head,
                score = score,
            )
        }
    }

    private fun isMatch(key: String, chinese: String, pinyin: List<String>): Pair<Boolean, Int> {
        var score = 60
        var match = false
        if (chinese.startsWith(key)) {
            score += 30 + key.length - chinese.length
            match = true
        } else if (chinese.contains(key)) {
            score += 20 + key.length - chinese.length
            match = true
        }
        if (!match && key.length > 1) {
            val full = pinyin.joinToString(separator = "")
            if (full.startsWith(key)) {
                score += 30 + key.length - full.length
                match = true
            } else if (full.contains(key)) {
                score += 20 + key.length - full.length
                match = true
            }
        }
        if (!match) {
            val prefix = pinyin.joinToString(separator = "") { it.first().toString() }
            if (prefix.startsWith(key)) {
                score += 40 + (key.length - prefix.length) * 3
                match = true
            }
        }
        return match to score
    }

    private fun getPinyin(str: String): List<String> {
        val list = mutableListOf<String>()
        str.forEach {
            val pinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(it, PinyinActionHandler.format)
            if (ArrayUtil.isNotEmpty(pinyinStringArray)) {
                list.add(pinyinStringArray[0])
            }
        }
        return list
    }
}