package bl.deflecc.freqcord.backend.dao

import bl.deflecc.freqcord.dao.DatabaseFactory.dbQuery
import bl.deflecc.freqcord.shared.models.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DAOFacadeImpl : DAOFacade {

    private fun resultRowToAuthor(row: ResultRow) = Author(
        id = row[Authors.id], name = row[Authors.discordAuthorName]
    )

    private fun resultRowToMessage(row: ResultRow) = Message(
        id = row[Messages.id],
        guildId = row[Messages.guildId],
        authorId = row[Messages.authorId],
        content = row[Messages.discordMessageContent],
        created = row[Messages.discordCreatedDateTime].toKotlinLocalDateTime(),
    )

    override suspend fun addNewMessage(
        guildId: Int,
        authorId: Int,
        discordMessageId: String,
        discordMessageContent: String?,
        discordCreatedDateTime: LocalDateTime
    ): Message? = dbQuery {
        val insertStatement = Messages.insert {
            it[Messages.guildId] = guildId
            it[Messages.authorId] = authorId
            it[Messages.discordMessageId] = discordMessageId
            it[Messages.discordCreatedDateTime] = discordCreatedDateTime.toJavaLocalDateTime()
            it[Messages.discordMessageContent] = discordMessageContent
        }

        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToMessage)
    }

    override suspend fun addNewMessageBulk(
        guildIds: List<Int>,
        authorIds: List<Int>,
        discordMessageIds: List<String>,
        discordMessageContents: List<String?>,
        discordCreatedDateTimes: List<LocalDateTime>
    ): List<Message> {
        val insertStatement = Messages.batchInsert(guildIds.indices) { idx ->
            this[Messages.guildId] = guildIds[idx]
            this[Messages.authorId] = authorIds[idx]
            this[Messages.discordMessageId] = discordMessageIds[idx]
            this[Messages.discordCreatedDateTime] = discordCreatedDateTimes[idx].toJavaLocalDateTime()
            this[Messages.discordMessageContent] = discordMessageContents[idx]
        }

        return insertStatement.map(::resultRowToMessage)
    }

    override suspend fun addNewAuthor(guildId: Int, discordAuthorId: String, discordAuthorName: String): Author? =
        dbQuery {
            val insertStatement = Authors.insert {
                it[Authors.guildId] = guildId
                it[Authors.discordAuthorId] = discordAuthorId
                it[Authors.discordAuthorName] = discordAuthorName
            }

            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToAuthor)
        }

    override suspend fun addNewGuild(discordGuildId: String, discordGuildName: String): Guild? = dbQuery {
        val insertStatement = Guilds.insert {
            it[Guilds.discordGuildId] = discordGuildId
            it[Guilds.discordGuildName] = discordGuildName
        }

        insertStatement.resultedValues?.singleOrNull()?.let {
            val id = it[Guilds.id]
            val name = it[Guilds.discordGuildName]
            Guild(
                id, name, Authors.select(Authors.guildId eq it[Guilds.id]).map(::resultRowToAuthor)
            )
        }
    }

    override suspend fun getMessages(
        guildId: Int, startDateTime: LocalDateTime, endDateTime: LocalDateTime
    ): List<Message> = dbQuery {
        Messages.select(Messages.guildId eq guildId).filter {
            it[Messages.discordCreatedDateTime].isBefore(endDateTime.toJavaLocalDateTime()) and it[Messages.discordCreatedDateTime].isAfter(
                startDateTime.toJavaLocalDateTime()
            )
        }.map(::resultRowToMessage)
    }

    override suspend fun getTimestamps(guildId: Int): Pair<LocalDateTime, LocalDateTime> = dbQuery {
        val start = Messages.selectAll()
            .minBy { it[Messages.discordCreatedDateTime] }[Messages.discordCreatedDateTime].toKotlinLocalDateTime()

        val end = Messages.selectAll()
            .maxBy { it[Messages.discordCreatedDateTime] }[Messages.discordCreatedDateTime].toKotlinLocalDateTime()

        return@dbQuery start to end
    }

    override suspend fun getAuthor(authorId: Int): Author? = dbQuery {
        return@dbQuery Authors.select(Authors.id eq authorId).firstOrNull()?.let(::resultRowToAuthor)
    }


    override suspend fun getGuilds(): List<Guild> = dbQuery {
        return@dbQuery Guilds.selectAll().map {
            val id = it[Guilds.id]
            val name = it[Guilds.discordGuildName]
            Guild(
                id, name, Authors.select(Authors.guildId eq it[Guilds.id]).map(::resultRowToAuthor)
            )
        }
    }

    override suspend fun deleteMessage(id: Int): Boolean = dbQuery {
        Messages.deleteWhere { Messages.id eq id } > 0
    }

    override suspend fun getOrCreateGuildId(
        discordGuildId: String, discordGuildName: String
    ): Int = dbQuery {
        Guilds.select(Guilds.discordGuildId eq discordGuildId).let {
            if (it.count() == 0L) {
                val newGuild = dao.addNewGuild(
                    discordGuildId, discordGuildName
                )
                newGuild!!.id
            } else {
                it.first()[Guilds.id]
            }
        }
    }

    override suspend fun getOrCreateAuthorId(
        guildId: Int, discordAuthorId: String, discordAuthorName: String
    ): Int = dbQuery {
        Authors.select(Authors.discordAuthorId eq discordAuthorId).let {
            if (it.count() == 0L) {
                val newAuthor = dao.addNewAuthor(
                    guildId, discordAuthorId, discordAuthorName
                )
                newAuthor!!.id
            } else {
                it.first()[Authors.id]
            }
        }
    }
}

val dao = DAOFacadeImpl()