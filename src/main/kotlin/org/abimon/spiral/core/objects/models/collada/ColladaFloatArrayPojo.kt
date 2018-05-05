package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

@JsonRootName("float_array")
class ColladaFloatArrayPojo (
        @JacksonXmlProperty(isAttribute = true)
        val count: Int,
        @JacksonXmlProperty(isAttribute = true)
        val id: String?,
        @JacksonXmlProperty(isAttribute = true)
        val name: String?,
        @JacksonXmlProperty(isAttribute = true)
        val digits: Byte = 6,
        @JacksonXmlProperty(isAttribute = true)
        val magnitude: Short = 38
) {
    @JacksonXmlText
    private var value: String? = null

    var floatArrayValue: FloatArray
        @JsonIgnore
        get() = value?.split(' ')?.map(String::toFloat)?.toFloatArray() ?: floatArrayOf()
        set(floatValue) {
            value = floatValue.joinToString(" ", transform = Float::toString)
        }

    override fun toString(): String = "ColladaFloatArrayPojo(count=$count, id=$id, name=$name, digits=$digits, magnitude=$magnitude, value=${floatArrayValue.joinToString()})"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColladaFloatArrayPojo) return false

        if (count != other.count) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (digits != other.digits) return false
        if (magnitude != other.magnitude) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + digits
        result = 31 * result + magnitude
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }


}