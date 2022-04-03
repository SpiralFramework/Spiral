package info.spiralframework.base.binding

import info.spiralframework.base.common.Moment
import java.time.LocalDateTime

public operator fun Moment.Companion.invoke(ldt: LocalDateTime): Moment = Moment(ldt.year, ldt.monthValue, ldt.dayOfMonth, ldt.hour, ldt.minute, ldt.second, 0)
public actual fun Moment.Companion.now(): Moment = Moment(LocalDateTime.now())