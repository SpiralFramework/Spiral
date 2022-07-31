package info.spiralframework.gui.javafx

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableMap
import javafx.scene.Scene
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.File
import java.net.URI
import java.nio.file.*
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.toPath

class ReloadingStylesheets(val uris: Set<URI>, val uiScope: CoroutineScope, ioScope: CoroutineScope) : Closeable,
    CoroutineScope {
    private val scenes: ObservableMap<Scene, ListChangeListener<String>> = FXCollections.observableHashMap()

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + ioScope.coroutineContext

    private fun stylesheetsChanged(scene: Scene, change: ListChangeListener.Change<out String>) {
        println("$scene: $change")
    }

    public fun addScene(scene: Scene) {
        val listener = this.scenes.computeIfAbsent(scene) { ListChangeListener { stylesheetsChanged(scene, it) } }
        scene.stylesheets.addListener(listener)

        reload(uris.map(URI::toString), scene)
    }

    public fun removeScene(scene: Scene) {
        val listener = this.scenes.remove(scene)
        if (listener != null) scene.stylesheets.removeListener(listener)

        uiScope.launch {
            scene.stylesheets.removeAll(uris.map(URI::toString))
        }
    }

    public fun reload(uri: URI) =
        reload(uri.toString(), scenes.keys)

    public fun reload(uris: Collection<URI>) =
        reload(uris.map(URI::toString), scenes.keys)

    public fun reload(vararg uris: URI) =
        reload(uris.map(URI::toString), scenes.keys)

    private inline fun reload(uris: List<String>, scenes: Iterable<Scene>) =
        uiScope.launch { scenes.forEach { scene -> reload(uris, scene) } }

    private inline fun reload(uris: List<String>, scene: Scene) =
        scene.stylesheets.replaceAllFirstOrAdd(uris, uris) { a, b -> URI(a) == URI(b) }


    private inline fun reload(uri: String, scenes: Iterable<Scene>) =
        uiScope.launch { scenes.forEach { scene -> reload(uri, scene) } }

    private inline fun reload(uri: String, scene: Scene) =
        scene.stylesheets.replaceFirstOrAdd(uri, uri) { a, b -> URI(a) == URI(b) }

    override fun close() {
        cancel()
    }

    init {
        uris.forEach(this::reload)

        uris.mapNotNull { uri ->
            try {
                when (uri.scheme) {
                    "file" -> FileSystems.getFileSystem(
                        URI(
                            uri.scheme,
                            uri.userInfo,
                            uri.host,
                            uri.port,
                            "/",
                            null,
                            null
                        )
                    ) to uri
                    else -> FileSystems.getFileSystem(uri) to uri
                }
            } catch (th: Throwable) {
                th.printStackTrace()
                null
            }
        }
            .groupBy(Pair<FileSystem, URI>::first, Pair<FileSystem, URI>::second)
            .forEach { (fs, uris) ->
                val watchService = fs.newWatchService()
                uris.mapTo(HashSet()) { uri -> uri.toPath().parent }
                    .forEach { path -> path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY) }

                @Suppress("BlockingMethodInNonBlockingContext")
                launch {
                    watchService.use { watcher ->
                        while (isActive) {
                            val key = watcher.poll()
                            if (key == null) {
                                delay(500)
                                continue
                            }

                            try {
                                val dir = (key.watchable() as? Path)?.toFile() ?: continue
                                key.pollEvents().forEach poll@{ event ->
                                    when (event.kind()) {
                                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                                            val modified =
                                                File(dir, (event.context() as? Path ?: return@poll).toString()).toURI()
                                            if (modified in uris) {
                                                println("Reloading $modified")

                                                reload(modified)
                                            }
                                        }
                                    }
                                }
                            } catch (th: Throwable) {
                                th.printStackTrace()
                            } finally {
                                key.reset()
                                delay(100)
                            }
                        }
                    }
                }
            }
    }
}