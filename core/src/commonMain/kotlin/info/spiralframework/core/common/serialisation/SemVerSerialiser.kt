package info.spiralframework.core.common.serialisation

import dev.brella.kornea.toolkit.common.SemanticVersion
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object SemVerSerialiser: KSerializer<SemanticVersion> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SemanticVersion", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SemanticVersion): Unit =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): SemanticVersion =
        fromString(decoder.decodeString())

    private val regex: Regex by lazy { SemanticVersion.SEMVER_REGEX.toRegex() }

    public fun fromString(string: String): SemanticVersion {
        val match = requireNotNull(regex.matchEntire(string)) {
            "Version string is not a valid semantic version: $string"
        }.groupValues

        return SemanticVersion(match[1].toInt(), match[2].toInt(), match[3].toInt(), match.getOrNull(4)?.takeIf(String::isNotBlank)?.let(SemanticVersion.ReleaseCycle.Companion::fromRepresentation))
    }
}