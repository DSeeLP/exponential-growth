package org.example

import com.formdev.flatlaf.FlatLightLaf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.ui.tabbedui.VerticalLayout
import java.math.BigInteger
import javax.swing.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
object Test : JFrame() {
    val yLabel = "Bacterias"
    val xLabel = "Times split"
    val chartTite = "Bacterias"

    val defaultDuration = hours(5)
    val defaultDurationSelector: DurationSelector = DurationSelector.Hours
    val defaultInterval = minutes(20)
    val defaultIntervalSelector: DurationSelector = DurationSelector.Minutes

    var currentDuration = defaultDuration
    var currentDurationSelector: DurationSelector = defaultDurationSelector
    var currentInterval = defaultInterval
    var currentIntervalSelector: DurationSelector = defaultIntervalSelector

    private val dataset: DefaultCategoryDataset = DefaultCategoryDataset()

    fun calculateData(duration: Duration, interval: Duration): MutableMap<Long, BigInteger> {
        val data = mutableMapOf<Long, BigInteger>()
        var currentValue = BigInteger.valueOf(1)
        var currentIndex: Long
        val between = duration.inWholeMilliseconds / interval.inWholeMilliseconds
        val twoValue = BigInteger.valueOf(2)
        data[0] = BigInteger.ONE
        for (index in 0 until between) {
            currentIndex = index + 1
            currentValue *= twoValue
            data[currentIndex] = currentValue
        }
        return data
    }

    init {
        FlatLightLaf.setup()
        setSize(600, 500)
        name = "Bacterias"


        val tabView = JTabbedPane()
        refresh()
        tabView.add("Settings", generateControlsPanel())
        tabView.add("Bar chart", ChartPanel(generateBarChart(dataset)))
        tabView.add("Line chart", ChartPanel(generateLineChart(dataset)))
        add(tabView)
        isVisible = true
    }

    fun generateLineChart(dataset: CategoryDataset): JFreeChart {
        return ChartFactory.createLineChart(
            chartTite,
            xLabel,
            yLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )
    }

    private fun DefaultCategoryDataset.addData(data: Map<Long, BigInteger>) {
        data.onEach {
            addValue(it.value, "Bacterias", it.key.toString())
        }
    }

    fun DefaultCategoryDataset.refresh(data: Map<Long, BigInteger>) {
        clear()
        addData(data)
    }

    fun refresh() = GlobalScope.launch {
        dataset.refresh(calculateData(currentDuration, currentInterval))
    }

    fun generateBarChart(dataset: CategoryDataset): JFreeChart {
        return ChartFactory.createBarChart(
            chartTite,
            xLabel,
            yLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )
    }

    fun generateControlsPanel(): JPanel {
        return JPanel().apply {
            layout = VerticalLayout()
            add(generateDurationSelector(defaultDuration, defaultDurationSelector) { duration, selector ->
                if (currentDuration == duration && selector == currentDurationSelector) return@generateDurationSelector
                println("New duration: ${duration.toString(DurationUnit.MINUTES)}")
                currentDuration = duration
                currentDurationSelector = selector
                refresh()
            })
            add(generateDurationSelector(defaultInterval, defaultIntervalSelector) { interval, selector ->
                if (currentInterval == interval && selector == currentIntervalSelector) return@generateDurationSelector
                println("New interval: ${interval.toString(DurationUnit.MINUTES)}")
                currentInterval = interval
                currentIntervalSelector = selector
                refresh()
            })
        }
    }

    fun generateDurationSelector(
        defaultDuration: Duration,
        defaultDurationSelector: DurationSelector,
        onNewSelected: (Duration, DurationSelector) -> Unit
    ): JPanel {
        onNewSelected(defaultDuration, defaultDurationSelector)
        return JPanel().apply {
            val jSpinner = JSpinner()
            val combobox = generateDurationSelectorComboBox()
            fun reverse() {
                jSpinner.value = defaultDurationSelector.reverse(defaultDuration)
                combobox.selectedItem = defaultDurationSelector
            }
            reverse()
            fun update() {
                val selector = combobox.selectedItem as DurationSelector
                val duration = selector.convert(jSpinner.value.toString().toLong())
                if (duration > selector.maxValue) {
                    reverse()
                    JOptionPane.showMessageDialog(this@Test, "The max time frame is 31 days ore 1 month")
                    return
                }
                if (jSpinner.value.toString().toLong() <= 0) {
                    reverse()
                    JOptionPane.showMessageDialog(this@Test, "The minimum value is 1!")
                    return
                }
                onNewSelected(duration, selector)
            }
            jSpinner.addChangeListener {
                update()
            }
            combobox.addActionListener {
                update()
            }
            add(jSpinner)
            add(combobox)
        }
    }

    fun generateDurationSelectorComboBox(durations: Array<DurationSelector> = DurationSelector.values()): JComboBox<DurationSelector> {
        val combobox = JComboBox<DurationSelector>()
        durations.onEach { combobox.addItem(it) }
        return combobox
    }
}