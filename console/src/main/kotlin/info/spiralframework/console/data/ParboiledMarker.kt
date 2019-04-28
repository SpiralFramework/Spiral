package info.spiralframework.console.data

sealed class ParboiledMarker {
    object SUCCESS_COMMAND: ParboiledMarker()
    object SUCCESS_BASE: ParboiledMarker()
    data class FAILED_LOCALE(val localeMsg: String, val params: Array<out Any>): ParboiledMarker() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FAILED_LOCALE) return false

            if (localeMsg != other.localeMsg) return false
            if (!params.contentEquals(other.params)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = localeMsg.hashCode()
            result = 31 * result + params.contentHashCode()
            return result
        }
    }
}