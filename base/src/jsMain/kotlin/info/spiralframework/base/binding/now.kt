package info.spiralframework.base.binding

import info.spiralframework.base.common.Moment
import kotlin.js.Date

public operator fun Moment.Companion.invoke(date: Date): Moment = Moment(date.getFullYear(), date.getMonth(), date.getDay(), date.getHours(), date.getMinutes(), date.getSeconds(), date.getMilliseconds())
public actual fun Moment.Companion.now(): Moment = Moment(Date())