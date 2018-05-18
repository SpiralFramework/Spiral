package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import java.awt.Color

@JsonRootName("color")
data class ColladaColorPojo(
        @JacksonXmlProperty(isAttribute = true)
        val sid: String? = null
) {
    companion object {
        val VALID_RANGE = 3..4
    }

    private val value: String
        @JacksonXmlText
        get() {
            if (colourValues.size !in VALID_RANGE)
                return "0 0 0"
            return colourValues.joinToString(" ")
        }

    @JsonIgnore
    val colourValues: MutableList<Float> = ArrayList(listOf(0f, 0f, 0f))

    var lightingColour: Color
        @JsonIgnore
        get() = colourValues.let { list ->
            if (colourValues.size !in VALID_RANGE)
                return@let Color.BLACK

            return@let Color(list[0], list[1], list[2])
        }
        @JsonIgnore
        set(value) {
            colourValues.clear()
            colourValues.add(value.red / 255.0f)
            colourValues.add(value.green / 255.0f)
            colourValues.add(value.blue / 255.0f)
        }

    var profileColour: Color
        @JsonIgnore
        get() = colourValues.let { list ->
            if (colourValues.size !in VALID_RANGE)
                return@let Color.BLACK

            return@let Color(list[0], list[1], list[2], if (list.size == 3) 1.0f else list[3])
        }
        @JsonIgnore
        set(value) {
            colourValues.clear()
            colourValues.add(value.red / 255.0f)
            colourValues.add(value.green / 255.0f)
            colourValues.add(value.blue / 255.0f)
            colourValues.add(value.alpha / 255.0f)
        }
}