package de.dseelp.bacteriatracker

import java.math.BigInteger
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class BacteriaData(val steps: Int, val zoom: Zoom) {
    fun calculate(): MutableMap<Long, BigInteger> {
        val data = mutableMapOf<Long, BigInteger>()
        var currentValue = if (zoom.from == 1) BigInteger.ONE else BigInteger.valueOf(2).pow(zoom.from)
        var currentIndex: Long
        val twoValue = BigInteger.valueOf(2)
        if (zoom.from == 1) data[0] = BigInteger.ONE
        for (index in zoom.from.toLong()..zoom.to.toLong()) {
            currentIndex = index
            currentValue *= twoValue
            data[currentIndex] = currentValue
        }
        return data
    }

    companion object {
        fun generate(duration: Duration, interval: Duration): BacteriaData {
            val steps = (duration.inWholeMilliseconds / interval.inWholeMilliseconds).toInt()
            return BacteriaData(steps, Zoom(1, steps))
        }
    }
}

data class Zoom(val from: Int, val to: Int)
