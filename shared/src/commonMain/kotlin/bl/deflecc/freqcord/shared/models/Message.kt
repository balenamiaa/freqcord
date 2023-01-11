@file:UseSerializers(DateSerializer::class)

package bl.deflecc.freqcord.shared.models

import bl.deflecc.freqcord.shared.serializers.DateSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Message(
    val id: Int, val guildId: Int, val authorId: Int, val content: String?, val created: LocalDateTime
)

