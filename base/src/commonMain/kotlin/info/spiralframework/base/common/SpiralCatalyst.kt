package info.spiralframework.base.common

import dev.brella.kornea.annotations.AvailableSince
import dev.brella.kornea.toolkit.common.KorneaToolkit
import kotlin.reflect.KClass

public interface SpiralCatalyst<T : Any> {
    public val klass: KClass<T>
    public suspend fun prime(catalyst: T)
}

@AvailableSince(KorneaToolkit.VERSION_2_3_0_ALPHA)
public suspend inline fun <P1, T: SpiralCatalyst<P1>> prime(obj: T, p1: P1): T {
    obj.prime(p1)
    return obj
}

@AvailableSince(KorneaToolkit.VERSION_2_3_0_ALPHA)
public suspend inline fun <P1, T: SpiralCatalyst<P1>> P1.prime(obj: T): T {
    obj.prime(this)
    return obj
}


public suspend inline fun <P1 : Any, T> P1.tryPrime(obj: T): T {
    @Suppress("UNCHECKED_CAST")
    if (obj is SpiralCatalyst<*> && obj.klass.isInstance(this)) (obj as SpiralCatalyst<P1>).prime(this)

    return obj
}