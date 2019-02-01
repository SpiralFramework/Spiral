package info.spiralframework.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.github.kittinunf.fuel.Fuel
import info.spiralframework.base.LocaleLogger
import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.util.locale
import info.spiralframework.core.serialisation.InstantSerialisation
import info.spiralframework.formats.utils.DataHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

/**
 * This singleton holds important information for all Spiral modules
 * @author UnderMybrella
 */
object SpiralCoreData {
    /** Jackson mapper for JSON data */
    val JSON_MAPPER: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    /** Jackson mapper for YAML data */
    val YAML_MAPPER: ObjectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    /** Jackson mapper for XML data */
    val XML_MAPPER: ObjectMapper = XmlMapper(JacksonXmlModule().apply { setDefaultUseWrapper(false) })
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), InstantSerialisation.MODULE())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY))

    /** Steam ID for Danganronpa: Trigger Happy Havoc */
    val STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC = "413410"
    /** Steam ID for Danganronpa 2: Goodbye Despair */
    val STEAM_DANGANRONPA_2_GOODBYE_DESPAIR = "413420"

    /** 'File Name' for Spiral header data, to be used in archives */
    val SPIRAL_HEADER_NAME = "Spiral-Header"
    /**
     * 'File Name' for Spiral mod list data, to be used in archives
     * This file should ideally keep track of mods currently installed, and their files + versions
     * */
    val SPIRAL_MOD_LIST = "Spiral-Mod-List"

    val API_BASE = "https://api.abimon.org/api"
    val API_LATEST_BUILD = "$API_BASE/jenkins/projects/Spiral-%s/needs_update/%s"

    val JENKINS_BASE = "https://jenkins.abimon.org"
    val LATEST_BUILD = "$JENKINS_BASE/job/Spiral-%s/lastSuccessfulBuild/artifact/%s/build/libs/%s"

    val fileName: String? by lazy {
        val file = File(SpiralCoreData::class.java.protectionDomain.codeSource.location.path)
        if (!file.isFile)
            return@lazy null
        return@lazy file.name
    }

    /**
     * An MD5 hash of the running JAR file, or null if we're not running from a JAR file (developer directory)
     */
    val version: String? by lazy {
        val file = File(SpiralCoreData::class.java.protectionDomain.codeSource.location.path)
        if (!file.isFile)
            return@lazy null

        val md = MessageDigest.getInstance("MD5")

        val channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
        val buffer = ByteBuffer.allocate(8192)

        while (channel.isOpen) {
            val read = channel.read(buffer)
            if (read <= 0)
                break


            buffer.flip()
            md.update(buffer)
            buffer.rewind()
        }

        return@lazy String.format("%032x", BigInteger(1, md.digest()))
    }

    var LOGGER: Logger
    var NORMAL_LOGGER: Logger
        get() = DataHandler.LOGGER.let { logger -> if (logger is LocaleLogger) logger.logger else logger }
        set(value) {
            if (DataHandler.LOGGER is LocaleLogger)
                (DataHandler.LOGGER as LocaleLogger).logger = value
            else
                DataHandler.LOGGER = NORMAL_LOGGER
        }

    fun checkForUpdate(project: String): String? {
        val (_, response) = Fuel.get(String.format(API_LATEST_BUILD, project, version)).userAgent().response()
//        if (response.isSuccessful && String(response.data) == "true") {
            return String.format(LATEST_BUILD, project, project.toLowerCase(), fileName)
//        }

        return null
    }

    init {
        SpiralLocale.addBundle("SpiralCore")
        LOGGER = LocaleLogger(LoggerFactory.getLogger(locale<String>("logger.core.name")))
    }
}