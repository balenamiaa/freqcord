package bl.deflecc.freqcord.frontend

import io.kvision.navigo.Navigo

enum class View(val url: String) {
    HOME("/"),
}

fun Navigo.initialize(): Navigo {
    return on(View.HOME.url, { _ -> })
}

fun stringParameter(params: dynamic, parameterName: String): String {
    return (params[parameterName]).toString()
}