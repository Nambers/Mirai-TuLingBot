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
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "tech.eritquearcus.TuLingBot",
        name = "TuLingBot",
        version = "1.4.0"
    )
) {
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
        //配置文件目录 "${dataFolder.absolutePath}/"
        val configuration: config
        val file = File(dataFolder.absolutePath, "config.json")
        logger.info("配置文件目录 \"${dataFolder.absolutePath}\"")
        val gson = Gson()
        if (!file.exists()) {
            logger.error("配置文件不存在(路径:${file.absolutePath})，无法正常使用本插件")
            file.createNewFile()
            file.writeText(gson.toJson(config("", listOf(""), listOf(""), null)))
            return
        }
        try {
            configuration = gson.fromJson(file.readText(), config::class.java)
        } catch (e: com.google.gson.JsonSyntaxException) {
            logger.error(e)
            logger.error("config.json参数不全,应该为{\"apikey\":\"这里填从图灵获取的api令牌\",\"gkeyword\":\"这里填群聊内以什么开始触发聊天，如空即为任何时候\",\"fkeyword\":\"这里填私聊内以什么开始触发聊天，如空即为任何时候\"}")
            return
        }
        logger.info("群触发关键词为:${configuration.gkeyWord}")
        logger.info("私聊触发关键词为:${configuration.fkeyWord}")
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            //群消息
            var reS = ""
            (if (configuration.gkeyWord.isEmpty())
                this.message.toList()
            else
                this.message.containKey(configuration.gkeyWord, this.bot).let {
                    if (it.isEmpty())
                        return@subscribeAlways
                    else
                        return@let it
                })
                .forEach {
                    if (it.content == "")
                        return@forEach
                    val text = when {
                        (it is PlainText) -> {
                            //纯文本
                            """
                    {
            	"reqType":0,
                "perception": {
                    "inputText": {
                        "text": "${it.content}"
                    }
                },
                "userInfo": {
                    "apiKey": "${configuration.apikey}",
                    "userId": "${this.sender.id}",
                    "groupId": "${this.group.id}",
                    "userIdName": "${this.sender.nick}"
                }
            }
                """.trimIndent()
                        }
                        (it is Image) -> {
                            """
                            {
                        "reqType":1,
                        "perception": {
                            "inputImage": {
                                "url": "${it.queryUrl()}"
                            }
                        },
                        "userInfo": {
                            "apiKey": "${configuration.apikey}",
                            "userId": "${this.sender.id}",
                            "groupId": "${this.group.id}",
                            "userIdName": "${this.sender.nick}"
                        }
                    }
                """.trimIndent()
                        }
                        (it is OnlineAudio) -> {
                            """
                            {
                        "reqType":2,
                        "perception": {
                            "inputMedia": {
                                "url": "${it.urlForDownload}"
                            }
                        },
                        "userInfo": {
                            "apiKey": "${configuration.apikey}",
                            "userId": "${this.sender.id}",
                            "groupId": "${this.group.id}",
                            "userIdName": "${this.sender.nick}"
                        }
                    }
                """.trimIndent()
                        }
                        else -> {
                            return@forEach
                        }
                    }
                    val j = sendJson(text, configuration.debug)
                    if (configuration.debug == true)
                        logger.info(j)
                    reS += (JSONObject(j).getJSONArray("results")[0] as JSONObject).getJSONObject("values")
                        .getString("text")
                }
            this.group.sendMessage(At(this.sender) + reS)
        }
        globalEventChannel().subscribeAlways<FriendMessageEvent> {
            var reS = ""
            (if (configuration.fkeyWord.isEmpty())
                this.message.toList()
            else
                this.message.containKey(configuration.fkeyWord, this.bot).let {
                    if (it.isEmpty())
                        return@subscribeAlways
                    else
                        return@let it
                })
                .forEach {
                    if (it.content == "")
                        return@forEach
                    val text = when {
                        (it is PlainText) -> {
                            """
                    {
            	"reqType":0,
                "perception": {
                    "inputText": {
                        "text": "${it.content.replace("\n", "")}"
                    }
                },
                "userInfo": {
                    "apiKey": "${configuration.apikey}",
                    "userId": "${this.sender.id}",
                    "userIdName": "${this.sender.nick}"
                }
            }
                """.trimIndent()
                        }
                        (it is Image) -> {
                            //纯文本
                            """
                        {
                        "reqType":1,
                        "perception": {
                            "inputImage": {
                                "url": "${it.queryUrl()}"
                            }
                        },
                        "userInfo": {
                            "apiKey": "${configuration.apikey}",
                            "userId": "${this.sender.id}",
                            "userIdName": "${this.sender.nick}"
                        }
                    }
                        """.trimIndent()
                        }
                        (it is OnlineAudio) -> {
                            """
                            {
                        "reqType":2,
                        "perception": {
                            "inputMedia": {
                                "url": "${it.urlForDownload}"
                            }
                        },
                        "userInfo": {
                            "apiKey": "${configuration.apikey}",
                            "userId": "${this.sender.id}",
                            "userIdName": "${this.sender.nick}"
                        }
                    }
                """.trimIndent()
                        }
                        else -> {
                            return@forEach
                        }
                    }
                    val j = sendJson(text, configuration.debug)
                    if (configuration.debug == true)
                        logger.info(j)
                    reS += (JSONObject(j).getJSONArray("results")[0] as JSONObject).getJSONObject("values")
                        .getString("text") + "\n"
                }
            this.sender.sendMessage(reS.trim())
        }
    }
}
