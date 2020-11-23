package info.spiralframework.console.jvm.pipeline

import dev.brella.knolus.KnolusFunctionBuilder
import dev.brella.knolus.ParameterSpec
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.getValue
import dev.brella.knolus.modules.functionregistry.functionBuilder
import dev.brella.knolus.modules.functionregistry.setFunctionWithContextWithoutReturn
import dev.brella.knolus.types.KnolusTypedValue

fun <P1, P2, P3, P4> KnolusContext.registerFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4) -> Unit
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, fourthParameterSpec, func)
        .build()
)

fun <P1, P2, P3, P4, P5> KnolusContext.registerFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    fifthParameterSpec: ParameterSpec<*, P5>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4, fifthParameter: P5) -> KnolusTypedValue
) = register(
    functionName,
    functionBuilder()
        .setFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, fourthParameterSpec, fifthParameterSpec, func)
        .build()
)


fun <T, P1, P2, P3, P4, P5> KnolusFunctionBuilder<T?>.setFunction(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    fifthParameterSpec: ParameterSpec<*, P5>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4, fifthParameter: P5) -> T?,
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)
    addParameter(fourthParameterSpec)
    addParameter(fifthParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()
        val fourthParam = parameters.getValue(context, fourthParameterSpec).get()
        val fifthParam = parameters.getValue(context, fifthParameterSpec).get()

        func(firstParam, secondParam, thirdParam, fourthParam, fifthParam)
    }
}

fun <P1, P2, P3, P4, P5, P6, P7> KnolusContext.registerFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    fifthParameterSpec: ParameterSpec<*, P5>,
    sixthParameterSpec: ParameterSpec<*, P6>,
    seventhParameterSpec: ParameterSpec<*, P7>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4, fifthParameter: P5, sixthParameter: P6, seventhParameter: P7) -> Unit
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(
            firstParameterSpec,
            secondParameterSpec,
            thirdParameterSpec,
            fourthParameterSpec,
            fifthParameterSpec,
            sixthParameterSpec,
            seventhParameterSpec,
            func
        ).build()
)

fun <T, P1, P2, P3, P4> KnolusFunctionBuilder<T?>.setFunctionWithContext(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4) -> T?,
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)
    addParameter(fourthParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()
        val fourthParam = parameters.getValue(context, fourthParameterSpec).get()

        func(context, firstParam, secondParam, thirdParam, fourthParam)
    }
}

fun <T, P1, P2, P3, P4> KnolusFunctionBuilder<T?>.setFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)
    addParameter(fourthParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()
        val fourthParam = parameters.getValue(context, fourthParameterSpec).get()

        func(context, firstParam, secondParam, thirdParam, fourthParam)

        null
    }
}

fun <T, P1, P2, P3, P4, P5, P6, P7> KnolusFunctionBuilder<T?>.setFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    fifthParameterSpec: ParameterSpec<*, P5>,
    sixthParameterSpec: ParameterSpec<*, P6>,
    seventhParameterSpec: ParameterSpec<*, P7>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4, fifthParameter: P5, sixthParameter: P6, seventhParameter: P7) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)
    addParameter(fourthParameterSpec)
    addParameter(fifthParameterSpec)
    addParameter(sixthParameterSpec)
    addParameter(seventhParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()
        val fourthParam = parameters.getValue(context, fourthParameterSpec).get()
        val fifthParam = parameters.getValue(context, fifthParameterSpec).get()
        val sixthParam = parameters.getValue(context, sixthParameterSpec).get()
        val seventhParam = parameters.getValue(context, seventhParameterSpec).get()

        func(context, firstParam, secondParam, thirdParam, fourthParam, fifthParam, sixthParam, seventhParam)

        null
    }
}

fun <P1, P2, P3, P4, P5, P6, P7> KnolusContext.registerFunctionWithAliasesWithContextWithoutReturn(
    vararg functionNames: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    fourthParameterSpec: ParameterSpec<*, P4>,
    fifthParameterSpec: ParameterSpec<*, P5>,
    sixthParameterSpec: ParameterSpec<*, P6>,
    seventhParameterSpec: ParameterSpec<*, P7>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3, fourthParameter: P4, fifthParameter: P5, sixthParameter: P6, seventhParameter: P7) -> Unit
) = functionNames.forEach { functionName ->
    register(
        functionName,
        functionBuilder()
            .setFunctionWithContextWithoutReturn(
                firstParameterSpec,
                secondParameterSpec,
                thirdParameterSpec,
                fourthParameterSpec,
                fifthParameterSpec,
                sixthParameterSpec,
                seventhParameterSpec,
                func
            ).build()
    )
}

fun KnolusContext.registerFunctionWithAliasesWithContextWithoutReturn(
    vararg functionNames: String,
    func: suspend (context: KnolusContext) -> Unit
) = functionNames.forEach { functionName ->
    register(
        functionName,
        functionBuilder()
            .setFunctionWithContextWithoutReturn(func)
            .build()
    )
}

fun <P1> KnolusContext.registerFunctionWithAliasesWithContextWithoutReturn(
    vararg functionNames: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, firstParameter: P1) -> Unit
) = functionNames.forEach { functionName ->
    register(
        functionName,
        functionBuilder()
            .setFunctionWithContextWithoutReturn(firstParameterSpec, func)
            .build()
    )
}


//fun <T> KnolusFunctionBuilder<T?>.setFunctionWithContextWithoutReturn(
//    func: suspend (context: KnolusContext) -> Unit
//): KnolusFunctionBuilder<T?> {
//    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> ->
//        func(context)
//
//        null
//    }
//}