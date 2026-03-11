package nesski.de.cli

import java.time.LocalDate

sealed class Range {
    object Now : Range()
    data class Weeks(val weeks: Int) : Range()
    data class Months(val months: Int) : Range()

    companion object {
        fun parse(input: String): Range {
            return when {
                input.lowercase() == "now" -> Now
                input.endsWith("w") -> {
                    val number = input.dropLast(1).toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid weeks: $input")
                    if (number !in 1..8) throw IllegalArgumentException("Weeks must be 1-8")
                    Weeks(number)
                }
                input.endsWith("m") -> {
                    val number = input.dropLast(1).toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid months: $input")
                    if (number !in 1..2) throw IllegalArgumentException("Months must be 1-2")
                    Months(number)
                }
                else -> throw IllegalArgumentException("Invalid date range: $input")
            }
        }
    }
}

fun parseDate(input: String?): LocalDate? {
    return input?.let { LocalDate.parse(it) }
}