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

internal data class TulingRequest(
    val perception: Perception,
    val reqType: Int,
    val userInfo: UserInfo
) {
    data class Perception(
        val inputText: InputText? = null,
        val inputImage: InputImage? = null,
        val inputMedia: InputMedia? = null
    ) {
        data class InputText(
            val text: String
        )

        data class InputImage(
            val url: String
        )

        data class InputMedia(
            val url: String
        )
    }

    data class UserInfo(
        val apiKey: String,
        val groupId: String?,
        val userId: String,
        val userIdName: String
    )
}

internal val apikeyErr = listOf("4003", "4500")

internal val errCode = mapOf(
    "5000" to "无解析结果",
    "6000" to "暂不支持该功能",
    "4000" to "请求参数格式错误",
    "4001" to "加密方式错误",
    "4002" to "无功能权限",
    "4003" to "该apikey没有可用请求次数",
    "4005" to "无功能权限",
    "4007" to "apikey不合法",
    "4100" to "userid获取失败",
    "4200" to "上传格式错误",
    "4300" to "批量操作超过限制",
    "4400" to "没有上传合法userid",
    "4500" to "userid申请个数超过限制",
//    "4600" to "输入内容为空",
    "4602" to "输入文本内容超长(上限150)",
    "7002" to "上传信息失败",
    "8008" to "服务器错误"
)