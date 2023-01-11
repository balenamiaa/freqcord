package bl.deflecc.freqcord.frontend

import kotlinx.datetime.LocalDateTime

fun jsObject(block: dynamic.() -> Unit): dynamic {
    var obj = js("{}")
    block(obj)
    return obj
}


fun LocalDateTime.trimmedStringForJs(): String {
    val str = this.toString()
    return if (str.count { c -> c == ':' } > 1) str.slice(0 until str.indexOfLast { c -> c == ':' }) else str
}