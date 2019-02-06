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
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.LocaleLogger
import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.config.SpiralConfig
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
import java.util.jar.JarFile

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
    val API_CHECK_FOR_UPDATE = "$API_BASE/jenkins/projects/Spiral-%s/needs_update/%s"
    val API_LATEST_BUILD = "$API_BASE/jenkins/projects/Spiral-%s/latest_build"

    val JENKINS_BASE = "https://jenkins.abimon.org"
    val JENKINS_BUILD = "$JENKINS_BASE/job/Spiral-%s/%s/artifact/%s/build/libs/%s"

    val ENVIRONMENT_PROPERTIES: MutableList<String> = mutableListOf(
            "os.name", "os.version", "os.arch",
            "java.vendor", "java.version", "java.vendor.url",
            "file.separator", "path.separator", "line.separator",
            "spiral.module", "spiral.version", "spiral.name",
            "manifest.main-class"
    )
    val ADDITIONAL_ENVIRONMENT: MutableMap<String, String?> = HashMap()
    val ENVIRONMENT: String
        get() = buildString {
            this@SpiralCoreData::fileName.get() //If we're getting the environment we should proc these
            this@SpiralCoreData::version.get()
            for (env in ENVIRONMENT_PROPERTIES) {
                append(env)
                append(": ")
                appendln((System.getProperty(env) ?: ADDITIONAL_ENVIRONMENT[env])?.replace("\n", "\\n"))
            }
        }


    val fileName: String? by lazy {
        val file = File(SpiralCoreData::class.java.protectionDomain.codeSource.location.path)
        val name = file.takeIf(File::isFile)?.let(File::getName)
        ADDITIONAL_ENVIRONMENT["spiral.name"] = name
        return@lazy name
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

        val hash = String.format("%032x", BigInteger(1, md.digest()))
        ADDITIONAL_ENVIRONMENT["spiral.version"] = hash
        return@lazy hash
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

    val CONFIG: SpiralCoreConfig? by cacheNullableYaml(SpiralConfig.getConfigFile("core"))

    fun checkForUpdate(project: String): Pair<String, String>? {
        val (_, checkForUpdateResponse) = Fuel.get(String.format(API_CHECK_FOR_UPDATE, project, version))
                .userAgent()
                .timeout(2 * 1000) //Time out if it takes longer than 2s to connect to our API
                .timeoutRead(2 * 1000) //Time out if it takes longer than 2s to read a response
                .response()
        if (checkForUpdateResponse.isSuccessful && String(checkForUpdateResponse.data) == "true") {
            val (_, latestBuildResponse) = Fuel.get(String.format(API_LATEST_BUILD, project))
                    .userAgent()
                    .timeout(2 * 1000) //Time out if it takes longer than 2s to connect to our API
                    .timeoutRead(2 * 1000) //Time out if it takes longer than 2s to read a response
                    .response()

            if (latestBuildResponse.isSuccessful) {
                val latestBuild = String(latestBuildResponse.data)
                return String.format(JENKINS_BUILD, project, latestBuild, project.toLowerCase(), fileName) to latestBuild
            }
        }

        return null
    }

    init {
        SpiralLocale.addBundle("SpiralCore")
        LOGGER = LocaleLogger(LoggerFactory.getLogger(locale<String>("logger.core.name")))

        //Add manifest data to environment
        val file = File(SpiralCoreData::class.java.protectionDomain.codeSource.location.path)
        if (file.isFile) {
            JarFile(file).use { jar ->
                //                jar.manifest.entries.forEach { headerKey, attributes ->
//                    attributes.forEach { key, value ->
//                        ADDITIONAL_ENVIRONMENT["manifest.${headerKey.toLowerCase()}.${key.toString().toLowerCase()}"] = value.toString().toLowerCase()
//                    }
//                }
                jar.manifest.mainAttributes.forEach { key, value ->
                    ADDITIONAL_ENVIRONMENT["manifest.${key.toString().toLowerCase()}"] = value.toString()

                }
            }
        }
    }
}