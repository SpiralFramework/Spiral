package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
/**
 * An interface that loosely defines a source of data - usually reproducible. This data may come from anywhere
 */
interface DataSource<I : InputFlow>: DataCloseable {
    companion object {

    }

    val dataSize: ULong?

    /**
     * The reproducibility traits of this data source.
     *
     * These traits *may* change between invocations, so a fresh instance should be obtained each time
     */
    val reproducibility: DataSourceReproducibility

    fun openInputFlow(): I?
    fun canOpenInputFlow(): Boolean
}

@ExperimentalUnsignedTypes
suspend fun DataSource<*>.copyToOutputFlow(sink: OutputFlow) {
    openInputFlow()?.copyToOutputFlow(sink)
}

inline class DataSourceReproducibility(val flag: Byte) {
    constructor(flag: Number) : this(flag.toByte())
    constructor(isStatic: Boolean = false,
                isDeterministic: Boolean = false,
                isExpensive: Boolean = false,
                isUnreliable: Boolean = false,
                isUnstable: Boolean = false,
                isRandomAccess: Boolean = false
    ) : this(
            (if (isStatic) STATIC_MASK else 0)
                    or (if (isDeterministic) DETERMINISTIC_MASK else 0)
                    or (if (isExpensive) EXPENSIVE_MASK else 0)
                    or (if (isUnreliable) UNRELIABLE_MASK else 0)
                    or (if (isUnstable) UNSTABLE_MASK else 0)
                    or (if (isRandomAccess) RANDOM_ACCESS_MASK else 0)
    )

    companion object {
        /**
         * The data is static, based in a reproducible, unchanging form.
         * Examples include file- or memory- based data sources.
         * Static data should not normally be cached.
         */
        const val STATIC_MASK = 0b00000001

        /**
         * The data is deterministic; it can be produced from an initial state, up to the end of the data stream.
         * Deterministic data may or may not need to be cached.
         * Slow operations, such as compression, may get a boost out of caching, whereas operations such as decompression will get little boost out of it.
         * Caching may be required in circumstances such as the underlying algorithm requiring impossible data, but that is left up to the invoker.
         */
        const val DETERMINISTIC_MASK = 0b00000010

        /**
         * The data is expensive; while it can be accessed or produced, such operations are expensive in time, memory, or computational resources.
         * Expensive data tends to cover network or compression resources; scenarios where the data is slow to retrieve, and caching will massively speed this up.
         * Expensive data sources should be cached where possible.
         */
        const val EXPENSIVE_MASK = 0b00000100

        /**
         * The source is unreliable; properties about this data, such as the size, or even the data, **may** change over time.
         * Unreliable data sources are guaranteed to be usable for at least one stream. Using this data source multiple times over a short period of time is likely to work, however it cannot be guarenteed.
         * Unreliable data should be cached if it is needed frequently, or if it is needed a while after it is created.
         */
        const val UNRELIABLE_MASK = 0b00001000

        /**
         * The source is unstable; no part of this source can be guaranteed beyond the first use.
         * Unstable data may come from a variety of sources, but most commonly it will come from network or nested data, or from a source that is non-deterministic.
         * Unstable data should be cached if it is needed more than once.
         */
        const val UNSTABLE_MASK = 0b00010000

        /**
         * The data is randomly accessible; it supports the seek operand.
         * Data that is randomly accessible tends to come from static sources, and tends to be at odds with other sources.
         * Randomly accessible data is unlikely to need to be cached, however data that is *not* randomly accessible may need to be.
         */
        const val RANDOM_ACCESS_MASK = 0b00100000

        fun static() = DataSourceReproducibility(STATIC_MASK)
        fun deterministic() = DataSourceReproducibility(DETERMINISTIC_MASK)
        fun expensive() = DataSourceReproducibility(EXPENSIVE_MASK)
        fun unreliable() = DataSourceReproducibility(UNRELIABLE_MASK)
        fun unstable() = DataSourceReproducibility(UNSTABLE_MASK)
        fun randomAccess() = DataSourceReproducibility(RANDOM_ACCESS_MASK)
    }

    infix fun or(other: Number): DataSourceReproducibility = DataSourceReproducibility((flag.toInt() or other.toInt()).toByte())
    infix fun and(other: Number): DataSourceReproducibility = DataSourceReproducibility((flag.toInt() and other.toInt()).toByte())
    infix fun xor(other: Number): DataSourceReproducibility = DataSourceReproducibility((flag.toInt() xor other.toInt()).toByte())

    infix fun has(mask: Number): Boolean = flag.toInt() and mask.toInt() == mask.toInt()

    fun isStatic(): Boolean = has(STATIC_MASK)
    fun isDeterministic(): Boolean = has(DETERMINISTIC_MASK)
    fun isExpensive(): Boolean = has(EXPENSIVE_MASK)
    fun isUnreliable(): Boolean = has(UNRELIABLE_MASK)
    fun isUnstable(): Boolean = has(UNSTABLE_MASK)
    fun isRandomAccess(): Boolean = has(RANDOM_ACCESS_MASK)
}