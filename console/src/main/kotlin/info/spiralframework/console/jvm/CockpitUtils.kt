package info.spiralframework.console.jvm

import dev.brella.kornea.errors.common.KorneaResult
import info.spiralframework.core.formats.FormatResult

//suspend fun <T: ParboiledCommand> SpiralEventBus.registerCommandClassViaRequest(context: SpiralContext, commandRegistry: Any, registerSubclass: KClass<in T> = ParboiledCommand::class) {
//    commandRegistry.javaClass.kotlin.memberProperties.forEach { recruit ->
//        @Suppress("UNCHECKED_CAST")
//        if((recruit.returnType.classifier as? KClass<*>)?.isSubclassOf(registerSubclass) == true || recruit.returnType.classifier == registerSubclass)
//            post(context, RegisterCommandRequest(recruit.get(commandRegistry) as T))
//    }
//}
//
//suspend fun <T: ParboiledCommand> SpiralEventBus.unregisterCommandClassViaRequest(context: SpiralContext, commandRegistry: Any, registerSubclass: KClass<in T> = ParboiledCommand::class) {
//    commandRegistry.javaClass.kotlin.memberProperties.forEach { recruit ->
//        @Suppress("UNCHECKED_CAST")
//        if((recruit.returnType.classifier as? KClass<*>)?.isSubclassOf(registerSubclass) == true || recruit.returnType.classifier == registerSubclass)
//            post(context, UnregisterCommandRequest(recruit.get(commandRegistry) as T))
//    }
//}

public inline fun <T, R: KorneaResult<*>> Iterable<T>.mapResults(transform: (T) -> R): List<R> {
    return mapResultsTo(ArrayList<R>(10), transform)
}

public inline fun <T, R: KorneaResult<*>, C : MutableCollection<in R>> Iterable<T>.mapResultsTo(destination: C, transform: (T) -> R): C {
    for (item in this) {
        val transformed = transform(item)
        destination.add(transformed)
        if (transformed is FormatResult<*, *> && transformed.confidence() >= 0.99) break
    }
    return destination
}