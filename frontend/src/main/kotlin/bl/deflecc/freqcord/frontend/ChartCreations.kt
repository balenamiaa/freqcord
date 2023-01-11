package bl.deflecc.freqcord.frontend

import bl.deflecc.freqcord.shared.models.LineChartData
import bl.deflecc.freqcord.shared.models.PieChartData

fun createPieChart(pieChartData: PieChartData): dynamic {

    val data = pieChartData.names.zip(pieChartData.counts).map { (name, count) ->
        jsObject {
            value = count
            this.name = name
        }
    }.toTypedArray()

    return jsObject {
        legend = jsObject {
            orient = "vertical"
            left = "left"
        }
        tooltip = jsObject {
            trigger = "item"
        }
        series = arrayOf(jsObject {
            name = "Server"
            type = "pie"
            radius = "50%"
            this.data = data
            emphasis = jsObject {
                itemStyle = jsObject {
                    shadowBlur = 10
                    shadowOffsetX = 0
                    shadowColor = "rgba(0, 0, 0, 0.5)"
                }
            }
        })
    }
}

fun createLineChart(lineChartData: LineChartData): dynamic {

    val names = lineChartData.names
    val xs = lineChartData.xs
    val ys = lineChartData.ys

    return jsObject {
        tooltip = jsObject {
            trigger = "axis"
            axisPointer = jsObject {
                type = "cross"
            }
        }

        dataZoom = arrayOf(jsObject {
            type = "inside"
            start = 0
            end = 100
        }, jsObject {
            start = 0
            end = 100
        })

        toolbox = jsObject {
            feature = jsObject {
                dataZoom = jsObject {
                    yAxisIndex = "none"
                }
                restore = jsObject {}
                saveAsImage = jsObject {}
            }
        }

        xAxis = jsObject {
            type = "category"
            boundaryGap = false
        }

        yAxis = jsObject {
            type = "value"
            axisPointer = jsObject {
                snap = true
            }
        }

        legend = jsObject {
            this.data = names.toTypedArray()
        }

        series = names.indices.map { idx ->
            jsObject {
                this.name = names[idx]
                type = "line"
                smooth = 0.6
                symbol = "none"
                areaStyle = jsObject {}
                this.data = xs[idx].zip(ys[idx]).map { (x, y) -> arrayOf(x.toString(), y) }.toTypedArray()
            }
        }.toTypedArray()
    }
}