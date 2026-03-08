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
 */
@Serializable
data class UserPlanItem(
    /** Day number within the plan (1-based). */
    val day: Int? = null,

    /** ISO-8601 date string (e.g. "2026-03-10T00:00:00.000Z"). */
    val plannedDate: String? = null,

    /** Ordering rank when multiple items share the same day. */
    val rank: Int? = null,

    /** Internal agenda row ID. */
    val agendaId: String? = null,

    /** Status of this agenda entry (e.g. "planned", "completed"). */
    val status: String? = null,

    /** High-level type tag (e.g. "workout", "rest"). */
    val type: String? = null,

    /** The timezone that was applied to date calculations. */
    val appliedTimeZone: String? = null,

    /** Wahoo device workout ID, if synced. */
    val wahooWorkoutId: String? = null,

    /** Data about a linked completed activity, if any. */
    val completionData: CompletionData? = null,

    /** Candidate workouts for this slot (usually exactly one). */
    val prospects: List<Prospect>? = null,

    /** The plan this item belongs to. */
    val plan: PlanInfo? = null,

    /** Data about a manually linked activity. */
    val linkData: LinkData? = null
)

// ── Prospect (the actual workout details) ───────────────────────────

/**
 * A prospect represents a workout that can fill a plan slot.
 */
@Serializable
data class Prospect(
    /** Workout type (e.g. "cycling", "strength"). */
    val type: String? = null,

    /** Display name of the workout. */
    val name: String? = null,

    /** How well this workout matches the athlete's profile. */
    val compatibility: String? = null,

    /** Short description of the workout. */
    val description: String? = null,

    /** Activity style (e.g. "cycling", "running"). */
    val style: String? = null,

    /** Power/intensity targets. */
    val intensity: Intensity? = null,

    /** Indoor trainer configuration. */
    val trainerSetting: TrainerSetting? = null,

    /** Planned duration in hours (fractional). */
    val plannedDuration: Double? = null,

    /** How the duration is measured (e.g. "time", "distance"). */
    val durationType: String? = null,

    /** Performance metrics / ratings. */
    val metrics: Metrics? = null,

    /** SYSTM content library ID. */
    val contentId: String? = null,

    /** Workout ID within the plan. */
    val workoutId: String? = null,

    /** Coach notes for this workout. */
    val notes: String? = null,

    /** 4DP workout graph data points. */
    val fourDPWorkoutGraph: List<WorkoutGraphPoint>? = null
)

// ── Intensity ───────────────────────────────────────────────────────

@Serializable
data class Intensity(
    val master: Double? = null,
    val nm: Double? = null,
    val ac: Double? = null,
    val map: Double? = null,
    val ftp: Double? = null
)

// ── TrainerSetting ──────────────────────────────────────────────────

@Serializable
data class TrainerSetting(
    val mode: String? = null,
    val level: Int? = null
)

// ── Metrics ─────────────────────────────────────────────────────────

@Serializable
data class Metrics(
    val ratings: Ratings? = null
)

@Serializable
data class Ratings(
    val nm: Double? = null,
    val ac: Double? = null,
    val map: Double? = null,
    val ftp: Double? = null
)

// ── WorkoutGraphPoint ───────────────────────────────────────────────

@Serializable
data class WorkoutGraphPoint(
    val time: Double? = null,
    val value: Double? = null,
    val type: String? = null
)

// ── CompletionData ──────────────────────────────────────────────────

/**
 * Data about the activity that completed this plan slot.
 */
@Serializable
data class CompletionData(
    val name: String? = null,
    val date: String? = null,
    val activityId: String? = null,
    val durationSeconds: Int? = null,
    val style: String? = null,
    val deleted: Boolean? = null
)

// ── LinkData ────────────────────────────────────────────────────────

/**
 * Data about a manually linked activity.
 */
@Serializable
data class LinkData(
    val name: String? = null,
    val date: String? = null,
    val activityId: String? = null,
    val durationSeconds: Int? = null,
    val style: String? = null,
    val deleted: Boolean? = null
)

// ── PlanInfo (metadata about the plan an item belongs to) ───────────

/**
 * Metadata about the training plan.
 */
@Serializable
data class PlanInfo(
    val id: String? = null,
    val name: String? = null,
    val color: String? = null,
    val deleted: Boolean? = null,
    val durationDays: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val addons: String? = null,
    val level: String? = null,
    val subcategory: String? = null,
    val weakness: String? = null,
    val description: String? = null,
    val category: String? = null,
    val grouping: String? = null,
    val option: String? = null,
    val uniqueToPlan: Boolean? = null,
    val type: String? = null,
    val progression: String? = null,
    val planDescription: String? = null,
    val volume: String? = null
)
