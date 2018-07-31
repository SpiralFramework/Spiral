package org.abimon.osl

import org.parboiled.Action
import org.parboiled.Context
import org.parboiled.support.Var

fun <T, V> Var<T>.runWith(op: (T) -> V): Action<Any> =
        Action {
            op(get())
            return@Action true
        }

fun contextFunc(func: () -> Boolean): ((Context<Any>) -> Boolean) = { func() }