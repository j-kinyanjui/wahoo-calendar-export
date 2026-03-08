package nesski.de.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nesski.de.utils.AnySerializer

// ── GraphQL infrastructure ──────────────────────────────────────────

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, @Serializable(with = AnySerializer::class) Any>? = null,
    val operationName: String? = null
)

@Serializable
data class GraphQLError(
    val message: String,
    val locations: List<GraphQLLocation>? = null,
    val path: List<String>? = null
)

@Serializable
data class GraphQLLocation(
    val line: Int,
    val column: Int
)

@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

// ── GetUserPlansRange response ──────────────────────────────────────

/**
 * Top-level data wrapper for the `userPlan` query.
 * The query returns `{ data: { userPlan: [ ... ] } }`.
 */
@Serializable
data class GetUserPlansRangeResponse(
    @SerialName("userPlan")
    val userPlan: List<UserPlanItem>? = null
)

// ── UserPlanItem (one row per day/workout slot) ─────────────────────

/**
 * A single item from the user's training agenda.
 * Each item represents one planned workout slot on a specific day.
 *
 */
@Serializable
data class UserPlanItem(
    /** ISO-8601 date string (e.g. "2026-03-10T00:00:00.000Z"). Maps to VTODO DUE. */
    val plannedDate: String? = null,

    /** Internal agenda row ID. Maps to VTODO UID. */
    val agendaId: String? = null,

    /** Status of this agenda entry (e.g. "planned", "completed"). Maps to VTODO STATUS. */
    val status: String? = null,

    /** High-level type tag (e.g. "workout", "rest"). Used to filter out rest days. */
    val type: String? = null,

    /** Candidate workouts for this slot (usually exactly one). */
    val prospects: List<Prospect>? = null,

    /** The plan this item belongs to. */
    val plan: PlanInfo? = null
)

/**
 * A prospect represents a workout that can fill a plan slot.
 *
 * Trimmed to fields needed for VTODO SUMMARY, emoji mapping, and duration display.
 */
@Serializable
data class Prospect(
    /** Workout type (e.g. "Cycling", "Yoga", "Strength"). Primary field for sport emoji mapping. */
    val type: String? = null,

    /** Display name of the workout. Maps to VTODO SUMMARY. */
    val name: String? = null,

    /** Activity style — often null in real API responses. Fallback for emoji mapping. */
    val style: String? = null,

    /** Planned duration in fractional hours (e.g. 0.601 = ~36 min). */
    val plannedDuration: Double? = null,

    /** Workout ID within the plan. Fallback for VTODO UID. */
    val workoutId: String? = null
)

/**
 * Metadata about the training plan.
 *
 * Trimmed to fields needed for CLI display grouping and email body.
 */
@Serializable
data class PlanInfo(
    val id: String? = null,
    val name: String? = null,
    val level: String? = null
)
