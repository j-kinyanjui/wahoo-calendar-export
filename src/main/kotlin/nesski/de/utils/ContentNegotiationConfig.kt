package nesski.de.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Serializer for Any type to handle GraphQL variables
 */
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is String -> encoder.encodeString(value)
            is Number -> encoder.encodeDouble(value.toDouble())
            is Boolean -> encoder.encodeBoolean(value)
            is Map<*, *> -> {
                val jsonObject = buildJsonObject {
                    value.forEach { (k, v) ->
                        when (v) {
                            is String -> put(k.toString(), v)
                            is Number -> put(k.toString(), v)
                            is Boolean -> put(k.toString(), v)
                            else -> put(k.toString(), v.toString())
                        }
                    }
                }
                (encoder as JsonEncoder).encodeJsonElement(jsonObject)
            }
            else -> encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        return when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> {
                element.content
            }
            is JsonObject -> {
                // You can parse JsonObject into a Map or some other object
                // If you just want the string representation of the object, do this:
                element.toString()
                // Or if you need a map representation:
                element.entries.associate { it.key to it.value }
            }
            else -> element.toString() // Default case
        }
    }
}