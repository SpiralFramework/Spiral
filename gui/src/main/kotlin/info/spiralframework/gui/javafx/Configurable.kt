package info.spiralframework.gui.javafx

class Configurable<T>(var backing: T? = null, val initialiser: (() -> T)? = null) {
    public inline operator fun invoke(): T = get()
    public inline fun get(): T = backing!!
    public inline fun getOrConfigure(): T {
        if (backing == null) backing = requireNotNull(initialiser)()
        return backing!!
    }

    public inline fun init() {
        if (backing == null) backing = requireNotNull(initialiser)()
    }

    public inline fun init(block: () -> T) {
        if (backing == null) backing = block()
    }

    public inline fun set(block: () -> T) {
        backing = block()
    }

    public inline operator fun invoke(init: () -> T, block: T.() -> Unit): T =
        configure(init, block)
    public inline fun configure(init: () -> T, block: T.() -> Unit): T {
        if (backing == null) backing = init()
        return backing!!.apply(block)
    }

    public inline operator fun invoke(block: T.() -> Unit): T =
        configure(block)
    public inline fun configure(block: T.() -> Unit): T {
        if (backing == null) backing = requireNotNull(initialiser)()
        return backing!!.apply(block)
    }

    fun clear() {
        backing = null
    }
}

public inline fun <T> config(noinline initialiser: (() -> T)? = null): Configurable<T> =
    Configurable(null, initialiser)

public inline fun <T> MutableList<in T>.add(configurable: Configurable<T>, block: T.() -> Unit) {
    add(configurable(block))
}