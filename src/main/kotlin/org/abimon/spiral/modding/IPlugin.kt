package org.abimon.spiral.modding

import org.abimon.imperator.handle.Imperator

interface IPlugin {
    fun enable(imperator: Imperator)
    fun disable(imperator: Imperator)
}