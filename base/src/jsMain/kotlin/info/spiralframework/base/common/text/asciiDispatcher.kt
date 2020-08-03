package info.spiralframework.base.common.text

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun asciiDispatcher(): CoroutineDispatcher = Dispatchers.Default