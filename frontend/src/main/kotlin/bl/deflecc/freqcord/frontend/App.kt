package bl.deflecc.freqcord.frontend

import bl.deflecc.freqcord.shared.models.Guild
import bl.deflecc.freqcord.shared.models.LineChartData
import bl.deflecc.freqcord.shared.models.PieChartData
import io.kvision.*
import io.kvision.core.*
import io.kvision.form.select.simpleSelectInput
import io.kvision.html.*
import io.kvision.panel.flexPanel
import io.kvision.panel.responsiveGridPanel
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.state.ObservableListWrapper
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.rem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.coroutines.CoroutineContext

class App : Application(), CoroutineScope {
    private val guildsList = ObservableListWrapper(mutableListOf<Guild>())
    private val currentGuild = ObservableValue<Guild?>(null)
    private val startTime = ObservableValue<LocalDateTime?>(null)
    private val endTime = ObservableValue<LocalDateTime?>(null)
    private val lineChartDataForCurrentGuild = ObservableValue<LineChartData?>(null)
    private val pieChartDataForCurrentGuild = ObservableValue<PieChartData?>(null)

    private fun onGuildsList(guilds: List<Guild>) {
        currentGuild.setState(guilds.firstOrNull())
    }

    private fun onCurrentGuild(newState: Guild?) {
        newState?.let { guild ->
            launch {
                val timestamps = Api.getTimestamps(guild)
                startTime.setState(timestamps.start)
                endTime.setState(timestamps.end)

                lineChartDataForCurrentGuild.setState(Api.getLineChartData(guild, timestamps.start, timestamps.end))
                pieChartDataForCurrentGuild.setState(Api.getPieChartData(guild, timestamps.start, timestamps.end))
            }
        }
    }

    private fun onTimestampStart(newState: LocalDateTime?) {
        newState?.let { start ->
            val guild = currentGuild.getState()
            val end = endTime.getState()
            if (guild != null && end != null) {
                launch {
                    lineChartDataForCurrentGuild.setState(
                        Api.getLineChartData(
                            guild, start, end
                        )
                    )
                    pieChartDataForCurrentGuild.setState(
                        Api.getPieChartData(
                            guild, start, end
                        )
                    )
                }
            }
        }
    }

    private fun onTimestampEnd(newState: LocalDateTime?) {
        newState?.let { end ->
            val guild = currentGuild.getState()
            val start = startTime.getState()
            if (guild != null && start != null) {
                launch {
                    lineChartDataForCurrentGuild.setState(
                        Api.getLineChartData(
                            guild, start, end
                        )
                    )
                    pieChartDataForCurrentGuild.setState(
                        Api.getPieChartData(
                            guild, start, end
                        )
                    )
                }
            }
        }
    }

    override fun start(state: Map<String, Any>) {
        Routing.init(null, true, "#")


        guildsList.subscribe(::onGuildsList)
        currentGuild.subscribe(::onCurrentGuild)
        startTime.subscribe(::onTimestampStart)
        endTime.subscribe(::onTimestampEnd)

        launch {
            guildsList.clear()
            guildsList.addAll(Api.getGuilds().guilds)
        }

        root("kvapp") {
            main {
                height = 100.perc
                padding = 10.px

                flexPanel(FlexDirection.COLUMN, spacing = 2) {
                    height = 100.perc

                    flexPanel(FlexDirection.COLUMN, spacing = 12) {
                        id = "inputBox"

                        flexPanel(FlexDirection.ROW, spacing = 2, alignItems = AlignItems.CENTER) {
                            label("Server")
                            simpleSelectInput {
                                marginLeft = 1.rem
                                setStyle("flex", "1 1 0")
                                options = guildsList.map { it.name to it.name }
                            }.bind(guildsList) { guilds ->
                                options = guilds.map { it.name to it.name }
                            }.onEvent {
                                change = { event ->
                                    guildsList.getState().find { it.name == event.target.asDynamic().value }
                                        ?.let(currentGuild::setState)
                                }
                            }
                        }


                        responsiveGridPanel {
                            options(1, 1) {
                                flexPanel(FlexDirection.COLUMN, spacing = 1, alignItems = AlignItems.CENTER) {
                                    label("Start Date-Time") {}
                                    input(InputType.DATETIME_LOCAL) {
                                        textAlign = TextAlign.CENTER
                                        width = 100.perc
                                    }.bind(startTime) { startTime ->
                                        startTime?.let { value = it.trimmedStringForJs() }
                                    }.onEvent {
                                        change = {
                                            startTime.setState(LocalDateTime.parse(it.target.asDynamic().value as String))
                                        }
                                    }
                                }
                            }

                            options(2, 1) {
                                flexPanel(FlexDirection.COLUMN, spacing = 1, alignItems = AlignItems.CENTER) {
                                    label("Stop Date-Time") {}
                                    input(InputType.DATETIME_LOCAL) {
                                        textAlign = TextAlign.CENTER
                                        width = 100.perc
                                    }.bind(endTime) { endTime ->
                                        endTime?.let { value = it.trimmedStringForJs() }
                                    }.onEvent {
                                        change = {
                                            endTime.setState(LocalDateTime.parse(it.target.asDynamic().value as String))
                                        }
                                    }
                                }
                            }

                            options(3, 1) {
                                flexPanel(FlexDirection.COLUMN, spacing = 1, alignItems = AlignItems.CENTER) {
                                    label("Stop Date-Time") {}
                                    input(InputType.DATETIME_LOCAL) {
                                        textAlign = TextAlign.CENTER
                                        width = 100.perc
                                    }.bind(endTime) { endTime ->
                                        endTime?.let { value = it.trimmedStringForJs() }
                                    }.onEvent {
                                        change = {
                                            endTime.setState(LocalDateTime.parse(it.target.asDynamic().value as String))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    div {
                        id = "chartBox"
                        display = Display.FLEX
                        setStyle("flex", "1 1 0")

                        div {
                            id = "pieChart"
                            setStyle("flex", "0.5 1 0")

                            chart().bind(pieChartDataForCurrentGuild) { data ->
                                data?.let { configuration = createPieChart(it) }
                            }
                        }

                        div {
                            id = "lineChart"
                            setStyle("flex", "0.5 1 0")
                            chart().bind(lineChartDataForCurrentGuild) { data ->
                                data?.let { configuration = createLineChart(it) }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        return mapOf()
    }

    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = job
}

fun main() {
    startApplication(::App, module.hot, CoreModule, BootstrapCssModule, BootstrapModule)
}