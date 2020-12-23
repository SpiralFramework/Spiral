package info.spiralframework.core.common.formats.images

import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.defaultEquals
import info.spiralframework.base.common.properties.defaultHashCode
import info.spiralframework.core.common.formats.WritableSpiralFormat

object PreferredImageFormat: ISpiralProperty.PropertyKey<WritableSpiralFormat>, KorneaTypeChecker<WritableSpiralFormat> by KorneaTypeChecker.ClassBased() {
    override val name: String = "PreferredImageFormat"

    override fun hashCode(): Int = defaultHashCode()
    override fun equals(other: Any?): Boolean = defaultEquals(other)
}