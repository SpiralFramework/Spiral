package org.abimon.spiral.core.formats.text

import com.fasterxml.jackson.databind.ObjectMapper
import org.abimon.spiral.core.data.SpiralData

object JSONFormat: JacksonFormat() {
    override val name: String = "JSON"
    override val extension: String = "json"

    override val MAPPER: ObjectMapper
        get() = SpiralData.MAPPER
}