package info.spiralframework.formats.common.scripting.osl

public interface TranspilerVariableValue {
    public fun represent(): String = StringBuilder().also(this::represent).toString()
    public fun represent(builder: StringBuilder)
}

public class StringValue(public val string: String): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append('"')
        builder.append(string)
        builder.append('"')
    }
}

public class RawNumberValue(public val number: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append(number)
    }
}

public class Int16LEValue(public val int16: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        val num = int16.toInt()
        builder.append("int16LE(${num and 0xFF}, ${num shr 8})")
    }
}

public class Int16BEValue(public val int16: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        val num = int16.toInt()
        builder.append("int16BE(${num and 0xFF}, ${num shr 8})")
    }
}

public class FlagIDValue(public val flagID: Number): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        val num = flagID.toInt()
        builder.append("flagID(${num and 0xFF}, ${num shr 8})")
    }
}

public class BooleanValue(public val boolean: Boolean): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append(boolean)
    }
}

public class LabelValue(public val label: String): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append("@{")
        builder.append(label)
        builder.append('}')
    }
}

public class ParameterValue(public val parameter: String): TranspilerVariableValue {
    override fun represent(builder: StringBuilder) {
        builder.append("%{")
        builder.append(parameter)
        builder.append('}')
    }
}