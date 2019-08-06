package info.spiralframework.console

import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.console.eventbus.RegisterCommandRequest
import info.spiralframework.console.eventbus.UnregisterCommandRequest
import org.greenrobot.eventbus.EventBus
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

fun <T: ParboiledCommand> EventBus.registerCommandClassViaRequest(commandRegistry: Any, registerSubclass: KClass<in T> = ParboiledCommand::class) {
    commandRegistry.javaClass.kotlin.memberProperties.forEach { recruit ->
        @Suppress("UNCHECKED_CAST")
        if((recruit.returnType.classifier as? KClass<*>)?.isSubclassOf(registerSubclass) == true || recruit.returnType.classifier == registerSubclass)
            post(RegisterCommandRequest(recruit.get(commandRegistry) as T))
    }
}

fun <T: ParboiledCommand> EventBus.unregisterCommandClassViaRequest(commandRegistry: Any, registerSubclass: KClass<in T> = ParboiledCommand::class) {
    commandRegistry.javaClass.kotlin.memberProperties.forEach { recruit ->
        @Suppress("UNCHECKED_CAST")
        if((recruit.returnType.classifier as? KClass<*>)?.isSubclassOf(registerSubclass) == true || recruit.returnType.classifier == registerSubclass)
            post(UnregisterCommandRequest(recruit.get(commandRegistry) as T))
    }
}