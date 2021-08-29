plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.7.0"
}

group = "tech.Eritque_arcus"
version = "1.4.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public/") }
    google()
}
dependencies{
    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20210307")
    implementation("com.google.code.gson:gson:2.8.8")

}