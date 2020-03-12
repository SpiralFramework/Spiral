package info.spiralframework.console.jvm

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