package info.spiralframework.base.binding

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.random.Random

private const val UUID_CLEAR_VERSION: Byte = 0x0F
private const val UUID_SET_VERSION_4: Byte = 0x40
private const val UUID_CLEAR_VARIANT: Byte = 0x3F
private const val UUID_SET_VARIANT_IETF: Byte = 0x80.toByte()

public actual fun formatPercent(percentage: Double): String = percentage.asDynamic().toFixed(2) as String
@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")
public fun uuidString(): String {
    val ng = Random.Default

    val data = ByteArray(16)
    ng.nextBytes(data)
    data[6] = data[6] and UUID_CLEAR_VERSION  /* clear version        */
    data[6] = data[6] or  UUID_SET_VERSION_4  /* set to version 4     */
    data[8] = data[8] and UUID_CLEAR_VARIANT  /* clear variant        */
    data[8] = data[8] or  UUID_SET_VARIANT_IETF  /* set to IETF variant  */

    var msb: ULong = 0u
    var lsb: ULong = 0u
    for (i in 0 until 8)
        msb = msb shl 8 or (data[i].toULong() and 0xFFu)
    for (i in 8 until 16)
        lsb = lsb shl 8 or (data[i].toULong() and 0xFFu)

    return buildString {

        /**
        formatUnsignedLong0(lsb,        4, buf, 24, 12);
        formatUnsignedLong0(lsb >>> 48, 4, buf, 19, 4);
        formatUnsignedLong0(msb,        4, buf, 14, 4);
        formatUnsignedLong0(msb >>> 16, 4, buf, 9,  4);
        formatUnsignedLong0(msb >>> 32, 4, buf, 0,  8);
         */

        append(((msb shr 32) and 0xFFFFFFFFu).toString(16).padStart(8, '0'))
        append('-')
        append(((msb shr 16) and 0xFFFFu).toString(16).padStart(4, '0'))
        append('-')
        append(((msb shr 0) and 0xFFFFu).toString(16).padStart(4, '0'))
        append('-')
        append(((lsb shr 48) and 0xFFFFu).toString(16).padStart(4, '0'))
        append('-')
        append(((lsb shr 0) and 0xFFFFFFFFFFFFu).toString(16).padStart(12, '0'))
    }
}