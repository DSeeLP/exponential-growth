package de.dseelp.bacteriatracker

import com.formdev.flatlaf.FlatLightLaf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.math.BigInteger
import javax.swing.*
import javax.swing.table.DefaultTableModel
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
object TrackerGui : JFrame() {
    val yLabel = "Bacterias"
    val xLabel = "Times split"
    val chartTite = "Bacterias"

    val defaultDuration = hours(5)
    val defaultDurationSelector: DurationSelector = DurationSelector.Hours
    val defaultInterval = minutes(20)
    val defaultIntervalSelector: DurationSelector = DurationSelector.Minutes

    private val dataset: DefaultCategoryDataset = DefaultCategoryDataset()

    val table = JTable().apply {
        val model = model as DefaultTableModel
        model.addColumn("Times split")
        model.addColumn("Bacterias")
        fillsViewportHeight = true
    }

    init {
        FlatLightLaf.setup()
        setSize(600, 500)
        name = "Bacterias"


        val tabView = JTabbedPane()
        tabView.add("Settings", generateControlsPanel {
            GlobalScope.launch {
                refresh(it)
            }
        })
        tabView.add("Bar chart", ChartPanel(generateBarChart(dataset)))
        tabView.add("Line chart", ChartPanel(generateLineChart(dataset)))
        tabView.add("Table", JScrollPane(generateTable()))
        add(tabView)
        defaultCloseOperation = EXIT_ON_CLOSE
        //pack()
        isVisible = true
        /*addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                GlobalScope.launch {
                    delay(1000)
                    exitProcess(0)
                }
            }
        })*/
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
        refreshTable(data)
    }

    fun refresh(data: BacteriaData) = GlobalScope.launch {
        dataset.refresh(data.calculate())
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

    fun refreshTable(data: Map<Long, BigInteger>) {
        val model = table.model as DefaultTableModel
        model.rowCount = 0
        data.onEach {
            model.addRow(arrayOf(it.key.toString(), it.value.toString()))
        }
    }

    fun generateTable() = JPanel().apply {
        add(table)
    }
}