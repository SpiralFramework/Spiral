package info.spiralframework.gui.jvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

public suspend inline fun <T> javafx(noinline block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.JavaFx, block)
public suspend inline fun <T> javaFX(noinline block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.JavaFx, block)