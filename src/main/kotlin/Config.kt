package tech.eritquearcus.tuling

data class config(
    val apikey: String,
    val gkeyWord: List<String>,
    val fkeyWord: List<String>,
    val debug: Boolean?
)