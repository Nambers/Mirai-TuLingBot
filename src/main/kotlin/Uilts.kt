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

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

internal fun TulingRequest.Perception?.toRequest(uinfo: TulingRequest.UserInfo): TulingRequest? =
    if (this == null) null
    else TulingRequest(
        this, when {
            this.inputText != null -> 0
            this.inputImage != null -> 1
            this.inputMedia != null -> 2
            else -> throw IllegalArgumentException("Unreachable")
        }, uinfo
    )

internal suspend fun SingleMessage.toRequest(uinfo: TulingRequest.UserInfo): TulingRequest? = when (this) {
    is PlainText -> TulingRequest.Perception(inputText = TulingRequest.Perception.InputText(this.content))
    is Image -> TulingRequest.Perception(inputImage = TulingRequest.Perception.InputImage(this.queryUrl()))
    is OnlineAudio -> TulingRequest.Perception(inputMedia = TulingRequest.Perception.InputMedia(this.urlForDownload))
    else -> null
}.toRequest(uinfo)

internal fun sendJson(out: String, debug: Boolean?): String {
    val url = URL("https://openapi.tuling123.com/openapi/api/v2")
    val con = url.openConnection()
    val http = con as HttpURLConnection
    http.requestMethod = "POST"
    http.setRequestProperty("Content-Type", "application/json; utf-8")
    http.setRequestProperty("Accept", "application/json")
    http.doOutput = true
    http.connect()
    if (debug == true) PluginMain.logger.info("图灵发送:\n$out")
    http.outputStream.use { os -> os.write(out.encodeToByteArray()) }
    val re = InputStreamReader(http.inputStream, StandardCharsets.UTF_8).readText()
    if (debug == true) PluginMain.logger.info("图灵返回:\n$re")
    return re
}

internal fun MessageChain.containKey(l: List<String>, bot: Bot): List<SingleMessage> {
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