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

inline class NumberValue(val number: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append(number)
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