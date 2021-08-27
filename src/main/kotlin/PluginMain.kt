package tech.eritquearcus.tuling

import com.google.gson.Gson
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
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
        version = "1.3.0"
    )
) {
    private fun sendJson(out: String, debug: Boolean?): String {
        val url = URL("http://openapi.tuling123.com/openapi/api/v2")
        val con = url.openConnection()
        val http = con as HttpURLConnection
        http.requestMethod = "POST" // PUT is another valid option
        http.setRequestProperty("Content-Type", "application/json; utf-8")
        http.setRequestProperty("Accept", "application/json")
        http.doOutput = true
        http.connect()
        http.outputStream.use { os -> os.write(out.encodeToByteArray()) }
        val re = InputStreamReader(http.inputStream, StandardCharsets.UTF_8).readText()
        if (debug == true)
            logger.info("图灵返回:\n $re")
        return re
    }

    private fun String.dontContain(l: List<String>, bot: Bot): Boolean {
        l.forEach {
            if (this.startsWith(if (it == "@bot") At(bot).contentToString() else it))
                return false
        }
        return true
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
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            //群消息
            if (configuration.gkeyWord.isNotEmpty() && this.message.contentToString()
                    .dontContain(configuration.gkeyWord, this.bot)
            ) return@subscribeAlways
            this.message.forEach {
                var text = "null"
                if (it is PlainText) {
                    //纯文本
                    text = """
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
                } else if (it is Image) {
                    //纯文本
                    text = """
                    {
            	"reqType":0,
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
                val j = sendJson(text, configuration.debug)
                if (configuration.debug == true)
                    logger.info(j)
                val re = JSONObject(j).getJSONArray("results")[0] as JSONObject
                this.group.sendMessage(re.getJSONObject("values").getString("text"))
            }
        }
        globalEventChannel().subscribeAlways<FriendMessageEvent> {
            if (configuration.fkeyWord.isNotEmpty() && this.message.contentToString()
                    .dontContain(configuration.fkeyWord, this.bot)
            ) return@subscribeAlways
            this.message.forEach {
                var text = ""
                if(it is PlainText){
                    text = """
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
                    "userIdName": "${this.sender.nick}"
                }
            }
                """.trimIndent()
                }
                else if(it is Image){
                    //纯文本
                    text = """
                    {
            	"reqType":0,
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
                val j = sendJson(text, configuration.debug)
                if (configuration.debug == true)
                    logger.info(j)
                val re = JSONObject(j).getJSONArray("results")[0] as JSONObject
                this.sender.sendMessage(re.getJSONObject("values").getString("text"))
            }
        }
    }
}
