package info.spiralframework.console.data

sealed class ParboiledMarker {
    object SUCCESS_COMMAND: ParboiledMarker()
    object SUCCESS_BASE: ParboiledMarker()
    data class FAILED_LOCALE(val localeMsg: String): ParboiledMarker()
}