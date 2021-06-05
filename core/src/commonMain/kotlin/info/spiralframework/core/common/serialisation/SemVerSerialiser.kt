package info.spiralframework.core.common.serialisation

import dev.brella.kornea.toolkit.common.SemanticVersion
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SemVerSerialiser: KSerializer<SemanticVersion> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SemanticVersion", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SemanticVersion) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): SemanticVersion =
        SemanticVersion.fromString(decoder.decodeString())
}