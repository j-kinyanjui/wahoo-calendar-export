package nesski.de.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nesski.de.plugins.AnySerializer

/**
 * Response wrapper for GetUserPlansRange GraphQL query
 */
@Serializable
data class GetUserPlansRangeResponse(
    @SerialName("userPlansRange")
    val userPlansRange: UserPlansData? = null
)

/**
 * Data container for user plans range query
 */
@Serializable
data class UserPlansData(
    @SerialName("plans")
    val plans: List<Plan> = emptyList()
)

/**
 * Represents a Systm training plan
 */
@Serializable
data class Plan(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("scheduledDate")
    val scheduledDate: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("workouts")
    val workouts: List<Workout> = emptyList()
)

/**
 * Represents a single workout within a plan
 */
@Serializable
data class Workout(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("scheduledDate")
    val scheduledDate: String? = null,
    @SerialName("duration")
    val duration: Int? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("status")
    val status: String? = null
)

/**
 * Plan status values from Systm API
 */
@Serializable
enum class PlanStatus {
    @SerialName("scheduled")
    SCHEDULED,
    @SerialName("in_progress")
    IN_PROGRESS,
    @SerialName("completed")
    COMPLETED,
    @SerialName("cancelled")
    CANCELLED
}

/**
 * Plan type values from Systm API
 */
@Serializable
enum class PlanType {
    @SerialName("training")
    TRAINING,
    @SerialName("race")
    RACE,
    @SerialName("recovery")
    RECOVERY
}

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

