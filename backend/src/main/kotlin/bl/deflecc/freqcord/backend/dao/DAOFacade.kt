package bl.deflecc.freqcord.backend.dao

import bl.deflecc.freqcord.shared.models.Author
import bl.deflecc.freqcord.shared.models.Guild
import bl.deflecc.freqcord.shared.models.Message
import kotlinx.datetime.LocalDateTime

interface DAOFacade {
    suspend fun getGuilds(): List<Guild>
    suspend fun addNewMessage(
        guildId: Int,
        authorId: Int,
        discordMessageId: String,
        discordMessageContent: String?,
        discordCreatedDateTime: LocalDateTime
    ): Message?

    suspend fun addNewMessageBulk(
        guildIds: List<Int>,
        authorIds: List<Int>,
        discordMessageIds: List<String>,
        discordMessageContents: List<String?>,
        discordCreatedDateTimes: List<LocalDateTime>
    ): List<Message>

    suspend fun addNewAuthor(
        guildId: Int,
        discordAuthorId: String,
        discordAuthorName: String,
    ): Author?

    suspend fun addNewGuild(
        discordGuildId: String,
        discordGuildName: String,
    ): Guild?

    suspend fun getOrCreateGuildId(
        discordGuildId: String,
        discordGuildName: String
    ): Int

    suspend fun getOrCreateAuthorId(
        guildId: Int,
        discordAuthorId: String,
        discordAuthorName: String
    ): Int

    suspend fun getMessages(guildId: Int, startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<Message>
    suspend fun deleteMessage(id: Int): Boolean
    suspend fun getTimestamps(guildId: Int): Pair<LocalDateTime, LocalDateTime>
    suspend fun getAuthor(authorId: Int): Author?
}