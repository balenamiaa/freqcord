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
import kotlinx.coroutines.async
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
    private var binWidthInput = ObservableValue<Input?>(null)
    private var pieChart: Chart? = null
    private var lineChart: Chart? = null

    private fun onGuildsList(guilds: List<Guild>) {
        currentGuild.setState(guilds.firstOrNull())
    }

    private fun onCurrentGuild(newState: Guild?) {
        newState?.let { guild ->
            launch {
                val timestamps = Api.getTimestamps(guild)
                startTime.setState(timestamps.start)
                endTime.setState(timestamps.end)

                updateChartsData()
                updateCharts()
            }
        }
    }

    private suspend fun updateChartsData() {
        val guild = currentGuild.getState()
        val start = startTime.getState()
        val end = endTime.getState()
        if (guild != null && start != null && end != null) {

            lineChartDataForCurrentGuild.setState(
                Api.getLineChartData(
                    guild, start, end, "${binWidthInput.getState()?.value ?: "5"}m"
                )
            )

            pieChartDataForCurrentGuild.setState(
                Api.getPieChartData(
                    guild, start, end
                )
            )
        }
    }

    private fun updateCharts() {
        pieChartDataForCurrentGuild.getState()?.let {
            pieChart?.configuration = createPieChart(it)
        }

        lineChartDataForCurrentGuild.getState()?.let {
            lineChart?.configuration = createLineChart(it)
        }
    }

    override fun start(state: Map<String, Any>) {
        Routing.init(null, true, "#")


        guildsList.subscribe(::onGuildsList)
        currentGuild.subscribe(::onCurrentGuild)

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
                                div(className = "labelledInput") {
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
                                div(className = "labelledInput") {
                                    label("stop datetime") {}
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
                                div(className = "labelledInput") {
                                    label("bin-width") { }
                                    binWidthInput.value = input(InputType.NUMBER) {
                                        textAlign = TextAlign.CENTER
                                    }
                                }
                            }
                            options(4, 1) {
                                button("Generate Plot") {
                                    align = Align.CENTER
                                    justifySelf = JustifyItems.CENTER
                                }.onEvent {
                                    click = {
                                        launch {
                                            updateChartsData()
                                            updateCharts()
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

                            pieChart = chart().bind(pieChartDataForCurrentGuild) { data ->
                                data?.let { configuration = createPieChart(it) }
                            }
                        }

                        div {
                            id = "lineChart"
                            setStyle("flex", "0.5 1 0")
                            lineChart = chart().bind(lineChartDataForCurrentGuild) { data ->
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