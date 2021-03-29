# Mirai-TuLingBot
Mirai接入图灵机器人

图灵虽然感觉越做越水了，不过还可能免费用

# 使用方法
1. 下载release中的插件文件
2. 去[图灵个人中心](www.tuling123.com)取到api令牌
![image](https://user-images.githubusercontent.com/35139537/110485654-d78cc780-8126-11eb-890a-aa68f9a5f0d3.png)
3. 把插件放到mcl里运行一次，插件会把配置文件目录通过日志发送出来(即在`data/TuLingBot/config.json`)，打开编辑这个文件
4. 按照以下格式输入
```json
{
"apikey":"api令牌",
"gkeyword":"群聊触发开始字符",
"fkeyword":"私聊触发开始字符"
}
```
其中，开始字符为空则代表在任何情况都触发
5. 运行mcl
