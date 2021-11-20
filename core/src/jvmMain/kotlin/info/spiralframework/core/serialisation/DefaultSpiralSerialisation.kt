package info.spiralframework.core.serialisation

import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

class DefaultSpiralSerialisation: SpiralSerialisation {
    override val json: StringFormat = Json
}