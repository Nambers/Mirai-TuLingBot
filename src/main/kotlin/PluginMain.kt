/*
 * Copyright (C) 2021-2022 Eritque arcus and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version(in your opinion).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package tech.eritquearcus.tuling

import com.google.gson.Gson
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import org.json.JSONObject
import tech.eritquearcus.tuling.TuringConfig.apikey
import tech.eritquearcus.tuling.TuringConfig.debug
import tech.eritquearcus.tuling.TuringConfig.friendKeyword
import tech.eritquearcus.tuling.TuringConfig.groupKeyword
import tech.eritquearcus.tuling.TuringConfig.overLimitReply


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "tech.eritquearcus.TuLingBot", name = "TuLingBot", version = "1.7.0"
    )
) {

    private suspend fun SingleMessage.getResult(uinfo: TulingRequest.UserInfo): Pair<String, Int> {
//        if (content.isBlank() || content.isEmpty()) return Pair("", 1)
        val text = Gson().toJson(toRequest(uinfo).apply {
            if (this == null) {
                logger.warning("遇到不能处理的消息类型: ${javaClass.name}")
                return Pair("", 0) // equal to `continue`
            }
        })
        val j = sendJson(text, debug)
        val code = JSONObject(j).getJSONObject("intent").getInt("code")
        if (code.toString() in errCode.keys) {
            if (code.toString() in apikeyErr) {
                logger.warning("图灵服务器返回apikey异常: code: $code, msg: ${errCode[code.toString()]}, apikey: ${uinfo.apiKey}, 尝试下一个key")
                return Pair("", 2) // change apikey
            }
            logger.error("图灵服务返回异常: code: $code, msg: ${errCode[code.toString()]}, apikey: ${uinfo.apiKey}")
            val re = if (overLimitReply.isNotEmpty()) overLimitReply.random() else "Err: " + errCode[code.toString()]!!
            return Pair(re, 1) // equal to `break`
        } else {
            val re = (JSONObject(j).getJSONArray("results")[0] as JSONObject).getJSONObject("values").getString("text")
            return Pair(re, 0)
        }
    }

    private suspend fun MessageEvent.getResult(keyWords: List<String>) {
        var reS = ""
        run out@{
            var msgs = (if (keyWords.isEmpty()) this.message.toList()
            else this.message.containKey(keyWords, this.bot).let {
                if (it == null) return
                else return@let it
            })
            if (msgs.isEmpty()) msgs = listOf(PlainText(""))
            msgs.forEach foreach1@{ msg ->
                var backup = ""
                apikey.forEach { key ->
                    val uinfo = TulingRequest.UserInfo(
                        key, null, this.sender.id.toString(), this.sender.nick
                    )
                    msg.getResult(uinfo).let {
                        when (it.second) {
                            0 -> { // continue
                                reS += it.first
                                return@foreach1
                            }
                            1 -> { // break
                                reS = it.first
                                return@out
                            }
                            2 -> backup = it.first // change apikey
                        }
                    }
                }
                // unreachable
                reS = backup
            }
        }
        this.subject.sendMessage((if (this is GroupMessageEvent) At(this.sender) else PlainText("")) + reS.trim())
    }

    override fun onEnable() {
        TuringConfig.reload()
        if (apikey.isEmpty()) logger.warning("未填写apikey，请到${configFolder.absoluteFile.resolve("config.yml")}文件下填写")
        globalEventChannel().filter { apikey.isNotEmpty() }.subscribeAlways<GroupMessageEvent> {
            this.getResult(groupKeyword)
        }
        globalEventChannel().filter { apikey.isNotEmpty() }.subscribeAlways<FriendMessageEvent> {
            this.getResult(friendKeyword)
        }
    }
}
