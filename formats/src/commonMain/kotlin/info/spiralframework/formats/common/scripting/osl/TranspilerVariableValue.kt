package info.spiralframework.formats.common.scripting.osl

interface TranspilerVariableValue {
    fun represent(): String = StringBuilder().also(this::represent).toString()
    fun represent(builder: StringBuilder)
}

inline class StringValue(val string: String): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append('"')
        builder.append(string)
        builder.append('"')
    }
}

inline class RawNumberValue(val number: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append(number)
    }
}

inline class Int16LEValue(val int16: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        val num = int16.toInt()
        builder.append("int16LE(${num and 0xFF}, ${num shr 8})")
    }
}

inline class Int16BEValue(val int16: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        val num = int16.toInt()
        builder.append("int16BE(${num and 0xFF}, ${num shr 8})")
    }
}

inline class FlagIDValue(val flagID: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        val num = flagID.toInt()
        builder.append("flagID(${num and 0xFF}, ${num shr 8})")
    }
}

inline class BooleanValue(val boolean: Boolean): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append(boolean)
    }
}

inline class LabelValue(val label: String): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append("@{")
        builder.append(label)
        builder.append('}')
    }
}

inline class ParameterValue(val parameter: String): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append("%{")
        builder.append(parameter)
        builder.append('}')
    }
}