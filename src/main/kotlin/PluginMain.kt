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
import net.mamoe.mirai.message.data.content
import org.json.JSONObject
import tech.eritquearcus.tuling.TuringConfig.apikey
import tech.eritquearcus.tuling.TuringConfig.debug
import tech.eritquearcus.tuling.TuringConfig.friendKeyword
import tech.eritquearcus.tuling.TuringConfig.groupKeyword
import tech.eritquearcus.tuling.TuringConfig.overLimitReply
import java.io.File


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "tech.eritquearcus.TuLingBot", name = "TuLingBot", version = "1.6.0"
    )
) {

    private suspend fun MessageEvent.getResult(keyWords: List<String>) {
        var reS = ""
        val uinfo = TulingRequest.UserInfo(
            apikey, null, this.sender.id.toString(), this.sender.nick
        )
        run out@{
            (if (keyWords.isEmpty()) this.message.toList()
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

    private fun move(file: File) {
        val old = Gson().fromJson(file.readText(), Config::class.java)
        apikey = old.apikey
        groupKeyword = old.gkeyWord
        friendKeyword = old.fkeyWord
        debug = old.debug == true
        file.delete()
    }

    override fun onEnable() {
        File(this.dataFolder.absoluteFile, "config.json").let {
            if (it.exists())
                move(it)
        }
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
