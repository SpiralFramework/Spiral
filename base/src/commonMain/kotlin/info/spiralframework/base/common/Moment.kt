package info.spiralframework.base.common

data class Moment(val year: Int, val month: Int, val day: Int, val hour: Int, val minute: Int, val second: Int, val millisecond: Int) {
    companion object {}

    override fun toString(): String = buildString {
        append(year.toString().padStart(4, '0'))
        append('/')
        append(month.toString().padStart(2, '0'))
        append('/')
        append(day.toString().padStart(2, '0'))

        append(' ')

        append(hour.toString().padStart(2, '0'))
        append(':')
        append(minute.toString().padStart(2, '0'))
        append(':')
        append(second.toString().padStart(2, '0'))
        append('.')
        append(millisecond.toString().padStart(3, '0'))
    }

    init {
        require(month in 1..12)
        require(day in 1..31)
        require(hour in 0..59)
        require(minute in 0..59)
        require(second in 0..59)
        require(millisecond in 0..999)
    }
}