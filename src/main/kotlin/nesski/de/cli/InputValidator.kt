package nesski.de.cli

import java.io.File
import java.time.LocalDate

sealed class Range {
    data object Now : Range()

    data class Weeks(val weeks: Int) : Range()

    data class Months(val months: Int) : Range()

    companion object {
        fun parse(input: String): Range {
            return when {
                input.lowercase() == "now" -> Now
                input.endsWith("w") -> {
                    val number =
                        input.dropLast(1).toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid weeks: $input")
                    if (number !in 1..8) error("Weeks must be 1-8")
                    Weeks(number)
                }
                input.endsWith("m") -> {
                    val number =
                        input.dropLast(1).toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid months: $input")
                    if (number !in 1..2) error("Months must be 1-2")
                    Months(number)
                }
                else -> error("Invalid date range: $input")
            }
        }
    }
}

fun parseDate(input: String?): LocalDate? {
    return input?.let { LocalDate.parse(it) }
}

fun configDir(): File {
    val xdg = System.getenv("XDG_CONFIG_HOME")
    val base = xdg?.let { File(it) } ?: File(System.getProperty("user.home"), ".config")

    return File(base, "wahoo_sync")
}

fun defaultConfigFile(): File {
    val dir = configDir()
    val file = File(dir, "config.toml")

    if (!file.exists()) {
        dir.mkdirs()
        val stream =
            object {}.javaClass.getResourceAsStream("/config.toml")
                ?: error("config.toml not found in resources")
        file.outputStream().use { stream.copyTo(it) }
    }
    return file
}
