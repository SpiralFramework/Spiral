package info.spiralframework.base.common.text

import java.text.DecimalFormat

private val PERCENT_FORMAT = DecimalFormat("00.00")

actual fun formatPercent(percentage: Double): String = PERCENT_FORMAT.format(percentage)