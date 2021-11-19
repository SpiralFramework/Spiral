package info.spiralframework.gui.jvm

import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import dev.brella.kornea.base.common.use
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.copyTo
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import dev.brella.kornea.toolkit.common.freeze
import info.spiralframework.antlr.pipeline.PipelineLexer
import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.base.binding.DefaultSpiralLogger
import info.spiralframework.base.binding.defaultSpiralContext
import info.spiralframework.base.common.NULL_TERMINATOR
import info.spiralframework.base.common.PrintOutputFlowWrapper
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignedTo
import info.spiralframework.base.common.config.getConfigFile
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.base.common.properties.plus
import info.spiralframework.base.jvm.crypto.sha512HashBytes
import info.spiralframework.base.jvm.retrieveStackTrace
import info.spiralframework.core.serialisation.DefaultSpiralSerialisation
import info.spiralframework.formats.common.archives.*
import info.spiralframework.formats.common.archives.srd.SrdArchive
import info.spiralframework.formats.common.archives.srd.TextureSrdEntry
import info.spiralframework.formats.common.games.*
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.gui.jvm.pipeline.BstContext
import info.spiralframework.gui.jvm.pipeline.PipelineVisitor
import info.spiralframework.gui.jvm.pipeline.run
import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.npe.tga.TGAReader
import net.npe.tga.readImage
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.Integer.min
import javax.imageio.ImageIO
import kotlin.math.log

class Lagann : Application(), CoroutineScope by MainScope() {
    companion object {
        val ASCII_RANGE = 0x20..0x7E

        @JvmStatic
        fun main(args: Array<String>) {
            launch(Lagann::class.java, *args)
        }
    }

    lateinit var spiralContext: SpiralContext
    lateinit var configFile: File
    val serialisation = DefaultSpiralSerialisation()
    var game: DrGame? = null

    val fileTree = TreeView<LagannTreeData>()
    val binaryDataView = TextArea()
    val dataTypesDropdown = ComboBox<DataPair<*>>()
    val dataView = BorderPane()
    val bstTextArea = TextArea()
    val bstView = SplitPane(bstTextArea, dataView)
    val fileTreeDataView = SplitPane(binaryDataView, bstView)
    val fileTreeSplitPane = SplitPane(fileTree, fileTreeDataView)

    val loadFile = MenuItem("Load File")
    val extractFile = MenuItem("Extract File")
    val saveBstMap = MenuItem("Save BST Map")
    val exportData = MenuItem("Export Item")
    val copyImage = MenuItem("Copy Image")
    val changeGameToDr1 = RadioMenuItem("Dr1")
    val changeGameToDr2 = RadioMenuItem("Dr2")
    val changeGameToUdg = RadioMenuItem("UDG")
    val changeGameToDrv3 = RadioMenuItem("DRv3")
    val changeGameRadial = ToggleGroup().apply { toggles.addAll(changeGameToDr1, changeGameToDr2, changeGameToUdg, changeGameToDrv3) }

    val changeGame = Menu("Change Game", null, changeGameToDr1, changeGameToDr2, changeGameToUdg, changeGameToDrv3)
    val fileMenu = Menu("File", null, loadFile, extractFile, saveBstMap, exportData, copyImage, changeGame)
    val menubar = MenuBar(fileMenu)

    val root = BorderPane()
    val scene: Scene = Scene(root, 600.0, 400.0)

    val bstMap: MutableMap<Pair<String, Int>, String> = HashMap()
    val selectedMap: MutableMap<LagannTreeData, Int> = HashMap()

    lateinit var primaryStage: Stage

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     *
     *
     * NOTE: This method is called on the JavaFX Application Thread.
     *
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set. The primary stage will be embedded in
     * the browser if the application was launched as an applet.
     * Applications may create other stages, if needed, but they will not be
     * primary stages and will not be embedded in the browser.
     */
    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage

        binaryDataView.font = Font.font("Consolas")
        binaryDataView.isEditable = false

        dataView.top = dataTypesDropdown

        bstView.orientation = Orientation.VERTICAL

        fileTree.setCellFactory { LagannTreeDataCell() }
        fileTree.selectionModel.selectedItemProperty().addListener { _, oldValue, newValue ->
            freeze(oldValue) { oldValue ->
                if (oldValue != null && bstTextArea.text != null) {
                    bstMap[oldValue.value.name to dataTypesDropdown.selectionModel.selectedIndex] = bstTextArea.text
                }
            }

            launch {
                freeze(newValue?.value) { value ->
                    //Load the first, say, 4 kB of data
                    val data = value?.dataSource?.useInputFlow { flow -> flow.readBytes() } ?: KorneaResult.empty()

                    withContext(Dispatchers.JavaFx) {
                        dataTypesDropdown.items.clear()
                    }

                    data.doOnFailure {
                        //Directory
                        withContext(Dispatchers.JavaFx) {
                            binaryDataView.text = null
                            dataView.center?.isVisible = false
                            dataView.center = null
                            bstTextArea.text = null
                        }
                    }.doOnSuccess { data ->
                        binaryDataView.text = data.slice(0 until min(4096, data.size)).chunked(16).joinToString("\n") { row -> row.joinToString(" ") { byte -> byte.toInt().and(0xFF).toString(16).padStart(2, '0').toUpperCase() } }

                        val dataSource = requireNotNull(value?.dataSource)
                        val formats: MutableList<DataPair<*>> = ArrayList()
                        requireNotNull(value)

                        if (value is LagannTreeData.PredefinedSubFile) {
                            formats.add(value.data)
                        } else {
                            when (value.name.toLowerCase().substringAfterLast('.')) {
                                "loop" -> formats.add(DataPair("Dr1 Loop", "LOOP", 1.0))  //Don't have a loop obj yet
                                "lin" -> LinScript(
                                    spiralContext, game as? DrGame.LinScriptable
                                                   ?: DrGame.LinScriptable.Unknown, dataSource
                                ).doOnSuccess { script ->
                                    formats.add(DataPair("Lin Script", script, if (script.scriptData.isEmpty()) 0.75 else 1.0))
                                }
                                "pak" -> PakArchive(spiralContext, dataSource).doOnSuccess { pak ->
                                    when {
                                        pak.files.isEmpty() -> formats.add(DataPair("Pak Archive", pak, 0.2))
                                        pak.files.first().offset < (4 + pak.files.size * 4) -> formats.add(DataPair("Pak Archive", pak, 0.1))
                                        pak.files.fold((4 + pak.files.size * 4).alignedTo(0x10)) { lastOffset, entry ->
                                            if (lastOffset == -1) lastOffset
                                            else if (lastOffset != entry.offset) -1
                                            else entry.offset + entry.size
                                        } != -1 -> formats.add(DataPair("Pak Archive", pak, 1.0))
                                        //TODO: Find out why some pak subfiles are 0x10 aligned
                                        pak.files.fold(pak.files.first().offset) { lastOffset, entry ->
                                            if (lastOffset == -1) lastOffset
                                            else if (lastOffset != entry.offset) -1
                                            else entry.offset + entry.size
                                        } != -1 -> formats.add(DataPair("Pak Archive", pak, 0.95))
                                        else -> formats.add(DataPair("Pak Archive", pak, 0.5))
                                    }
                                }
                                "sfl" -> formats.add(DataPair("SFL", null, 1.0))
                                "spc" -> SpcArchive(spiralContext, dataSource).doOnSuccess { spc ->
                                    formats.add(DataPair("SPC Archive", spc, 1.0))
                                }
                                "srdv" -> {
                                    val lookingFor = "${value.name.toLowerCase().substringBeforeLast('.')}.srd"
                                    val srdFileItem = fileTree.selectionModel.selectedItem.parent.children.firstOrNull { item -> item.value.name == lookingFor }

                                    if (srdFileItem?.value?.dataSource != null) {
                                        SrdArchive(spiralContext, srdFileItem.value.dataSource!!)
                                            .doOnSuccess { srd ->
                                                val textures = srd.entries.filterIsInstance(TextureSrdEntry::class.java)
                                                if (textures.isNotEmpty())
                                                    formats.add(DataPair("Srd Textures", SrdvTexture(textures.toTypedArray()), 1.0))
                                            }
                                    }
                                }
                            }

                            if (formats.isEmpty()) {
                                LinScript(
                                    spiralContext, game as? DrGame.LinScriptable
                                                   ?: DrGame.LinScriptable.Unknown, dataSource
                                ).doOnSuccess { script ->
                                    formats.add(DataPair("Lin Script", script, if (script.scriptData.isEmpty()) 0.5 else 0.75))
                                }

                                PakArchive(spiralContext, dataSource).doOnSuccess { pak ->
                                    when {
                                        pak.files.isEmpty() -> formats.add(DataPair("Pak Archive", pak, 0.2))
                                        pak.files.first().offset < (4 + pak.files.size * 4) -> formats.add(DataPair("Pak Archive", pak, 0.1))
                                        pak.files.fold((4 + pak.files.size * 4).alignedTo(0x10)) { lastOffset, entry ->
                                            if (lastOffset == -1) lastOffset
                                            else if (lastOffset != entry.offset) -1
                                            else entry.offset + entry.size
                                        } != -1 -> formats.add(DataPair("Pak Archive", pak, 1.0))
                                        //TODO: Find out why some pak subfiles are 0x10 aligned
                                        pak.files.fold(pak.files.first().offset) { lastOffset, entry ->
                                            if (lastOffset == -1) lastOffset
                                            else if (lastOffset != entry.offset) -1
                                            else entry.offset + entry.size
                                        } != -1 -> formats.add(DataPair("Pak Archive", pak, 0.95))
                                        else -> formats.add(DataPair("Pak Archive", pak, 0.5))
                                    }
                                }

                                try {
                                    formats.add(DataPair("TGA Image", TGAReader.readImage(spiralContext, data), 1.0))
                                } catch (th: Throwable) {
                                }

                                if ((data[0] == 0xFF.toByte() && data[1] == 0xFE.toByte()) || (data[0] == 0xFE.toByte() && data[1] == 0xFF.toByte())) {
                                    String(data, Charsets.UTF_16)
                                        .trimEnd(NULL_TERMINATOR)
                                        .let { str ->
                                            formats.add(DataPair("UTF-16", Utf16String(str), if (data[data.size - 1] == 0x00.toByte() && data[data.size - 2] == 0x00.toByte()) 1.0 else 0.9))
                                        }
                                } else if (data[data.size - 1] == 0x00.toByte() && data.dropLast(1).all { byte -> byte.toInt().and(0xFF) in ASCII_RANGE }) {
                                    String(data, Charsets.UTF_8)
                                        .trimEnd(NULL_TERMINATOR)
                                        .let { str ->
                                            formats.add(DataPair("UTF-8", Utf8String(str), 0.8))
                                        }
                                }

                                if (data.readInt32LE(0) == 0x53464C4C) {
                                    formats.add(DataPair("SFL", null, 1.0))
                                }
                            }
                        }

                        formats.add(DataPair("Unknown", null, 0.0))

                        withContext(Dispatchers.JavaFx) {
                            dataTypesDropdown.items.addAll(formats.sortedByDescending(DataPair<*>::chance))
                            if (dataTypesDropdown.items.isNotEmpty()) {
                                dataTypesDropdown.selectionModel.clearAndSelect(selectedMap.computeIfAbsent(newValue.value) { 0 })
                            } else {
                                dataTypesDropdown.selectionModel.clearSelection()
                            }
                        }
                    }
                }
            }
        }

        dataTypesDropdown.isEditable = false
        dataTypesDropdown.converter = DataPairCellConverter
        dataTypesDropdown.setCellFactory { DataPairCell() }
        dataTypesDropdown.selectionModel.selectedItemProperty().addListener { _, oldValue, newValue ->
            if (newValue == null)
                return@addListener

            if (oldValue != null && bstTextArea.text != null)
                bstMap[fileTree.selectionModel.selectedItem.value.name to dataTypesDropdown.items.indexOf(oldValue)] = bstTextArea.text

            launch {
                val dataNode: Node?

                freeze(fileTree.selectionModel.selectedItem) { selectedItem ->
                    if (selectedItem?.value != null) {
                        selectedMap[selectedItem.value] = dataTypesDropdown.selectionModel.selectedIndex
                    }
                    var clearChildren = true

                    when (val data = newValue.data) {
                        is PakArchive -> {
                            bstTextArea.text = bstMap.computeIfAbsent(selectedItem.value.name to dataTypesDropdown.selectionModel.selectedIndex) {
                                StringBuilder().apply {
                                    appendLine("parseDataAs(\$FILE_TYPE_PAK)")
                                    appendLine("addMagicNumber(\$MAGIC_NUMBER_PAK)")
                                    appendLine("done()")
                                }.toString()
                            }

                            val root = TreeItem<String>()
                            root.children.addAll(data.files.map { entry -> TreeItem("${entry.index}") })
                            dataNode = TreeView(root)
                            dataNode.isShowRoot = false

                            if (selectedItem.children.isEmpty()) {
                                val subfiles = data.files.map { entry -> TreeItem<LagannTreeData>(LagannTreeData.SubFile(entry.index.toString(), data.openSource(entry))) }

                                withContext(Dispatchers.JavaFx) { selectedItem.children.addAll(subfiles) }
                            }
                            clearChildren = false
                        }
                        is SpcArchive -> {
                            bstTextArea.text = null

                            val root = TreeItem<String>()
                            root.children.addAll(data.files.map { entry -> TreeItem(entry.name) })
                            dataNode = TreeView(root)
                            dataNode.isShowRoot = false

                            if (selectedItem.children.isEmpty()) {
                                val subfiles = data.files.map { entry -> TreeItem(LagannTreeData(entry.name, data.openDecompressedSource(spiralContext, entry).get())) }

                                withContext(Dispatchers.JavaFx) { selectedItem.children.addAll(subfiles) }
                            }
                            clearChildren = false
                        }
                        is LinScript -> {
                            bstTextArea.text = bstMap.computeIfAbsent(selectedItem.value.name to dataTypesDropdown.selectionModel.selectedIndex) {
                                StringBuilder().apply {
                                    appendln("addMagicNumber(\$MAGIC_NUMBER_${(game?.takeIf { it is DrGame.LinScriptable })?.identifier?.toUpperCase() ?: "UNK"}_LIN)")
                                    appendln("done()")
                                }.toString()
                            }
                            val out = BinaryOutputFlow()
                            LinTranspiler(data).transpile(PrintOutputFlowWrapper(out))
                            dataNode = TextArea(String(out.getData()))
                            dataNode.isEditable = false
                        }
                        "LOOP" -> {
                            bstTextArea.text = bstMap.computeIfAbsent(selectedItem.value.name to dataTypesDropdown.selectionModel.selectedIndex) {
                                StringBuilder().apply {
                                    appendln("addMagicNumber(\$MAGIC_NUMBER_DR1_LOOP)")
                                    appendln("done()")
                                }.toString()
                            }

                            dataNode = null
                        }
                        is BufferedImage -> {
                            bstTextArea.text = null

                            val img = WritableImage(data.width, data.height)
                            val writer: PixelWriter = img.pixelWriter
                            val pixels = IntArray(data.width * data.height)
                            data.getRGB(0, 0, data.width, data.height, pixels, 0, data.width)
                            writer.setPixels(0, 0, data.width, data.height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, data.width)
                            dataNode = ImageView(img)
//                    (dataNode as ImageView).maxWidth(dataView.width)
//                    dataNode.maxHeight(dataView.height)
                            dataNode.maxHeight(dataView.height)
                            dataNode.fitWidth = dataView.width * 0.8
                            dataNode.isPreserveRatio = true

                            dataView.heightProperty().addListener { _, _, height ->
                                dataNode.maxHeight(height.toDouble())
                            }
                            dataView.widthProperty().addListener { _, _, width ->
                                dataNode.fitWidth = width.toDouble() * 0.8
                            }
                        }
                        is Utf8String -> {
                            bstTextArea.text = bstMap.computeIfAbsent(selectedItem.value.name to dataTypesDropdown.selectionModel.selectedIndex) {
                                StringBuilder().apply {
                                    appendln("addMagicNumber(\$MAGIC_NUMBER_UTF8)")
                                    appendln("done()")
                                }.toString()
                            }
                            dataNode = TextArea(data.string)
                            dataNode.isEditable = false
                        }
                        is Utf16String -> {
                            bstTextArea.text = null
                            dataNode = TextArea(data.string)
                            dataNode.isEditable = false
                        }
/*                        is SrdvTexture -> {
                            bstTextArea.text = null

                            val root = TreeItem<String>()
                            root.children.addAll(data.textures.map { entry -> TreeItem(entry.rsiEntry.name) })
                            dataNode = TreeView(root)
                            dataNode.isShowRoot = false

                            if (selectedItem.children.isEmpty()) {
                                val baseDataSource = fileTree.selectionModel.selectedItem.value.dataSource!!
                                val subfiles = data.textures.map { srdEntry ->
                                    val dataPair = baseDataSource.useInputFlow { baseFlow ->
                                        val texture = WindowedInputFlow(baseFlow, srdEntry.rsiEntry.resources[0].start.toULong(), srdEntry.rsiEntry.resources[0].length.toULong())

                                        val swizzled = srdEntry.swizzle and 1 != 1
                                        if (srdEntry.format in arrayOf(0x01, 0x02, 0x05, 0x1A)) {
                                            val bytespp: Int

                                            when (srdEntry.format) {
                                                0x01 -> bytespp = 4
                                                0x02 -> bytespp = 2
                                                0x05 -> bytespp = 2
                                                0x1A -> bytespp = 4
                                                else -> bytespp = 2
                                            }

                                            val width: Int = srdEntry.displayWidth //(scanline / bytespp).toInt()
                                            val height: Int = srdEntry.displayHeight

                                            val processing: InputFlow

                                            if (swizzled) {
                                                val processingData = texture.readAndClose()
                                                processingData.deswizzle(width / 4, height / 4, bytespp)
                                                processing = BinaryInputFlow(processingData)
                                            } else
                                                processing = texture

                                            when (srdEntry.format) {
                                                0x01 -> {
                                                    val resultingImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                                                    for (y in 0 until height) {
                                                        for (x in 0 until width) {
                                                            //                                                    val b = processing.read()
                                                            //                                                    val g = processing.read()
                                                            //                                                    val r = processing.read()
                                                            //                                                    val a = processing.read()
                                                            val bgra = processing.readInt32LE()

                                                            resultingImage.setRGB(x, y, bgra ?: break)
                                                        }
                                                    }

                                                    return@useInputFlow DataPair("Srdv Texture (bgra)", resultingImage, 1.0)
                                                }
                                                else -> return@useInputFlow null
                                            }
                                        } else if (srdEntry.format in arrayOf(0x0F, 0x11, 0x14, 0x16, 0x1C)) {
                                            val bytespp: Int

                                            when (srdEntry.format) {
                                                0x0F -> bytespp = 8
                                                0x1C -> bytespp = 16
                                                else -> bytespp = 8
                                            }

                                            var width: Int = srdEntry.displayWidth
                                            var height: Int = srdEntry.displayHeight

                                            if (width % 4 != 0)
                                                width += 4 - (width % 4)

                                            if (height % 4 != 0)
                                                height += 4 - (height % 4)

                                            val processingFlow: InputFlow

                                            if (swizzled && width >= 4 && height >= 4) {
                                                val processingData = texture.readAndClose()
                                                processingData.deswizzle(width / 4, height / 4, bytespp)
                                                processingFlow = BinaryInputFlow(processingData)
                                            } else
                                                processingFlow = BinaryInputFlow(texture.readAndClose())

                                            when (srdEntry.format) {
                                                0x0F -> return@useInputFlow DataPair("Srdv Texture (DXT1)", DXT1PixelData.read(width, height, processingFlow).createPngImage(), 1.0)
                                                0x16 -> return@useInputFlow DataPair("Srdv Texture (BC4)", null, 1.0)
                                                //                                        0x16 -> return BC4PixelData.read(width, height, processingFlow)
                                                0x1C -> return@useInputFlow DataPair("Srdv Texture (BC7)", BC7PixelData.read(width, height, processingFlow).createPngImage(), 1.0)
                                                else -> return@useInputFlow null
                                            }
                                        } else {
                                            return@useInputFlow null
                                        }
                                    }.filterNotNull().getOrElseRun { DataPair("Srdv Texture (${srdEntry.format.toHexString()}", null, 1.0) }
                                    TreeItem<LagannTreeData>(
                                        LagannTreeData.PredefinedSubFile(
                                            srdEntry.rsiEntry.name,
                                            WindowedDataSource(baseDataSource, srdEntry.rsiEntry.resources[0].start.toULong(), srdEntry.rsiEntry.resources[0].length.toULong()),
                                            dataPair
                                        )
                                    )
                                }

                                withContext(Dispatchers.JavaFx) { fileTree.selectionModel.selectedItem.children.addAll(subfiles) }
                            }
                            clearChildren = false
                        }*/
                        else -> {
                            bstTextArea.text = null
                            dataNode = null
                        }
                    }

                    withContext(Dispatchers.JavaFx) {
                        dataView.center = dataNode
                        if (clearChildren && selectedItem?.value?.dataSource != null && selectedItem.children.isNotEmpty()) {
                            selectedItem.children.clear()
                        }
                    }
                }
            }
        }

        root.top = menubar
        root.center = fileTreeSplitPane

        loadFile.accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
        loadFile.setOnAction { event ->
            val fileChooser = FileChooser()
            var tree = Json.parseToJsonElement(configFile.readText()).jsonObject
            val lastFileLoaded = tree["last_file_loaded"]?.jsonPrimitive?.content
            if (lastFileLoaded != null) {
                val file = File(lastFileLoaded)
                fileChooser.initialDirectory = file.parentFile
                fileChooser.initialFileName = file.name
            }

            val selectedFile = fileChooser.showOpenDialog(primaryStage)
            if (selectedFile != null) {
                tree = JsonObject(tree.plus("last_file_loaded", JsonPrimitive(selectedFile.absolutePath)))
                configFile.writeText(Json.encodeToString(tree))
                launch {
                    bstMap.clear()
                    withContext(Dispatchers.JavaFx) { fileTree.root = TreeItem() }

                    WadArchive(spiralContext, AsyncFileDataSource(selectedFile))
                        .doOnSuccess { wad ->
                            fileTree.loadWad(wad, selectedFile.name)

                            withContext(Dispatchers.JavaFx) {
                                val alert = Alert(Alert.AlertType.INFORMATION, "Loaded ${selectedFile.name}", ButtonType.OK)
                                alert.showAndWait()
                            }
                        }.doOnFailure {
                            CpkArchive(spiralContext, AsyncFileDataSource(selectedFile))
                                .doOnSuccess { cpk ->
                                    fileTree.loadCpk(cpk, selectedFile.name)

                                    withContext(Dispatchers.JavaFx) {
                                        val alert = Alert(Alert.AlertType.INFORMATION, "Loaded ${selectedFile.name}", ButtonType.OK)
                                        alert.showAndWait()
                                    }
                                }.doOnFailure {
                                    withContext(Dispatchers.JavaFx) {
                                        val alert = Alert(Alert.AlertType.ERROR, "Was unable to load ${selectedFile.name}; invalid format", ButtonType.OK)
                                        alert.showAndWait()
                                    }
                                }
                        }
                }
            }
        }

        extractFile.accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        extractFile.setOnAction { event ->
            val fileChooser = FileChooser()
            var tree = Json.parseToJsonElement(configFile.readText()).jsonObject
            val lastFileLoaded = tree["last_file_loaded"]?.jsonPrimitive?.content

            if (lastFileLoaded != null) {
                val file = File(lastFileLoaded)
                fileChooser.initialDirectory = file.parentFile
                fileChooser.initialFileName = file.name
            }

            val selectedFile = fileChooser.showOpenDialog(primaryStage)
            if (selectedFile != null) {
                tree = JsonObject(tree.plus("last_file_loaded", JsonPrimitive(selectedFile.absolutePath)))
                configFile.writeText(Json.encodeToString(tree))

                val directoryChooser = DirectoryChooser()
                directoryChooser.initialDirectory = selectedFile.parentFile
                directoryChooser.title = "Extraction Directory"

                val extractDirectory = directoryChooser.showDialog(primaryStage)

                if (extractDirectory != null) {
                    launch {
                        /*AsyncFileDataSource(selectedFile).use { ds ->
                            LagannExtractFilesCommand(
                                spiralContext, DefaultFormatReadContext(selectedFile.absolutePath, game), ds, extractDirectory.absolutePath, ".+",
                                leaveCompressed = false,
                                extractSubfiles = false,
                                predictive = false,
                                convert = false
                            )
                        }*/
                    }
                } else {
                    val alert = Alert(Alert.AlertType.INFORMATION, "Extraction cancelled", ButtonType.OK)
                    alert.showAndWait()
                }
            }
        }

        saveBstMap.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN)
        saveBstMap.setOnAction {
            launch {
                val allItems: MutableSet<LagannTreeData> = HashSet()

                fun recurseTree(item: TreeItem<LagannTreeData>) {
                    if (item.value.dataSource != null) allItems.add(item.value)
                    item.children.forEach(::recurseTree)
                }

                recurseTree(fileTree.root)

                val bstItems = allItems.mapNotNull { data ->
                    val lastIndex = selectedMap[data] ?: return@mapNotNull null
                    val bstText = bstMap[data.name to lastIndex] ?: return@mapNotNull null
                    val charStream = CharStreams.fromString(bstText)
                    val lexer = PipelineLexer(charStream)
                    val tokens = CommonTokenStream(lexer)
                    val parser = PipelineParser(tokens)
                    val visitor = PipelineVisitor()
                    val scope = visitor.visitScope(parser.file().scope())
                    val context = BstContext()
                    scope.run(spiralContext, context)
                    val bst = context.out.getData()
                    if (bst.isEmpty()) return@mapNotNull null
                    return@mapNotNull data.dataSource?.useInputFlow { flow -> flow.sha512HashBytes() }?.to(bst)
                }
            }
        }

        exportData.accelerator = KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN)
        exportData.setOnAction {
            val fileChooser = FileChooser()
            fileChooser.initialFileName = fileTree.selectionModel.selectedItem?.value?.name ?: return@setOnAction
            when (val data = dataTypesDropdown.selectionModel.selectedItem.data) {
                is LinScript -> fileChooser.initialFileName = fileChooser.initialFileName.substringBeforeLast(".lin") + ".osl"
                is BufferedImage -> fileChooser.initialFileName = fileChooser.initialFileName.substringBeforeLast(".tga") + ".png"
                is Utf8String -> fileChooser.initialFileName += ".txt"
                is Utf16String -> fileChooser.initialFileName += ".txt"
            }
            val selectedFile = fileChooser.showSaveDialog(primaryStage)
            if (selectedFile != null) {
                launch {
                    when (val data = dataTypesDropdown.selectionModel.selectedItem.data) {
                        is LinScript -> {
                            AsyncFileOutputFlow(selectedFile).use { out ->
                                LinTranspiler(data).transpile(out)
                            }
                        }
                        is BufferedImage -> {
                            withContext(Dispatchers.IO) {
                                ImageIO.write(data, "PNG", selectedFile)
                            }
                        }
                        is Utf8String -> {
                            AsyncFileOutputFlow(selectedFile).use { out -> out.print(data.string) }
                        }
                        is Utf16String -> {
                            AsyncFileOutputFlow(selectedFile).use { out -> out.print(data.string) }
                        }
                        else -> {
                            AsyncFileOutputFlow(selectedFile).use { out ->
                                fileTree.selectionModel.selectedItem.value.dataSource?.useInputFlow { flow -> flow.copyTo(out) }
                            }
                        }
                    }

                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.INFORMATION, "Saved to ${selectedFile}", ButtonType.OK)
                        alert.showAndWait()
                    }
                }
            }
        }

        copyImage.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN)
        copyImage.setOnAction {
            launch {
                when (val data = dataTypesDropdown.selectionModel.selectedItem.data) {
                    is LinScript -> {
                        BinaryOutputFlow().use { out ->
                            LinTranspiler(data).transpile(PrintOutputFlowWrapper(out))
                            withContext(Dispatchers.JavaFx) {
                                Clipboard.getSystemClipboard().setContent(mapOf(DataFormat.PLAIN_TEXT to String(out.getData())))
                            }
                        }
                    }
                    is BufferedImage -> {
                        withContext(Dispatchers.JavaFx) {
                            val img = WritableImage(data.width, data.height)
                            val writer: PixelWriter = img.pixelWriter
                            val pixels = IntArray(data.width * data.height)
                            data.getRGB(0, 0, data.width, data.height, pixels, 0, data.width)
                            writer.setPixels(0, 0, data.width, data.height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, data.width)

                            Clipboard.getSystemClipboard().setContent(mapOf(DataFormat.IMAGE to img))
                        }
                    }
                    is Utf8String -> {
                        withContext(Dispatchers.JavaFx) {
                            Clipboard.getSystemClipboard().setContent(mapOf(DataFormat.PLAIN_TEXT to data.string))
                        }
                    }
                    is Utf16String -> {
                        withContext(Dispatchers.JavaFx) {
                            Clipboard.getSystemClipboard().setContent(mapOf(DataFormat.PLAIN_TEXT to data.string))
                        }
                    }
                }
            }
        }

        changeGameToDr1.accelerator = KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN)
        changeGameToDr1.setOnAction {
            launch {
                try {
                    val dr1 = Dr1(spiralContext).get()
                    game = dr1

                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.INFORMATION, "Switched game to Dr1", ButtonType.OK)
                        alert.showAndWait()
                    }
                } catch (iae: IllegalArgumentException) {
                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.ERROR, "Could not switch game to Dr1: ${iae.retrieveStackTrace()}", ButtonType.OK)
                        alert.showAndWait()
                    }
                }
            }
        }
        changeGameToDr2.accelerator = KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN)
        changeGameToDr2.setOnAction {
            launch {
                try {
                    val dr2 = Dr2(spiralContext).get()
                    game = dr2

                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.INFORMATION, "Switched game to Dr2", ButtonType.OK)
                        alert.showAndWait()
                    }
                } catch (iae: IllegalArgumentException) {
                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.ERROR, "Could not switch game to Dr2: ${iae.retrieveStackTrace()}", ButtonType.OK)
                        alert.showAndWait()
                    }
                }
            }
        }
        changeGameToUdg.accelerator = KeyCodeCombination(KeyCode.U, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN)
        changeGameToUdg.setOnAction {
            launch {
                try {
                    val udg = UDG(spiralContext).get()
                    game = udg

                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.INFORMATION, "Switched game to UDG", ButtonType.OK)
                        alert.showAndWait()
                    }
                } catch (iae: IllegalArgumentException) {
                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.ERROR, "Could not switch game to UDG: ${iae.retrieveStackTrace()}", ButtonType.OK)
                        alert.showAndWait()
                    }
                }
            }
        }
        changeGameToDrv3.accelerator = KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN)
        changeGameToDrv3.setOnAction {
            launch {
                try {
                    val drv3 = DRv3(spiralContext).get()
                    game = drv3

                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.INFORMATION, "Switched game to DRv3", ButtonType.OK)
                        alert.showAndWait()
                    }
                } catch (iae: IllegalArgumentException) {
                    withContext(Dispatchers.JavaFx) {
                        val alert = Alert(Alert.AlertType.ERROR, "Could not switch game to DRv3: ${iae.retrieveStackTrace()}", ButtonType.OK)
                        alert.showAndWait()
                    }
                }
            }
        }

        launch {
            val context = defaultSpiralContext()

            val loggerFactory = LoggerFactory.getILoggerFactory()
            val baseLogger: Logger
            if (loggerFactory is ch.qos.logback.classic.LoggerContext) {
                context.loadResource("logback.xml")
                    .useAndMapInputFlow { flow -> flow.readBytes() }
                    .doOnSuccess { loggerData ->
                        try {
                            val configurator = JoranConfigurator()
                            configurator.context = loggerFactory
                            // Call context.reset() to clear any previous configuration, e.g. default
                            // configuration. For multi-step configuration, omit calling context.reset().
                            loggerFactory.reset()
                            configurator.doConfigure(ByteArrayInputStream(loggerData))
                        } catch (je: JoranException) {
                            // StatusPrinter will handle this
                        }
                        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerFactory)
                    }

                baseLogger = loggerFactory.getLogger("Lagann")
            } else {
                baseLogger = LoggerFactory.getLogger("Lagann")
            }
            val logger: SpiralLogger = DefaultSpiralLogger(baseLogger)

            spiralContext = context.copy(newLogger = logger)
            configFile = File(spiralContext.getConfigFile(spiralContext, "lagann"))

            withContext(Dispatchers.JavaFx) {
                primaryStage.show()
            }
        }

        primaryStage.scene = scene
    }

    suspend fun TreeView<LagannTreeData>.loadWad(wad: WadArchive, wadName: String? = wad.dataSource.location) {
        val rootName = wadName ?: ""
        val directories = LinkedHashMap<String, MutableList<String>>()

        for (name in wad.files.map(WadFileEntry::name)) {
            var subbedName = name

            while (subbedName.contains('/')) {
                val newSub = subbedName.substringBeforeLast('/')

                if (!directories.containsKey(newSub))
                    directories[newSub] = ArrayList()

                directories.getValue(newSub).add(subbedName)
                subbedName = newSub
            }

            if (!directories.containsKey(rootName))
                directories[rootName] = ArrayList()

            directories.getValue(rootName).add(subbedName)
        }

        suspend fun String.toTreeItem(map: Map<String, List<String>>): TreeItem<LagannTreeData> {
            val entry = wad[this]
            val node = TreeItem(LagannTreeData(this, if (entry != null) wad.openSource(entry) else null))
            if (this in map) {
                for (str in map.getValue(this).distinct()) {
                    node.children.add(str.toTreeItem(map))
                }
            }
            return node
        }

        withContext(Dispatchers.JavaFx) {
            root = rootName.toTreeItem(directories)
        }
    }

    suspend fun TreeView<LagannTreeData>.loadCpk(cpk: CpkArchive, cpkName: String? = cpk.dataSource.location) {
        val rootName = cpkName ?: ""
        val directories = LinkedHashMap<String, MutableList<String>>()

        for (name in cpk.files.map(CpkFileEntry::name)) {
            var subbedName = name

            while (subbedName.contains('/')) {
                val newSub = subbedName.substringBeforeLast('/')

                if (!directories.containsKey(newSub))
                    directories[newSub] = ArrayList()

                directories.getValue(newSub).add(subbedName)
                subbedName = newSub
            }

            if (!directories.containsKey(rootName))
                directories[rootName] = ArrayList()

            directories.getValue(rootName).add(subbedName)
        }

        suspend fun String.toTreeItem(map: Map<String, List<String>>): TreeItem<LagannTreeData> {
            val entry = cpk[this]
            val node = TreeItem(LagannTreeData(this, if (entry != null) cpk.openDecompressedSource(spiralContext, entry).getOrNull() else null))

            if (this in map) {
                for (str in map.getValue(this).distinct()) {
                    node.children.add(str.toTreeItem(map))
                }
            }
            return node
        }

        withContext(Dispatchers.JavaFx) {
            root = rootName.toTreeItem(directories)
        }
    }

    fun lowestPowerOfTwo(num: Int): Int {
        var n = num - 1
        n = n or (n shr 1)
        n = n or (n shr 2)
        n = n or (n shr 4)
        n = n or (n shr 8)
        n = n or (n shr 16)
        return n + 1
    }

    fun compact1By1(num: Int): Int {
        var x = num
        x = x and 0x55555555
        x = (x xor (x shr 1)) and 0x33333333
        x = (x xor (x shr 2)) and 0x0f0f0f0f
        x = (x xor (x shr 4)) and 0x00ff00ff
        x = (x xor (x shr 8)) and 0x0000ffff
        return x
    }

    fun decodeMorton2X(num: Int): Int = compact1By1(num shr 0)
    fun decodeMorton2Y(num: Int): Int = compact1By1(num shr 1)

    fun ByteArray.deswizzle(width: Int, height: Int, bytespp: Int): ByteArray {
        val unswizzled = ByteArray(size)
        val min = kotlin.math.min(width, height)
        val k = log(min.toDouble(), 2.0).toInt()

        for (i in 0 until width * height) {
            val x: Int
            val y: Int

            if (height < width) {
                val j = i shr (2 * k) shl (2 * k) or (decodeMorton2Y(i) and (min - 1)) shl k or (decodeMorton2X(i) and (min - 1)) shl 0
                x = j / height
                y = j % height
            } else {
                val j = i shr (2 * k) shl (2 * k) or (decodeMorton2X(i) and (min - 1)) shl k or (decodeMorton2Y(i) and (min - 1)) shl 0
                x = j % width
                y = j / width
            }

            val p = ((y * width) + x) * bytespp

            for (l in 0 until bytespp) {
                unswizzled[(p + l)] = this[(i * bytespp + l)]
            }
        }

        return unswizzled
    }
}