package info.spiralframework.base

import info.spiralframework.base.common.SemanticVersion

interface SpiralModuleProvider {
    val moduleName: String
    val moduleVersion: SemanticVersion
}