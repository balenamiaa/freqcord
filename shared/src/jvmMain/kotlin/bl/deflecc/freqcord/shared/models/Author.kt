package bl.deflecc.freqcord.shared.models

import org.jetbrains.exposed.sql.Table


object Authors : Table() {
    val id = integer("id").autoIncrement()
    val guildId = integer("guildId") references Guilds.id
    val discordAuthorId = varchar("discordAuthorId", 32)
    val discordAuthorName = varchar("discordAuthorName", 32)

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, discordAuthorId)
    }
}