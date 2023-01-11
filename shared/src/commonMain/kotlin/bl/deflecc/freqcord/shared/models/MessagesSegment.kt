@file:UseSerializers(DateSerializer::class)

package bl.deflecc.freqcord.shared.models

import bl.deflecc.freqcord.shared.serializers.DateSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers


@Serializable
data class MessagesSegment(
    val messages: List<Message>,
    val guild: Guild,
    val authors: List<Author>,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)