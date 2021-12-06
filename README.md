# Mirai-TuLingBot
> Build Test: [![Build test](https://github.com/Nambers/Mirai-TuLingBot/actions/workflows/gradle.yml/badge.svg)](https://github.com/Nambers/Mirai-TuLingBot/actions/workflows/gradle.yml)

Mirai接入图灵机器人

图灵虽然感觉越做越水了，不过还可能免费用

# 使用方法
1. 下载release中的插件文件
2. 去[图灵个人中心](www.tuling123.com)取到api令牌
![image](https://user-images.githubusercontent.com/35139537/110485654-d78cc780-8126-11eb-890a-aa68f9a5f0d3.png)
3. 把插件放到mcl里运行一次，插件会把配置文件目录通过日志发送出来(即在`data/TuLingBot/config.json`或`data/tech.eritquearcus.tuling/config.json`)，打开编辑这个文件
4. 按照以下格式输入
```json
{
"apikey":"api令牌",
"gkeyword":["群聊触发开始字符串1", "字符串2", "@bot"],
"fkeyword": []
}
```
其中
- `gkeyword` 代表在群聊中的触发字符, `@bot` 代表 @bot 的时候也触发
- `fkeyword` 代表在私聊中的触发字符, 为空代表任意消息都会触发
- [可选] `debug` 代表要不要接收内部日志, 如果为true代表会打印, false或不填代表不会
5. 运行mcl

# 许可
```
Copyright (C) 2021-2021 Eritque arcus and contributors.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or any later version(in your opinion).

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

```
