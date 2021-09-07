package de.dseelp.bacteriatracker

import java.math.BigInteger
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object DataUtil {
    fun calculateSteps(duration: Duration, interval: Duration): Int = (duration.inWholeMilliseconds / interval.inWholeMilliseconds).toInt()
}