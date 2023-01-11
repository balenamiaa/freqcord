@file:UseSerializers(DateSerializer::class)

package bl.deflecc.freqcord.shared.models

import bl.deflecc.freqcord.shared.serializers.DateSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers


@Serializable
data class GetTimeStampsResponse(
    val start: LocalDateTime, val end: LocalDateTime
)