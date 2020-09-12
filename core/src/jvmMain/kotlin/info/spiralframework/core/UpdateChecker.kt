package info.spiralframework.core

import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_FILE_NAME_KEY
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.jvm.UTF8String
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

fun SpiralCoreContext.apiCheckForUpdate(project: String, build: String): String = apiCheckForUpdate(apiBase, project, build)
fun SpiralCoreContext.apiLatestBuild(project: String): String = apiLatestBuild(apiBase, project)
fun SpiralCoreContext.apiBuildForFingerprint(fingerprint: String): String = apiBuildForFingerprint(apiBase, fingerprint)
fun SpiralCoreContext.jenkinsArtifactForBuild(project: String, latestBuild: String, fileName: String): String = jenkinsArtifactForBuild(jenkinsBase, project, latestBuild, fileName)

fun apiCheckForUpdate(apiBase: String, project: String, build: String): String = String.format("%s/jenkins/projects/Spiral-%s/needs_update/%s", apiBase, project, build)
fun apiLatestBuild(apiBase: String, project: String): String = String.format("%s/jenkins/projects/Spiral-%s/latest_build", apiBase, project)
fun apiBuildForFingerprint(apiBase: String, fingerprint: String): String = String.format("%s/jenkins/fingerprint/%s/build", apiBase, apiBase, fingerprint)
fun jenkinsArtifactForBuild(jenkinsBase: String, project: String, latestBuild: String, fileName: String): String =
    String.format("%s/job/Spiral-%s/%s/artifact/%s/build/libs/%s", jenkinsBase, project, latestBuild, project.toLowerCase(), fileName)

//val spiralFrameworkOnline: Boolean by lazy { Fuel.head("https://spiralframework.info").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }
//val githubOnline: Boolean by lazy { Fuel.head("https://github.com").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }
//val signaturesCdnOnline: Boolean by lazy { Fuel.head("https://storage.googleapis.com/signatures.spiralframework.info").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }

suspend fun SpiralCoreContext.spiralFrameworkOnline(): Boolean =
    httpClient.head<HttpResponse>("https://spiralframework.info").status.value < 400

suspend fun SpiralCoreContext.githubOnline(): Boolean =
    httpClient.head<HttpResponse>("https://github.com").status.value < 400

suspend fun SpiralCoreContext.signaturesCdnOnline(): Boolean =
    httpClient.head<HttpResponse>("https://storage.googleapis.com/signatures.spiralframework.info")
        .status.value < 400

suspend fun checkForUpdate(context: SpiralCoreContext, project: String): Pair<String, Int>? {
    with(context) {
        val jenkinsBuild = context.retrieveStaticValue(SPIRAL_ENV_BUILD_KEY)?.toIntOrNull()
        val fileName = context.retrieveStaticValue(SPIRAL_FILE_NAME_KEY)

        if (jenkinsBuild == null || fileName == null) {
            printlnLocale("gurren.update.none")
            return null
        }

        val latestBuild = arbitraryProgressBar(loadingText = localise("gurren.update.checking"), loadedText = null) {
            runCatching { httpClient.get<String>(apiLatestBuild(project)) }.getOrNull()?.toIntOrNull()
        }

        if (latestBuild == null) {
            printlnLocale("gurren.update.none")
            return null
        } else if (latestBuild > jenkinsBuild) {
            printlnLocale("gurren.update.available")

            return Pair(jenkinsArtifactForBuild(project, latestBuild.toString(), fileName), latestBuild)
        } else {
            printlnLocale("gurren.update.none")
            return null
        }
    }
}

suspend fun buildForVersion(context: SpiralCoreContext, version: String): Int? =
    runCatching { context.httpClient.get<String>(context.apiBuildForFingerprint(version)).toIntOrNull() }
        .getOrNull()