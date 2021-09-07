package de.dseelp.bacteriatracker

import org.jfree.ui.tabbedui.VerticalLayout
import javax.swing.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun generateControlsPanel(process: (data: BacteriaData) -> Unit): JPanel {
    return JPanel().apply {
        layout = VerticalLayout()
        var duration = TrackerGui.defaultDuration
        var interval = TrackerGui.defaultInterval
        var oldSteps = DataUtil.calculateSteps(duration, interval)
        var steps: Int = oldSteps
        fun calcSteps() {
            steps = DataUtil.calculateSteps(duration, interval)
        }
        add(generateDurationSelector("Duration", TrackerGui.defaultDuration, TrackerGui.defaultDurationSelector) { nDuration, selector ->
            if (duration == nDuration) return@generateDurationSelector
            duration = nDuration
            calcSteps()
        })
        add(generateDurationSelector("Interval", TrackerGui.defaultInterval, TrackerGui.defaultIntervalSelector) { nInterval, selector ->
            if (interval == nInterval) return@generateDurationSelector
            interval = nInterval
            calcSteps()
        })
        val zoomSelector = generateZoomSelector(steps)
        add(zoomSelector.panel)
        val processButton = JButton("Process")
        var oldZoom = zoomSelector.toZoom()
        processButton.addActionListener {
            if (oldSteps == steps && zoomSelector.toZoom() == oldZoom) return@addActionListener
            if (oldSteps != steps) zoomSelector.refresh(steps)
            val data = BacteriaData(steps, zoomSelector.toZoom())
            oldSteps = steps
            oldZoom = data.zoom
            process(data)
        }
        add(JPanel().apply {
            add(processButton)
        })
        process(BacteriaData.generate(TrackerGui.defaultDuration, TrackerGui.defaultInterval))
    }
}

fun generateZoomSelector(steps: Int): ZoomSelector {
    return ZoomSelector(steps)
}

class ZoomSelector(steps: Int, val fromSpinner: JSpinner = JSpinner(SpinnerNumberModel(1, 1, steps, 1)), val toSpinner: JSpinner = JSpinner(SpinnerNumberModel(steps, 1, steps, 1))) {
    var steps = steps
    private set
    val panel = JPanel().apply {
        add(JLabel("Zoom"))
        add(fromSpinner)
        add(toSpinner)
        val resetButton = JButton("Reset")
        resetButton.addActionListener { refresh(steps) }
        add(resetButton)
    }
    fun refresh(steps: Int) {
        this.steps = steps
        val fromSpinnerValue = fromSpinner.value.toString().toInt()
        val nFromSpinnerValue = if (fromSpinnerValue in 1..steps) fromSpinnerValue else steps
        val toSpinnerValue = toSpinner.value.toString().toInt()
        val nToSpinnerValue = if (toSpinnerValue in 1..steps) toSpinnerValue else steps
        fromSpinner.model = SpinnerNumberModel(nFromSpinnerValue, 1, steps, 1)
        toSpinner.model = SpinnerNumberModel(nToSpinnerValue, 1, steps, 1)
        toSpinner.value = steps
        fromSpinner.value = nFromSpinnerValue
    }

    fun apply(zoom: Zoom) {
        fromSpinner.value = zoom.from
        toSpinner.value = zoom.to
    }

    fun toZoom() = Zoom(fromSpinner.value.toString().toInt(), toSpinner.value.toString().toInt())
}


@OptIn(ExperimentalTime::class)
fun generateDurationSelector(
    text: String,
    defaultDuration: Duration,
    defaultDurationSelector: DurationSelector,
    onNewSelected: (Duration, DurationSelector) -> Unit
): JPanel {
    onNewSelected(defaultDuration, defaultDurationSelector)
    return JPanel().apply {
        add(JLabel(text))
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
                JOptionPane.showMessageDialog(TrackerGui, "The max time frame is 31 days ore 1 month")
                return
            }
            if (jSpinner.value.toString().toLong() <= 0) {
                reverse()
                JOptionPane.showMessageDialog(TrackerGui, "The minimum value is 1!")
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
        val resetButton = JButton("Reset")
        resetButton.addActionListener {
            reverse()
            update()
        }
        add(resetButton)
    }
}

fun generateDurationSelectorComboBox(durations: Array<DurationSelector> = DurationSelector.values()): JComboBox<DurationSelector> {
    val combobox = JComboBox<DurationSelector>()
    durations.onEach { combobox.addItem(it) }
    return combobox
}