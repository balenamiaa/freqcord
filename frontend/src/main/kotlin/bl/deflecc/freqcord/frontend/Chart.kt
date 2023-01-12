package bl.deflecc.freqcord.frontend

import io.kvision.core.Container
import io.kvision.core.Widget
import io.kvision.html.Div
import io.kvision.snabbdom.VNode
import kotlinx.browser.window

@JsModule("echarts")
@JsNonModule()
external object echarts {
    fun init(dom: dynamic): Chart = definedExternally
    fun init(dom: dynamic, unk_00: dynamic, options: dynamic): Chart = definedExternally

    class Chart {
        fun setOption(option: dynamic): Unit = definedExternally
    }
}

class ChartDiv(
    config: dynamic = null
) : Div() {

    var jsChart: dynamic = null

    var config = config
        set(value) {
            field = value
            if (value != null) jsChart?.setOption(value, true)
        }

    init {
        useSnabbdomDistinctKey()
    }

    override fun render(): VNode {
        return render("div")
    }

    override fun afterInsert(node: VNode) {
        jsChart = echarts.init(node.elm, null, jsObject {})

        window.onresize = {
            jsChart.resize()
            this
        }
    }

    override fun afterDestroy() {
        jsChart?.dispose()
        jsChart = null
    }
}

open class Chart(configuration: dynamic = null, init: (Chart.() -> Unit)? = null) : Widget() {
    var configuration
        get() = chartDiv.config
        set(value) {
            chartDiv.config = value
        }


    internal val chartDiv: ChartDiv = ChartDiv(configuration)

    init {
        chartDiv.setStyle("flex", "1 1 0")
        chartDiv.setStyle("height", "100%")

        setStyle("display", "flex")
        setStyle("flex", "1 1 0")
        setStyle("height", "100%")
        @Suppress("LeakingThis") init?.invoke(this)
    }

    override fun render(): VNode {
        return render("div", arrayOf(chartDiv.renderVNode()))
    }

}

fun Container.chart(configuration: dynamic = null, init: (Chart.() -> Unit)? = null): Chart {
    val chart = Chart(configuration, init)
    this.add(chart)
    return chart
}