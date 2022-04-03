package info.spiralframework.base.common.serialisation

import dev.brella.kornea.errors.common.KorneaResult
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

public class DefaultSpiralSerialisation(public val json: Json = Json.Default): SpiralSerialisation {
    override val jsonSerialisersModule: SerializersModule
        get() = json.serializersModule

    override fun <T> encodeToJsonString(serialiser: SerializationStrategy<T>, value: T): KorneaResult<String> =
        KorneaResult.successOrCatch { json.encodeToString(serialiser, value) }

    override fun <T> decodeFromJsonString(deserialiser: DeserializationStrategy<T>, string: String): KorneaResult<T> =
        KorneaResult.successOrCatch { json.decodeFromString(deserialiser, string) }
}