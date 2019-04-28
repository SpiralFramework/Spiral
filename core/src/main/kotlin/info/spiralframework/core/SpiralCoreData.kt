package info.spiralframework.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import info.spiralframework.base.LocaleLogger
import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.jar.JarFile

/**
 * This singleton holds important information for all Spiral modules
 * @author UnderMybrella
 */
object SpiralCoreData: SpiralCoreConfigAccessor {
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

    val ENVIRONMENT_PROPERTIES: MutableList<String> = mutableListOf(
            "os.name", "os.version", "os.arch",
            "java.vendor", "java.version", "java.vendor.url",
            "file.separator", "path.separator", "line.separator",
            "spiral.module", "spiral.version", "spiral.name",
            "manifest.main-class"
    )
    val ADDITIONAL_ENVIRONMENT: MutableMap<String, String?> = hashMapOf("spiral.module" to "spiral-core")
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

    val apiCheckForUpdate: String
        get() = "$apiBase/jenkins/projects/Spiral-%s/needs_update/%s"
    val apiLatestBuild: String
        get() = "$apiBase/jenkins/projects/Spiral-%s/latest_build"
    val apiBuildForFingerprint: String
        get() = "$apiBase/jenkins/fingerprint/%s/build"

    val jenkinsArtifactForBuild: String
        get() = "$jenkinsBase/job/Spiral-%s/%s/artifact/%s/build/libs/%s"

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

        val hash = FileChannel.open(file.toPath(), StandardOpenOption.READ).use(ReadableByteChannel::md5Hash)
        ADDITIONAL_ENVIRONMENT["spiral.version"] = hash
        return@lazy hash
    }
    val sha256Hash: String? by lazy {
        val file = File(SpiralCoreData::class.java.protectionDomain.codeSource.location.path)
        if (!file.isFile)
            return@lazy null

        val hash = FileChannel.open(file.toPath(), StandardOpenOption.READ).use(ReadableByteChannel::sha256Hash)
        ADDITIONAL_ENVIRONMENT["spiral.sha256"] = hash
        return@lazy hash
    }

    /** The build for this jar file, or null if we're either not running a JAR file (developer directory), or if we're using a custom compiled version */
    val jenkinsBuild: Int? by lazy { version?.let(this::buildForVersion) }

    val LOGGER: Logger

    var mainModule: String = "spiral-core"
        set(value) {
            field = value
            ADDITIONAL_ENVIRONMENT["spiral.module"] = mainModule
        }
    val moduleLoader = ServiceLoader.load(SpiralModuleProvider::class.java)
    val loadedModules: Array<String> = moduleLoader.iterator()
            .asSequence()
            .map(SpiralModuleProvider::moduleName)
            .toList()
            .toTypedArray()

    fun checkForUpdate(project: String): Pair<String, Int>? {
        val updateResult = arbitraryProgressBar(loadingText = "gurren.update.checking", loadedText = "") {
            if (jenkinsBuild == null)
                return@arbitraryProgressBar null
            val latestBuild = Fuel.get(String.format(apiLatestBuild, project))
                    .userAgent()
                    .timeout(updateConnectTimeout) //Time out if it takes longer than 2s to connect to our API
                    .timeoutRead(updateReadTimeout) //Time out if it takes longer than 2s to read a response
                    .response().also(this::printResponse).takeIfSuccessful()?.let(::UTF8String)?.toIntOrNull() ?: return@arbitraryProgressBar null

            if (latestBuild > jenkinsBuild!!)
                return@arbitraryProgressBar String.format(jenkinsArtifactForBuild, project, latestBuild.toString(), project.toLowerCase(), fileName) to latestBuild

            return@arbitraryProgressBar null
        }

        if (updateResult == null)
            printlnLocale("gurren.update.none")
        else
            printlnLocale("gurren.update.available")

        return updateResult
    }

    fun buildForVersion(version: String): Int? =
            Fuel.get(String.format(apiBuildForFingerprint, version))
                .userAgent()
                .timeout(2 * 1000) //Time out if it takes longer than 2s to connect to our API
                .timeoutRead(2 * 1000) //Time out if it takes longer than 2s to read a response
                .response().also(this::printResponse).takeIfSuccessful()?.let { data -> String(data) }?.toIntOrNull()

    fun printResponse(response: Triple<Request, Response, *>) {
        LOGGER.trace(response.first.method.value + " " + response.second.url.toExternalForm() + ": " + response.second.statusCode + " / " + String(response.second.data))
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

        ADDITIONAL_ENVIRONMENT["spiral.modules"] = loadedModules.joinToString()
    }
}