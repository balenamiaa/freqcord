package bl.deflecc.freqcord.shared.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime


object Messages : Table() {
    val id = integer("id").autoIncrement()
    val guildId = integer("guildId") references Guilds.id
    val authorId = integer("authorId") references Authors.id
    val discordMessageId = varchar("discordMessageId", 32)
    val discordMessageContent = text("discordMessageContent").nullable()
    val discordCreatedDateTime = datetime("discordCreatedDateTime")

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, discordMessageId)
    }
}