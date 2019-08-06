package info.spiralframework.base.common

data class EntryPair<K, V>(override val key: K, override val value: V): Map.Entry<K, V>