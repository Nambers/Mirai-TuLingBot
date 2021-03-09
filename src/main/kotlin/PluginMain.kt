package org.example.mirai.plugin

import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "me.Eritquearcus.TuLingBot",
        name = "TuLingBot",
        version = "1.0.0"
    )
) {
    private var apikey = ""
    private var gkeyWord = ""
    private var fkeyWord = ""
    fun sendJson(out:String):String {
        val url = URL("http://openapi.tuling123.com/openapi/api/v2")
        val con = url.openConnection()
        val http = con as HttpURLConnection
        http.requestMethod = "POST" // PUT is another valid option
        http.setRequestProperty("Content-Type", "application/json; utf-8")
        http.setRequestProperty("Accept", "application/json")
        http.doOutput = true
        http.connect()
        http.outputStream.use { os -> os.write(out.encodeToByteArray()) }
        return InputStreamReader(http.inputStream, StandardCharsets.UTF_8).readText()
    }
    override fun onEnable() {
        //配置文件目录 "${dataFolder.absolutePath}/"
        val file = File("${dataFolder.absolutePath}\\config.json")
        if(!file.exists()){
            logger.error("配置文件不存在(路径:${dataFolder.absolutePath}\\config.json)，无法正常使用本插件")
            return
        }
        val config = file.readText()
        try {
            val configjson = JSONObject(config)
            //api令牌，需要去图灵处注册获得
            apikey = configjson.getString("apikey")
            /*
        触发关键词，如果为空则包含全部情况
         */
            gkeyWord = configjson.getString("gkeyword")
            fkeyWord = configjson.getString("fkeyword")
        }catch (e: JSONException){
            logger.error("config.json参数不全,应该为{\"apikey\":\"这里填从图灵获取的api令牌\",\"gkeyword\":\"这里填群聊内以什么开始触发聊天，如空即为任何时候\",\"fkeyword\":\"这里填私聊内以什么开始触发聊天，如空即为任何时候\"}")
        }
        globalEventChannel().subscribeAlways<GroupMessageEvent>{
            //群消息
            if(gkeyWord != ""&&!this.message.contentToString().startsWith(gkeyWord))return@subscribeAlways
            this.message.forEach {
                if(it is PlainText){
                    //纯文本
                    val text = """
                    {
            	"reqType":0,
                "perception": {
                    "inputText": {
                        "text": "${it.content}"
                    }
                },
                "userInfo": {
                    "apiKey": "$apikey",
                    "userId": "${this.sender.id}",
                    "groupId": "${this.group.id}",
                    "userIdName": "${this.sender.nick}"
                }
            }
                """.trimIndent()
                    val j = sendJson(text)
                    val re = JSONObject(j).getJSONArray("results")[0] as JSONObject
                    this.group.sendMessage(re.getJSONObject("values").getString("text"))
                }
                else if(it is Image){
                    //纯文本
                    val text = """
                    {
            	"reqType":0,
                "perception": {
                    "inputImage": {
                        "url": "${it.queryUrl()}"
                    }
                },
                "userInfo": {
                    "apiKey": "$apikey",
                    "userId": "${this.sender.id}",
                    "groupId": "${this.group.id}",
                    "userIdName": "${this.sender.nick}"
                }
            }
                """.trimIndent()
                    val j = sendJson(text)
                    val re = JSONObject(j).getJSONArray("results")[0] as JSONObject
                    this.group.sendMessage(re.getJSONObject("values").getString("text"))
                }
            }
        }
        globalEventChannel().subscribeAlways<FriendMessageEvent> {
            if(fkeyWord != ""&&!this.message.contentToString().startsWith(fkeyWord))return@subscribeAlways
            this.message.forEach {
                if(it is PlainText){
                    //纯文本
                    val text = """
                    {
            	"reqType":0,
                "perception": {
                    "inputText": {
                        "text": "${it.content}"
                    }
                },
                "userInfo": {
                    "apiKey": "$apikey",
                    "userId": "${this.sender.id}",
                    "userIdName": "${this.sender.nick}"
                }
            }
                """.trimIndent()
                    val j = sendJson(text)
                    val re = JSONObject(j).getJSONArray("results")[0] as JSONObject
                    this.friend.sendMessage(re.getJSONObject("values").getString("text"))
                }
                else if(it is Image){
                    //纯文本
                    val text = """
                    {
            	"reqType":0,
                "perception": {
                    "inputImage": {
                        "url": "${it.queryUrl()}"
                    }
                },
                "userInfo": {
                    "apiKey": "$apikey",
                    "userId": "${this.sender.id}",
                    "userIdName": "${this.sender.nick}"
                }
            }
                """.trimIndent()
                    val j = sendJson(text)
                    val re = JSONObject(j).getJSONArray("results")[0] as JSONObject
                    this.sender.sendMessage(re.getJSONObject("values").getString("text"))
                }
            }
        }
    }
}
