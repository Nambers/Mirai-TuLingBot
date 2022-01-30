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
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.json.JSONObject
import tech.eritquearcus.tuling.TuringConfig.apikey
import tech.eritquearcus.tuling.TuringConfig.debug
import tech.eritquearcus.tuling.TuringConfig.friendKeyword
import tech.eritquearcus.tuling.TuringConfig.groupKeyword
import tech.eritquearcus.tuling.TuringConfig.overLimitReply
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "tech.eritquearcus.TuLingBot", name = "TuLingBot", version = "1.6.0"
    )
) {
    private fun TulingRequest.Perception?.toRequest(uinfo: TulingRequest.UserInfo): TulingRequest? =
        if (this == null) null
        else TulingRequest(
            this, when {
                this.inputText != null -> 0
                this.inputImage != null -> 1
                this.inputMedia != null -> 2
                else -> throw IllegalArgumentException("Unreachable")
            }, uinfo
        )

    private suspend fun SingleMessage.toRequest(uinfo: TulingRequest.UserInfo): TulingRequest? = when (this) {
        is PlainText -> TulingRequest.Perception(inputText = TulingRequest.Perception.InputText(this.content))
        is Image -> TulingRequest.Perception(inputImage = TulingRequest.Perception.InputImage(this.queryUrl()))
        is OnlineAudio -> TulingRequest.Perception(inputMedia = TulingRequest.Perception.InputMedia(this.urlForDownload))
        else -> null
    }.toRequest(uinfo)

    private fun sendJson(out: String, debug: Boolean?): String {
        val url = URL("https://openapi.tuling123.com/openapi/api/v2")
        val con = url.openConnection()
        val http = con as HttpURLConnection
        http.requestMethod = "POST"
        http.setRequestProperty("Content-Type", "application/json; utf-8")
        http.setRequestProperty("Accept", "application/json")
        http.doOutput = true
        http.connect()
        if (debug == true) logger.info("图灵发送:\n$out")
        http.outputStream.use { os -> os.write(out.encodeToByteArray()) }
        val re = InputStreamReader(http.inputStream, StandardCharsets.UTF_8).readText()
        if (debug == true) logger.info("图灵返回:\n$re")
        return re
    }

    private fun MessageChain.containKey(l: List<String>, bot: Bot): List<SingleMessage> {
        l.forEach {
            if (it == "@bot" && this.contains(At(bot))) return this.toMutableList().apply {
                this.remove(At(bot))
                this.removeAt(0)
            }
            else if (this.contentToString().startsWith(it)) return this.toMutableList().apply {
                this.removeAt(0)
                this[0] = PlainText(this[0].contentToString().replace(it, ""))
            }
        }
        return emptyList()
    }

    private suspend fun MessageEvent.getResult(keyWords: List<String>) {
        var reS = ""
        val uinfo = TulingRequest.UserInfo(
            apikey, null, this.sender.id.toString(), this.sender.nick
        )
        run out@{
            (if (groupKeyword.isEmpty()) this.message.toList()
            else this.message.containKey(keyWords, this.bot).let {
                if (it.isEmpty()) return
                else return@let it
            }).forEach {
                if (it.content.isBlank() || it.content.isEmpty()) return@forEach
                val text = Gson().toJson(it.toRequest(uinfo).apply {
                    if (this == null) {
                        logger.warning("遇到不能处理的消息类型: ${it.javaClass.name}")
                        return@forEach // equal to `continue`
                    }
                })
                val j = sendJson(text, debug)
                val code = JSONObject(j).getJSONObject("intent").getInt("code")
                if (code.toString() in errCode.keys) {
                    logger.error("图灵服务返回异常: code: $code, msg: ${errCode[code.toString()]}")
                    reS = if (overLimitReply.isNotEmpty()) overLimitReply.random()
                    else errCode[code.toString()]!!
                    return@out // equal to `break`
                } else reS += (JSONObject(j).getJSONArray("results")[0] as JSONObject).getJSONObject("values")
                    .getString("text")
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
