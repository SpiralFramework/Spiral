package org.abimon.spiral.core.formats.text

import com.fasterxml.jackson.databind.ObjectMapper
import org.abimon.spiral.core.data.SpiralData

object YAMLFormat: JacksonFormat() {
    override val name: String = "YAML"
    override val extension: String = "yaml"

    override val MAPPER: ObjectMapper
        get() = SpiralData.YAML_MAPPER
}