package info.spiralframework.console.jvm.pipeline

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.knolus.types.asType
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.console.jvm.data.GurrenSpiralContext
import kotlinx.coroutines.Job
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree.Tree
import org.antlr.v4.runtime.tree.Trees
import java.io.File
import java.util.*
import kotlin.reflect.KClass

fun toStringTree(t: Tree, recog: Parser?): String? {
    val ruleNames = recog?.ruleNames
    val ruleNamesList = if (ruleNames != null) listOf(*ruleNames) else null
    return toStringTree(t, ruleNamesList)
}

/** Print out a whole tree in LISP form. [.getNodeText] is used on the
 * node payloads to get the text for the nodes.
 */
fun toStringTree(t: Tree, ruleNames: List<String?>?, indent: Int = 0): String? {
    var s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    if (t.childCount == 0) return s
    val buf = StringBuilder()
//    buf.append("(")
    buf.appendln()
    repeat(indent) { buf.append('\t') }
    buf.append("> ")
    s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    buf.append(s)
    buf.append(' ')
    for (i in 0 until t.childCount) {
        if (i > 0) buf.append(' ')
        buf.append(toStringTree(t.getChild(i), ruleNames, indent + 1))
    }
//    buf.append(")")
    return buf.toString()
}

object SpiralContextPlaceholder : KnolusTypedValue, KnolusTypedValue.TypeInfo<SpiralContextPlaceholder>, KorneaTypeChecker<SpiralContextPlaceholder> by KorneaTypeChecker.ClassBased() {
    override val typeInfo: KnolusTypedValue.TypeInfo<*> = this
    override val typeHierarchicalNames: Array<String> = arrayOf("SpiralContextPlaceholder", "Object")
}

//data class KnolusMessageEvent(public val event: MessageCreateEvent): KnolusTypedValue {
//    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusMessageEvent>, KorneaTypeChecker<KnolusMessageEvent> by KorneaTypeChecker.ClassBased() {
//        override val typeHierarchicalNames: Array<String> = arrayOf("KnolusMessageEvent", "Object")
//    }
//    override val typeInfo: KnolusTypedValue.TypeInfo<*> = TypeInfo
//}

data class KnolusTypedWrapper<T : Any>(public val inner: T, override val typeInfo: KnolusTypedValue.TypeInfo<*>) : KnolusTypedValue {
    data class TypeInfo<T : Any>(val innerKlass: KClass<T>) : KnolusTypedValue.TypeInfo<KnolusTypedWrapper<T>> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Object")
        override fun asInstance(instance: Any?): KnolusTypedWrapper<T> = instance as KnolusTypedWrapper<T>
        override fun asInstanceSafe(instance: Any?): KnolusTypedWrapper<T>? = instance as? KnolusTypedWrapper<T>
        override fun isInstance(instance: Any?): Boolean = instance is KnolusTypedWrapper<*> && innerKlass.isInstance(instance.inner)
    }

    companion object {
        val SPIRAL_CONTEXT = typeInfo<GurrenSpiralContext>()
        val RANDOM = typeInfo<Random>()
        val FILE = typeInfo<File>()
        val JOB = typeInfo<Job>()

        inline operator fun <reified T : Any> invoke(inner: T): KnolusTypedWrapper<T> = KnolusTypedWrapper(inner, TypeInfo(T::class))
    }

    override suspend fun <R : KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext, typeInfo: I): KorneaResult<R> =
        if (typeInfo.isInstance(this)) KorneaResult.success(typeInfo.asInstance(this)) else super.asTypeImpl(context, typeInfo)
}

inline fun <reified T : Any> typeInfo() = KnolusTypedWrapper.TypeInfo(T::class)

//inline fun messageEventParameter(name: String? = null) = wrappedParameter(name, KnolusWrapperTypes.MESSAGE_CREATE_EVENT)
//inline fun userQueryParameter(name: String? = null) = wrappedParameter(name, KnolusWrapperTypes.USER_QUERY)
//inline fun userQueryListParameter(name: String? = null) = wrappedParameter(name, KnolusWrapperTypes.USER_QUERY_LIST)
//inline fun customEmojiParameter(name: String? = null) = wrappedParameter(name, KnolusWrapperTypes.CUSTOM_EMOJI_QUERY)

//inline fun spiralContextParameter(name: String? = null) = wrappedParameter(name, KnolusTypedWrapper.SPIRAL_CONTEXT)
inline fun fileParameter(name: String? = null) = wrappedParameter(name, KnolusTypedWrapper.FILE)
inline fun jobParameter(name: String? = null) = wrappedParameter(name, KnolusTypedWrapper.JOB)

inline fun <reified T : Any> wrappedParameter(name: String? = null, typeInfo: KnolusTypedWrapper.TypeInfo<T> = typeInfo(), vararg aliases: String) =
    typeInfo.typeSpecWith(name, null, aliases) { _ -> KorneaResult.success(inner) }

suspend fun <S : KnolusTypedValue, R : Any, I : KnolusTypedWrapper.TypeInfo<R>> S.unwrap(context: KnolusContext, typeInfo: I): KorneaResult<R> =
    asType(context, typeInfo).map(KnolusTypedWrapper<R>::inner)

@ExperimentalUnsignedTypes
suspend fun KnolusContext.spiralContext(): KorneaResult<GurrenSpiralContext> = this["spiralContext"].flatMap { value -> value.unwrap(this, KnolusTypedWrapper.SPIRAL_CONTEXT) }

inline fun <reified R: Any> wrap(value: R): KnolusTypedWrapper<R> = KnolusTypedWrapper.invoke(value)