package bl.deflecc.freqcord.leech

import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.gateway.ratelimit.IdentifyRateLimiter
import dev.kord.gateway.retry.LinearRetry
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds


const val BOT_TOKEN = "MTA0MDk5ODQyMTQyODk3NzgyNg.GMlByD.b0Gmsg81p9uh7HYF0b5EWe0w9dRxTvM8TbyBaE"
const val API_URL = "http://127.0.0.1:8080/api"

suspend fun registerMessageToDb(client: HttpClient, guild: Guild?, message: Message) {
    val discordGuildId = guild?.id?.toString() ?: return
    val discordAuthorId = message.author?.id?.toString() ?: return
    val discordGuildName = guild.name
    val discordAuthorName = message.author?.username ?: return
    val discordMessageId = message.id.toString()
    val discordMessageContent = message.content
    val discordCreatedDateTime = message.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

    client.submitForm(url = "$API_URL/messages", Parameters.build {
        append("discordGuildId", discordGuildId)
        append("discordAuthorId", discordAuthorId)
        append("discordGuildName", discordGuildName)
        append("discordAuthorName", discordAuthorName)
        append("discordMessageId", discordMessageId)
        append("discordMessageContent", discordMessageContent)
        append("discordCreatedDateTime", discordCreatedDateTime)
    })
}

suspend fun main() {
    val kord = Kord(BOT_TOKEN) {
        this.gateways { resources, shards ->

            val rateLimiter = IdentifyRateLimiter(resources.maxConcurrency, defaultDispatcher)
            shards.map {
                DefaultGateway {
                    client = resources.httpClient
                    identifyRateLimiter = rateLimiter
                    reconnectRetry = LinearRetry(2.seconds, 20.seconds, Int.MAX_VALUE)
                }
            }
        }
    }
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    kord.on<MessageCreateEvent> {
        val guild = getGuild() ?: return@on

        if (message.content.contains("<so what became of reality. nothing came out of it.>")) {
            guild.channels.collect {
                it.asChannelOfOrNull<MessageChannel>()?.getMessagesBefore(message.id)?.collect { oldMessage ->
                    println(oldMessage.content)
                    registerMessageToDb(client, guild, oldMessage)
                }
            }
        }

        registerMessageToDb(client, guild, message)
    }

    @OptIn(PrivilegedIntent::class) kord.login {
        intents += Intent.GuildMessages
        intents += Intent.MessageContent
    }

}
