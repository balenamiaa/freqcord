@file:UseSerializers(DateSerializer::class)

package bl.deflecc.freqcord.shared.models

import bl.deflecc.freqcord.shared.serializers.DateSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers


@Serializable
data class LineChartData(
    val names: List<String>,
    val xs: List<List<LocalDateTime>>,
    val ys: List<List<Int>>,
)