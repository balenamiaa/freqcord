package bl.deflecc.freqcord.backend.routes

import bl.deflecc.freqcord.backend.dao.dao
import bl.deflecc.freqcord.shared.models.GetGuildsResponse
import bl.deflecc.freqcord.shared.models.GetTimeStampsResponse
import bl.deflecc.freqcord.shared.models.LineChartData
import bl.deflecc.freqcord.shared.models.PieChartData
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.toJavaDuration


class DateTimeIterator(
    startDate: LocalDateTime, private val endDateInclusive: LocalDateTime, private val step: Duration
) : Iterator<LocalDateTime> {
    private var currentDate = startDate

    override fun hasNext() = currentDate <= endDateInclusive

    override fun next(): LocalDateTime {

        val next = currentDate

        currentDate = currentDate.toInstant(TimeZone.currentSystemDefault()).plus(step)
            .toLocalDateTime(TimeZone.currentSystemDefault())

        return next

    }

}

class DateTimeProgression(
    override val start: LocalDateTime,
    override val endInclusive: LocalDateTime,
    private val step: Duration = Duration.parse("1h")
) : Iterable<LocalDateTime>, ClosedRange<LocalDateTime> {

    override fun iterator(): Iterator<LocalDateTime> = DateTimeIterator(start, endInclusive, step)

    infix fun step(duration: Duration) = DateTimeProgression(start, endInclusive, duration)

}

operator fun LocalDateTime.rangeTo(other: LocalDateTime) = DateTimeProgression(this, other)


fun Route.messageRouting() {
    route("/messages") {
        post {
            val formParameters = call.receiveParameters()
            val discordGuildId = formParameters.getOrFail("discordGuildId")
            val discordAuthorId = formParameters.getOrFail("discordAuthorId")
            val discordMessageId = formParameters.getOrFail("discordMessageId")
            val discordGuildName = formParameters.getOrFail("discordGuildName")
            val discordAuthorName = formParameters.getOrFail("discordAuthorName")
            val discordMessageContent = formParameters["discordMessageContent"]
            val discordCreatedDateTime =
                formParameters.getOrFail("discordCreatedDateTime").let { LocalDateTime.parse(it) }

            val guildId = dao.getOrCreateGuildId(discordGuildId, discordGuildName)
            val authorId = dao.getOrCreateAuthorId(guildId, discordAuthorId, discordAuthorName)

            val message =
                dao.addNewMessage(guildId, authorId, discordMessageId, discordMessageContent, discordCreatedDateTime)
                    ?: throw InternalError()
            call.respond(message)
        }

        post("bulk") {
            val formParameters = call.receiveParameters()
            val guildIds = formParameters.getOrFail<List<Int>>("guildId")
            val authorIds = formParameters.getOrFail<List<Int>>("authorId")
            val discordMessageIds = formParameters.getOrFail<List<String>>("discordMessageId")
            val discordMessageContents = formParameters.getOrFail<List<String?>>("discordMessageContent")
            val discordCreatedDateTimes =
                formParameters.getOrFail<List<String>>("discordCreatedDateTime").map { LocalDateTime.parse(it) }

            val message = dao.addNewMessageBulk(
                guildIds, authorIds, discordMessageIds, discordMessageContents, discordCreatedDateTimes
            )
            call.respond(message)
        }
    }

    route("/guilds") {
        post {
            val formParameters = call.receiveParameters()
            val discordGuildId = formParameters.getOrFail("discordGuildId")
            val discordGuildName = formParameters.getOrFail("discordGuildName")

            val guild = dao.addNewGuild(discordGuildId, discordGuildName) ?: throw InternalError()
            call.respond(guild)
        }
    }

    route("/authors") {
        post {
            val formParameters = call.receiveParameters()
            val guildId = formParameters.getOrFail("guildId").let { Integer.parseInt(it) }
            val discordAuthorId = formParameters.getOrFail("discordAuthorId")
            val discordAuthorName = formParameters.getOrFail("discordAuthorName")

            val guild = dao.addNewAuthor(guildId, discordAuthorId, discordAuthorName) ?: throw InternalError()
            call.respond(guild)
        }
    }

    route("/stats") {
        get("pie_chart_data") {
            val guildId = call.request.queryParameters.getOrFail("guildId").let { Integer.parseInt(it) }
            val startTime = call.request.queryParameters.getOrFail("startTime").let { LocalDateTime.parse(it) }
            val stopTime = call.request.queryParameters.getOrFail("stopTime").let { LocalDateTime.parse(it) }
            val messages = dao.getMessages(guildId, startTime, stopTime)
            val names: MutableList<String> = mutableListOf()
            val counts: MutableList<Int> = mutableListOf()

            for ((name, groupedMessages) in messages.groupBy { dao.getAuthor(it.authorId)!!.name }) {
                names.add(name)
                counts.add(groupedMessages.count())
            }

            call.respond(PieChartData(names, counts))
        }

        get("line_chart_data") {
            val binWidth = call.request.queryParameters.getOrFail("binWidth").let { Duration.parse(it) }
            val guildId = call.request.queryParameters.getOrFail("guildId").let { Integer.parseInt(it) }
            val startTime = call.request.queryParameters.getOrFail("startTime").let { LocalDateTime.parse(it) }
            val stopTime = call.request.queryParameters.getOrFail("stopTime").let { LocalDateTime.parse(it) }
            val messages = dao.getMessages(guildId, startTime, stopTime)
            val names: MutableList<String> = mutableListOf()
            val xs: MutableList<List<LocalDateTime>> = mutableListOf()
            val ys: MutableList<List<Int>> = mutableListOf()

            for ((name, groupedMessages) in messages.groupBy { dao.getAuthor(it.authorId)!!.name }) {
                val xsForName: MutableList<LocalDateTime> = mutableListOf()
                val ysForName: MutableList<Int> = mutableListOf()

                for (time in startTime..stopTime step binWidth) {
                    xsForName.add(time)
                    ysForName.add(groupedMessages.count {
                        time.rangeTo((time.toJavaLocalDateTime() + binWidth.toJavaDuration()).toKotlinLocalDateTime())
                            .contains(it.created)
                    })
                }

                names.add(name)
                xs.add(xsForName)
                ys.add(ysForName)
            }

            call.respond(LineChartData(names, xs, ys))
        }

        get("get_time_stamps") {
            val guildId = call.request.queryParameters.getOrFail("guildId").let { Integer.parseInt(it) }
            call.respond(dao.getTimestamps(guildId).let { GetTimeStampsResponse(it.first, it.second) })
        }

        get("guilds") {
            call.respond(GetGuildsResponse(dao.getGuilds()))
        }
    }
}