package bl.deflecc.freqcord.shared.models


import kotlinx.serialization.Serializable


@Serializable
data class Guild(
    val id: Int, val name: String, val authors: List<Author>
)

