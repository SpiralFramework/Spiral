package info.spiralframework.base.common.text

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual fun asciiDispatcher(): CoroutineDispatcher =
    Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "Ascii-Dispatcher") }.asCoroutineDispatcher()