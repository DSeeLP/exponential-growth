package de.dseelp.bacteriatracker

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
enum class DurationSelector(val durationName: String, val maxValue: Duration, val convertor: (Long) -> Duration, val reverser: (Duration) -> Long) {
    Minutes("Minute", Duration.days(31), { Duration.minutes(it) }, { it.inWholeMinutes }),
    Hours("Hour", Duration.days(31), { Duration.hours(it) }, { it.inWholeHours }),
    Days("Day", Duration.days(31), { Duration.days(it) }, { it.inWholeDays });

    fun convert(long: Long): Duration = convertor(long)
    fun reverse(duration: Duration): Long = reverser(duration)

    override fun toString(): String = name
}