package nesski.de.models

import kotlinx.serialization.Serializable
import nesski.de.utils.InstantSerializer
import java.time.Instant

@Serializable
data class WahooWorkouts(
    val total: Int,
    val page: Int,
    val order: String,
    val sort: String,
    val workouts: List<Workouts>,
)

@Serializable
data class Workouts(
    val id: Int,
    @Serializable(InstantSerializer::class)
    val starts: Instant,
    val minutes: Int,
    val name: String,
    val plan_id: Int,
    val workout_token: String,
    val workout_type_id: Int,
    @Serializable(InstantSerializer::class)
    val created_at: Instant,
    @Serializable(InstantSerializer::class)
    val updated_at: Instant,
    // val workout_summary: null,
)
