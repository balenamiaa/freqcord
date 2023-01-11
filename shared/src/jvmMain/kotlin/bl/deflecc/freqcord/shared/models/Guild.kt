package bl.deflecc.freqcord.shared.models

import org.jetbrains.exposed.sql.Table


object Guilds : Table() {
    val id = integer("id").autoIncrement()
    val discordGuildId = varchar("discordGuildId", 32)
    val discordGuildName = varchar("discordGuildName", 32)

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, discordGuildId)
    }
}