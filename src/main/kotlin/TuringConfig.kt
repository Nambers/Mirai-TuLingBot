package tech.eritquearcus.tuling

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object TuringConfig:AutoSavePluginConfig("config") {
    @ValueDescription("图灵机器人密钥")
    val apikey:String by value()

    @ValueDescription("唤起关键词（群组）")
    val groupKeyword:List<String> by value()

    @ValueDescription("唤起关键词（私聊）")
    val friendKeyword:List<String> by value()

    @ValueDescription("是否输出debug信息")
    val debug:Boolean by value(false)

    @ValueDescription("请求次数超限后的自定义回复")
    val overLimitReply:List<String> by value()
}