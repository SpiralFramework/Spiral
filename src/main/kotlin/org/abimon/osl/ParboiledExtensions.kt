package org.abimon.osl

import org.parboiled.Action
import org.parboiled.support.Var

fun <T, V> Var<T>.runWith(op: (T) -> V): Action<Any> =
        Action {
            op(get())
            return@Action true
        }