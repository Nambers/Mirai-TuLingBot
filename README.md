# Mirai-TuLingBot
> Build Test: [![Build test](https://github.com/Nambers/Mirai-TuLingBot/actions/workflows/gradle.yml/badge.svg)](https://github.com/Nambers/Mirai-TuLingBot/actions/workflows/gradle.yml)

Mirai接入图灵机器人

图灵虽然感觉越做越水了，不过还可能免费用

# 使用方法
1. 下载release中的插件文件
2. 去[图灵个人中心](www.tuling123.com)取到api令牌
![image](https://user-images.githubusercontent.com/35139537/110485654-d78cc780-8126-11eb-890a-aa68f9a5f0d3.png)
3. 把插件放到mcl里运行一次，插件会把配置文件目录通过日志发送出来(即在`config/tech.eritquearcus.tuling/config.yml`)，打开编辑这个文件
4. 按照以下格式输入
```yml
# 图灵机器人Apikey
apikey:
  - key1
  - key2
# 唤起关键词(群组)
groupKeyword:
  - @bot
# 唤起关键词(私聊)
friendKeyword: []
# 是否输出debug信息
debug: false
# 图灵服务不可用时的自定义回复
overLimitReply:
  - 错误错误错误
```
其中
- `gkeyword` 代表在群聊中的触发字符, `@bot` 代表 @bot 的时候也触发
- `fkeyword` 代表在私聊中的触发字符, 为空代表任意消息都会触发
- \[可选] `debug` 代表要不要接收内部日志, 如果为true代表会打印, false或不填代表不会
- \[可选] `overLimitReply` 当图灵服务[不可用](https://www.kancloud.cn/turing/www-tuling123-com/718227#:~:text=%E5%BC%82%E5%B8%B8%E8%BF%94%E5%9B%9E%E8%AF%B4%E6%98%8E-,%E5%BC%82%E5%B8%B8%E7%A0%81,-%E8%AF%B4%E6%98%8E)(通常为4003请求次数超过限制)时随机回复其中一个
5. 运行mcl

# 许可
```
Copyright (C) 2021-2022 Eritque arcus and contributors.

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
