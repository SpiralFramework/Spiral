package info.spiralframework.base.common.serialisation

import dev.brella.kornea.errors.common.KorneaResult
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

interface SpiralSerialisation {
    object NoOp : SpiralSerialisation {
        override val jsonSerialisersModule: SerializersModule = EmptySerializersModule

        override fun <T> decodeFromJsonString(deserialiser: DeserializationStrategy<T>, string: String): KorneaResult<T> =
            KorneaResult.empty()

        override fun <T> encodeToJsonString(serialiser: SerializationStrategy<T>, value: T): KorneaResult<String> =
            KorneaResult.empty()
    }

    val jsonSerialisersModule: SerializersModule
    public fun <T> encodeToJsonString(serialiser: SerializationStrategy<T>, value: T): KorneaResult<String>
    public fun <T> decodeFromJsonString(deserialiser: DeserializationStrategy<T>, string: String): KorneaResult<T>
}

public inline fun <reified T> SpiralSerialisation.encodeToJsonString(value: T): KorneaResult<String> =
    encodeToJsonString(jsonSerialisersModule.serializer(), value)

public inline fun <reified T> SpiralSerialisation.decodeFromJsonString(string: String): KorneaResult<T> =
    decodeFromJsonString(jsonSerialisersModule.serializer(), string)