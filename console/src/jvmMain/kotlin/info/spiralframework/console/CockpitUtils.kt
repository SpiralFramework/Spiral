package info.spiralframework.console

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.events.post
import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.console.eventbus.RegisterCommandRequest
import info.spiralframework.console.eventbus.UnregisterCommandRequest
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

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