package org.abimon.spiral.core.formats.text

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.NonstopFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.readMapValue
import org.abimon.spiral.core.writeShort
import org.yaml.snakeyaml.error.YAMLException
import java.io.CharConversionException
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

abstract class JacksonFormat: SpiralFormat {
    override val conversions: Array<SpiralFormat> = emptyArray() //We should not be doing any automated conversions
    val manualConversions: Array<SpiralFormat> = arrayOf(NonstopFormat) //But we should allow manual conversions

    open val OTHER_EXCEPTION_TYPES: Array<KClass<out Throwable>> = emptyArray()

    override fun canConvert(game: DRGame?, format: SpiralFormat): Boolean = format in manualConversions
    abstract val MAPPER: ObjectMapper

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            dataSource().use { stream -> MAPPER.readValue(stream, Map::class.java) }
            return true
        } catch (json: JsonParseException) {
        } catch (json: JsonMappingException) {
        } catch (io: CharConversionException) {
        } catch (th: Throwable) {
            if(OTHER_EXCEPTION_TYPES.none { klass -> klass.isInstance(th) })
                throw th
        }

        try {
            dataSource().use { stream -> MAPPER.readValue(stream, List::class.java) }
            return true
        } catch (json: JsonParseException) {
        } catch (json: JsonMappingException) {
        } catch (io: CharConversionException) {
        } catch (th: Throwable) {
            if(OTHER_EXCEPTION_TYPES.none { klass -> klass.isInstance(th) })
                throw th
        }

        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true

        when(format) {
            NonstopFormat -> {
                val debateMap = dataSource().use { stream -> MAPPER.readMapValue(stream, String::class, Any::class) }

                val duration = debateMap["duration"]?.toString()?.toIntOrNull() ?: throw IllegalArgumentException("$name is an invalid Nonstop Debate $name file ('duration' is either not present or not a number)")
                val sections = (debateMap["sections"] as? List<*> ?: throw IllegalArgumentException("$name is an invalid Nonstop Debate $name file ('sections' is either not present or not a list)"))
                        .filterIsInstance(Map::class.java)
                        .map { theMap -> theMap.filter { (a, b) -> a is String && b is Int }.mapKeys { (a) ->
                            val op = a as String
                            when {
                                op.startsWith("0x") -> return@mapKeys op.substring(2).toInt(16)
                                op.matches("\\d+".toRegex()) -> return@mapKeys op.toInt()
                                SpiralData.nonstopOpCodes.entries.any { (_, name) -> name.equals(op, true) } -> return@mapKeys SpiralData.nonstopOpCodes.entries.first { (_, name) -> name.equals(op, true) }.key
                                else -> return@mapKeys 0x00
                            }
                        }.mapValues { (_, b) -> b as Int } }

                if(sections.isEmpty())
                    throw throw IllegalArgumentException("$name is an invalid Nonstop Debate $name file ('sections' is empty)")

                output.writeShort(duration / 2, unsigned = true)
                output.writeShort(sections.size, unsigned = true)

                val max = sections.first().entries.sortedBy { (index) -> index }.last().key + 1 //Zero Indexing

                for(section in sections) {
                    for(i in 0 until max)
                        output.writeShort(section[i] ?: 0x00, unsigned = true)
                }
            }
        }

        return false
    }

    object YAML: JacksonFormat() {
        override val name: String = "YAML"
        override val extension: String = "yaml"

        override val MAPPER: ObjectMapper
            get() = SpiralData.YAML_MAPPER

        override val OTHER_EXCEPTION_TYPES: Array<KClass<out Throwable>> = arrayOf(YAMLException::class)
    }

    object JSON: JacksonFormat() {
        override val name: String = "JSON"
        override val extension: String = "json"

        override val MAPPER: ObjectMapper
            get() = SpiralData.MAPPER
    }
}