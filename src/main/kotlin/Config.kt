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

data class Config(
    val apikey: String,
    val gkeyWord: List<String>,
    val fkeyWord: List<String>,
    val debug: Boolean?
)

data class TulingRequest(
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