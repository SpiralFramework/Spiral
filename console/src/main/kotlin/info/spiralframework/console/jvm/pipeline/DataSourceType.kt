package info.spiralframework.console.jvm.pipeline

import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.io.common.DataSource

class DataSourceType(val inner: DataSource<*>): KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<DataSourceType> {
        override val typeHierarchicalNames: Array<String> = arrayOf("DataSource", "Object")
        override fun asInstance(instance: Any?): DataSourceType = instance as DataSourceType
        override fun asInstanceSafe(instance: Any?): DataSourceType? = instance as? DataSourceType
        override fun isInstance(instance: Any?): Boolean = instance is DataSourceType
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<*>
        get() = TypeInfo
}