package bl.deflecc.freqcord.frontend

import bl.deflecc.freqcord.shared.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDateTime

object Api {
    private const val API_URL: String = "http://127.0.0.1:8080/api"
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getGuilds(): GetGuildsResponse = client.get("$API_URL/stats/guilds").body()
    suspend fun getPieChartData(guild: Guild, startTime: LocalDateTime, stopTime: LocalDateTime): PieChartData =
        client.get("$API_URL/stats/pie_chart_data") {
            parameter("guildId", guild.id)
            parameter("startTime", startTime)
            parameter("stopTime", stopTime)
        }.body()

    suspend fun getLineChartData(guild: Guild, startTime: LocalDateTime, stopTime: LocalDateTime): LineChartData =
        client.get("$API_URL/stats/line_chart_data") {
            parameter("guildId", guild.id)
            parameter("startTime", startTime)
            parameter("stopTime", stopTime)
        }.body()

    suspend fun getTimestamps(guild: Guild): GetTimeStampsResponse = client.get("$API_URL/stats/get_time_stamps") {
        parameter("guildId", guild.id)
    }.body()
}