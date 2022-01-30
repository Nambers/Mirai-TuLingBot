/*
 * Copyright (C) 2021-2021 Eritque arcus and contributors.
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
        id = "tech.eritquearcus.TuLingBot",
        name = "TuLingBot",
        version = "1.5.0"
    )
) {
    private fun TulingRequest.Perception.toRequest(uinfo: TulingRequest.UserInfo): TulingRequest =
        TulingRequest(
            this,
            when {
                this.inputText != null -> 0
                this.inputImage != null -> 1
                this.inputMedia != null -> 2
                else -> throw IllegalArgumentException("")
            }, uinfo
        )

    private suspend fun SingleMessage.toRequest(uinfo: TulingRequest.UserInfo): TulingRequest =
        when (this) {
            is PlainText -> TulingRequest.Perception(inputText = TulingRequest.Perception.InputText(this.content))
            is Image -> TulingRequest.Perception(inputImage = TulingRequest.Perception.InputImage(this.queryUrl()))
            is OnlineAudio -> TulingRequest.Perception(inputMedia = TulingRequest.Perception.InputMedia(this.urlForDownload))
            else -> throw IllegalArgumentException("")
        }.toRequest(uinfo)

    private fun sendJson(out: String, debug: Boolean?): String {
        val url = URL("http://openapi.tuling123.com/openapi/api/v2")
        val con = url.openConnection()
        val http = con as HttpURLConnection
        http.requestMethod = "POST"
        http.setRequestProperty("Content-Type", "application/json; utf-8")
        http.setRequestProperty("Accept", "application/json")
        http.doOutput = true
        http.connect()
        if (debug == true)
            logger.info("图灵发送:\n$out")
        http.outputStream.use { os -> os.write(out.encodeToByteArray()) }
        val re = InputStreamReader(http.inputStream, StandardCharsets.UTF_8).readText()
        if (debug == true)
            logger.info("图灵返回:\n$re")
        return re
    }

    private fun MessageChain.containKey(l: List<String>, bot: Bot): List<SingleMessage> {
        l.forEach {
            if (it == "@bot" && this.contains(At(bot)))
                return this.toMutableList().apply {
                    this.remove(At(bot))
                    this.removeAt(0)
                }
            else if (this.contentToString().startsWith(it))
                return this.toMutableList().apply {
                    this.removeAt(0)
                    this[0] = PlainText(this[0].contentToString().replace(it, ""))
                }
        }
        return emptyList()
    }

    override fun onEnable() {
        TuringConfig.reload()
        if(apikey.isEmpty())
            logger.warning("未填写apikey，请到${configFolder.absolutePath}/config.yml文件下填写")
        globalEventChannel().filter{ apikey.isNotEmpty() }.subscribeAlways<GroupMessageEvent> {
            //群消息
            val uinfo = TulingRequest.UserInfo(
                apikey,
                this.group.id.toString(),
                this.sender.id.toString(),
                this.senderName
            )
            var reS = ""
            (if (groupKeyword.isEmpty())
                this.message.toList()
            else
                this.message.containKey(groupKeyword, this.bot).let {
                    if (it.isEmpty())
                        return@subscribeAlways
                    else
                        return@let it
                })
                .forEach {
                    if (it.content.isBlank() || it.content.isEmpty())
                        return@forEach
                    val text = Gson().toJson(it.toRequest(uinfo))
                    val j = sendJson(text, debug)
                    val code = JSONObject(j).getJSONObject("intent").getInt("code")
                    reS += if(overLimitReply.isNotEmpty() && code == 4003)
                        overLimitReply.random()
                    else
                        (JSONObject(j).getJSONArray("results")[0] as JSONObject).getJSONObject("values")
                            .getString("text")
                }
            this.group.sendMessage(At(this.sender) + reS)
        }

        globalEventChannel().filter{ apikey.isNotEmpty() }.subscribeAlways<FriendMessageEvent> {
            val uinfo = TulingRequest.UserInfo(
                apikey,
                null,
                this.sender.id.toString(),
                this.sender.nick
            )
            var reS = ""
            (if (friendKeyword.isEmpty())
                this.message.toList()
            else
                this.message.containKey(friendKeyword, this.bot).let {
                    if (it.isEmpty())
                        return@subscribeAlways
                    else
                        return@let it
                })
                .forEach {
                    if (it.content.isBlank() || it.content.isEmpty())
                        return@forEach
                    val text = Gson().toJson(it.toRequest(uinfo))
                    val j = sendJson(text, debug)

                    val code = JSONObject(j).getJSONObject("intent").getInt("code")
                    reS += if(overLimitReply.isNotEmpty() && code == 4003)
                        overLimitReply.random()
                    else
                        (JSONObject(j).getJSONArray("results")[0] as JSONObject).getJSONObject("values")
                            .getString("text")
                }
            this.sender.sendMessage(reS.trim())
        }
    }
}
