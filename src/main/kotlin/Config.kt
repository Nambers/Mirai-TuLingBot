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