package com.lolha.learningapp.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

object AvailabilityValidator {
    private val allowedWeekdays = setOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun validateWeeklyRule(weekday: String, startTime: String, endTime: String): String? {
        if (weekday.trim() !in allowedWeekdays) {
            return "Day must be Mon, Tue, Wed, Thu, Fri, Sat, or Sun."
        }
        return validateTimeRange(startTime, endTime)
    }

    fun validateException(date: String, startTime: String, endTime: String): String? {
        if (parseDate(date.trim()) == null) {
            return "Date must use YYYY-MM-DD format."
        }
        return validateTimeRange(startTime, endTime)
    }

    private fun validateTimeRange(startTime: String, endTime: String): String? {
        val start = parseTime(startTime.trim()) ?: return "Start time must use HH:mm format."
        val end = parseTime(endTime.trim()) ?: return "End time must use HH:mm format."
        if (!end.isAfter(start)) {
            return "End time must be later than start time."
        }
        return null
    }

    private fun parseTime(value: String): LocalTime? =
        try {
            LocalTime.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }

    private fun parseDate(value: String): LocalDate? =
        try {
            LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
}

