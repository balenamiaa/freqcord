@file:UseSerializers(DateSerializer::class)

package bl.deflecc.freqcord.shared.models

import bl.deflecc.freqcord.shared.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers


@Serializable
data class GetGuildsResponse(
    val guilds: List<Guild>
)