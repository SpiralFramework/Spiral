package info.spiralframework.osl.games

import info.spiralframework.osl.OSLUnion

fun DRGameVisitor?.handleCltCode(builder: StringBuilder, code: String): Boolean =
        this?.handleCltCode(builder, code) == true
fun DRGameVisitor?.clearCltCode(builder: StringBuilder): Boolean =
        this?.clearCltCode(builder) == true

/**
 * Gets the entry for the name and arguments provided.
 * Returns OSLUnion.UndefinedType if this is null
 */
fun DRGameVisitor?.entryForName(name: String, arguments: IntArray): OSLUnion =
        this?.entryForName(name, arguments) ?: OSLUnion.UndefinedType

/**
 * Gets the entry for the op code and arguments provided.
 * Returns OSLUnion.UndefinedType if this is null
 */
fun DRGameVisitor?.entryForOpCode(opCode: Int, arguments: IntArray): OSLUnion =
        this?.entryForOpCode(opCode, arguments) ?: OSLUnion.UndefinedType