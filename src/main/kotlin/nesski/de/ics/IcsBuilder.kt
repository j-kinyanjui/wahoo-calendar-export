package nesski.de.ics

import nesski.de.models.UserPlanItem
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val log = LoggerFactory.getLogger("IcsBuilder")

/**
 * Result of building an ICS calendar from workout plan items.
 *
 * @property icsContent The full VCALENDAR string (RFC 5545 compliant)
 * @property exportedCount Number of workouts successfully converted to VTODOs
 * @property skippedCount Number of workouts skipped due to bad/missing data
 * @property skippedReasons Details about why specific workouts were skipped
 */
data class IcsBuildResult(
    val icsContent: String,
    val exportedCount: Int,
    val skippedCount: Int,
    val skippedReasons: List<String> = emptyList()
)

/**
 * Builds RFC 5545-compliant VCALENDAR content with VTODO entries from
 * Wahoo SYSTM training plan items. Each workout becomes a VTODO task
 * compatible with Apple Reminders.
 *
 * Key ICS/VTODO decisions:
 * - DUE uses DATE-only format (e.g. `20260310`) for all-day tasks in Apple Reminders
 * - UID uses agendaId (falling back to workoutId or generated value)
 * - STATUS maps: "Completed" -> "COMPLETED", everything else -> "NEEDS-ACTION"
 * - SUMMARY includes sport emoji + workout name
 * - DESCRIPTION includes workout type and plan info
 */
object IcsBuilder {

    private const val PRODID = "-//WahooCLI//SYSTM Plan Export//EN"
    private const val VCALENDAR_VERSION = "2.0"
    private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * Build a complete VCALENDAR string from a list of [UserPlanItem]s.
     *
     * Workouts with missing plannedDate are skipped. Rest days (type == "Rest")
     * are skipped. Items with no prospect (no workout details) are skipped.
     *
     * @param items The workout plan items from the GraphQL API
     * @return An [IcsBuildResult] with the ICS content and export statistics
     */
    fun build(items: List<UserPlanItem>): IcsBuildResult {
        val vtodos = mutableListOf<String>()
        val skippedReasons = mutableListOf<String>()
        var skippedCount = 0

        val now = ZonedDateTime.now(java.time.ZoneOffset.UTC)
        val dtstamp = now.format(TIMESTAMP_FORMATTER)

        for (item in items) {
            // Skip rest days
            if (item.type?.equals("Rest", ignoreCase = true) == true) {
                log.debug("Skipping rest day: agendaId=${item.agendaId}")
                skippedCount++
                skippedReasons.add("Rest day skipped: agendaId=${item.agendaId ?: "unknown"}")
                continue
            }

            // Skip items with no planned date
            if (item.plannedDate.isNullOrBlank()) {
                log.warn("Skipping item with no planned date: agendaId=${item.agendaId}")
                skippedCount++
                skippedReasons.add("No planned date: agendaId=${item.agendaId ?: "unknown"}")
                continue
            }

            // Parse the date from ISO-8601 datetime
            val dueDate = try {
                LocalDate.parse(item.plannedDate.substringBefore("T"))
            } catch (e: Exception) {
                log.warn("Skipping item with unparseable date '${item.plannedDate}': ${e.message}")
                skippedCount++
                skippedReasons.add("Unparseable date '${item.plannedDate}': agendaId=${item.agendaId ?: "unknown"}")
                continue
            }

            val prospect = item.prospects?.firstOrNull()

            // Skip items with no workout details (no prospect and no type)
            if (prospect == null && item.type.isNullOrBlank()) {
                log.warn("Skipping item with no workout details: agendaId=${item.agendaId}")
                skippedCount++
                skippedReasons.add("No workout details: agendaId=${item.agendaId ?: "unknown"}")
                continue
            }

            val vtodo = buildVtodo(item, prospect, dueDate, dtstamp)
            vtodos.add(vtodo)
        }

        val icsContent = buildCalendar(vtodos)

        log.info("ICS build complete: ${vtodos.size} exported, $skippedCount skipped")

        return IcsBuildResult(
            icsContent = icsContent,
            exportedCount = vtodos.size,
            skippedCount = skippedCount,
            skippedReasons = skippedReasons
        )
    }

    /**
     * Build a single VTODO component string for one workout.
     */
    internal fun buildVtodo(
        item: UserPlanItem,
        prospect: nesski.de.models.Prospect?,
        dueDate: LocalDate,
        dtstamp: String
    ): String {
        val uid = resolveUid(item, prospect)
        val summary = formatSummary(item, prospect)
        val description = formatDescription(item, prospect)
        val status = mapStatus(item.status)
        val dueDateStr = dueDate.format(DATE_FORMATTER)

        return buildString {
            appendLine("BEGIN:VTODO")
            appendLine("UID:$uid")
            appendLine("DTSTAMP:$dtstamp")
            appendLine("DUE;VALUE=DATE:$dueDateStr")
            appendLine("SUMMARY:$summary")
            if (description.isNotBlank()) {
                appendLine("DESCRIPTION:${escapeIcsText(description)}")
            }
            appendLine("STATUS:$status")
            appendLine("END:VTODO")
        }.trimEnd()
    }

    /**
     * Wrap VTODO entries in a VCALENDAR envelope.
     */
    private fun buildCalendar(vtodos: List<String>): String {
        return buildString {
            appendLine("BEGIN:VCALENDAR")
            appendLine("VERSION:$VCALENDAR_VERSION")
            appendLine("PRODID:$PRODID")
            for (vtodo in vtodos) {
                appendLine(vtodo)
            }
            appendLine("END:VCALENDAR")
        }.trimEnd()
    }

    /**
     * Resolve a unique ID for the VTODO.
     * Priority: agendaId > prospect.workoutId > generated fallback.
     */
    private fun resolveUid(item: UserPlanItem, prospect: nesski.de.models.Prospect?): String {
        return item.agendaId
            ?: prospect?.workoutId
            ?: "wahoo-${item.plannedDate?.replace(Regex("[^0-9]"), "") ?: System.currentTimeMillis()}"
    }

    /**
     * Format the SUMMARY field with sport emoji and workout name.
     */
    internal fun formatSummary(item: UserPlanItem, prospect: nesski.de.models.Prospect?): String {
        val workoutName = prospect?.name ?: item.type ?: "Workout"
        val sportType = prospect?.type ?: prospect?.style ?: item.type
        val emoji = SportEmoji.forType(sportType)
        return "$emoji $workoutName"
    }

    /**
     * Format the DESCRIPTION field with workout type and plan info.
     */
    private fun formatDescription(item: UserPlanItem, prospect: nesski.de.models.Prospect?): String {
        val parts = mutableListOf<String>()

        val workoutType = prospect?.type ?: prospect?.style ?: item.type
        if (!workoutType.isNullOrBlank()) {
            parts.add("Type: $workoutType")
        }

        if (item.plan?.name != null) {
            parts.add("Plan: ${item.plan.name.trim()}")
        }

        val duration = prospect?.plannedDuration
        if (duration != null) {
            val totalMinutes = (duration * 60).toInt()
            parts.add("Duration: ${totalMinutes}min")
        }

        return parts.joinToString("\\n")
    }

    /**
     * Map Wahoo status strings to ICS VTODO STATUS values.
     */
    private fun mapStatus(status: String?): String {
        return when (status?.lowercase()) {
            "completed" -> "COMPLETED"
            else -> "NEEDS-ACTION"
        }
    }

    /**
     * Escape special characters in ICS text fields per RFC 5545 Section 3.3.11.
     */
    private fun escapeIcsText(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
    }
}
