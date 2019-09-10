package info.spiralframework.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_FILE_NAME_KEY
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.text.arbitraryProgressBar
import info.spiralframework.base.jvm.UTF8String
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY

fun SpiralCoreContext.apiCheckForUpdate(project: String, build: String): String = apiCheckForUpdate(apiBase, project, build)
fun SpiralCoreContext.apiLatestBuild(project: String): String = apiLatestBuild(apiBase, project)
fun SpiralCoreContext.apiBuildForFingerprint(fingerprint: String): String = apiBuildForFingerprint(apiBase, fingerprint)
fun SpiralCoreContext.jenkinsArtifactForBuild(project: String, latestBuild: String, fileName: String): String = jenkinsArtifactForBuild(jenkinsBase, project, latestBuild, fileName)

fun apiCheckForUpdate(apiBase: String, project: String, build: String): String = String.format("%s/jenkins/projects/Spiral-%s/needs_update/%s", apiBase, project, build)
fun apiLatestBuild(apiBase: String, project: String): String = String.format("%s/jenkins/projects/Spiral-%s/latest_build", apiBase, project)
fun apiBuildForFingerprint(apiBase: String, fingerprint: String): String = String.format("%s/jenkins/fingerprint/%s/build", apiBase, apiBase, fingerprint)
fun jenkinsArtifactForBuild(jenkinsBase: String, project: String, latestBuild: String, fileName: String): String = String.format("%s/job/Spiral-%s/%s/artifact/%s/build/libs/%s", jenkinsBase, project, latestBuild, project.toLowerCase(), fileName)

val spiralFrameworkOnline: Boolean by lazy { Fuel.head("https://spiralframework.info").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }
val githubOnline: Boolean by lazy { Fuel.head("https://github.com").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }
val signaturesCdnOnline: Boolean by lazy { Fuel.head("https://storage.googleapis.com/signatures.spiralframework.info").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }

suspend fun checkForUpdate(context: SpiralCoreContext, project: String): Pair<String, Int>? {
    with(context) {
        val updateResult = arbitraryProgressBar(loadingText = "gurren.update.checking", loadedText = "") {
            val jenkinsBuild = context.retrieveStaticValue(SPIRAL_ENV_BUILD_KEY)?.toIntOrNull()
            val fileName = context.retrieveStaticValue(SPIRAL_FILE_NAME_KEY)

            if (jenkinsBuild == null || fileName == null)
                return@arbitraryProgressBar null
            val latestBuild = Fuel.get(apiLatestBuild(project))
                    .userAgent()
                    .timeout(updateConnectTimeout) //Time out if it takes longer than 2s to connect to our API
                    .timeoutRead(updateReadTimeout) //Time out if it takes longer than 2s to read a response
                    .response().takeIfSuccessful()?.let(::UTF8String)?.toIntOrNull() //.also(this@SpiralCoreData::printResponse)
                    ?: return@arbitraryProgressBar null

            if (latestBuild > jenkinsBuild)
                return@arbitraryProgressBar jenkinsArtifactForBuild(project, latestBuild.toString(), fileName) to latestBuild

            return@arbitraryProgressBar null
        }

        if (updateResult == null)
            printlnLocale("gurren.update.none")
        else
            printlnLocale("gurren.update.available")

        return updateResult
    }
}

fun buildForVersion(context: SpiralCoreContext, version: String): Int? =
        Fuel.get(context.apiBuildForFingerprint(version))
                .userAgent()
                .timeout(5 * 1000) //Time out if it takes longer than 5s to connect to our API
                .timeoutRead(5 * 1000) //Time out if it takes longer than 5s to read a response
                .response().takeIfSuccessful()?.let { data -> String(data) }?.toIntOrNull() //.also(this::printResponse)