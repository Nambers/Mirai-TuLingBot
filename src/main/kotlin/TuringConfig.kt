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

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object TuringConfig:AutoSavePluginConfig("config") {
    @ValueDescription("图灵机器人Apikey")
    val apikey:String by value()

    @ValueDescription("唤起关键词(群组)")
    val groupKeyword:List<String> by value()

    @ValueDescription("唤起关键词(私聊)")
    val friendKeyword:List<String> by value()

    @ValueDescription("是否输出debug信息")
    val debug:Boolean by value(false)

    @ValueDescription("图灵服务不可用时的自定义回复")
    val overLimitReply:List<String> by value()
}