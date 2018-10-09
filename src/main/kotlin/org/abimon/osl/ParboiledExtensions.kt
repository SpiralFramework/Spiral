package org.abimon.osl

import org.abimon.osl.drills.StaticDrill
import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Context
import org.parboiled.Rule
import org.parboiled.support.Var

fun <T, V> Var<T>.runWith(op: (T) -> V): Action<Any> =
        Action {
            op(get())
            return@Action true
        }

fun contextFunc(func: () -> Boolean): ((Context<Any>) -> Boolean) = { func() }

fun OSLRule(func: OpenSpiralLanguageParser.() -> Rule): OpenSpiralLanguageParser.() -> Rule = func

inline fun <reified T: Any> BaseParser<Any>.pushStaticDrill(value: T): Action<Any> = Action { push(listOf(SpiralDrillBit(StaticDrill(value)))) }
inline fun <reified T: Any> BaseParser<Any>.pushStaticDrillDirect(value: T) = push(listOf(SpiralDrillBit(StaticDrill(value))))