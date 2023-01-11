@file:UseSerializers(DateSerializer::class)

package bl.deflecc.freqcord.shared.models

import bl.deflecc.freqcord.shared.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers


@Serializable
data class PieChartData(
    val names: List<String>,
    val counts: List<Int>
)