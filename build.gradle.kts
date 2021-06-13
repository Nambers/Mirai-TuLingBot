plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.6.5"
}

group = "tech.Eritque_arcus"
version = "1.3.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven{ url =uri("https://maven.aliyun.com/nexus/content/groups/public/")}
    google()
}
dependencies{
    //在IDE内运行的mcl添加滑块模块，请参考https://github.com/project-mirai/mirai-login-solver-selenium把版本更新为最新
    //runtimeOnly("net.mamoe:mirai-login-solver-selenium:1.0-dev-15")
    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20201115")

}