package info.spiralframework.console.jvm.commands.pilot

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cache
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.text.ProgressTracker
import info.spiralframework.base.common.text.trackProgress
import info.spiralframework.base.common.text.arbitraryProgressBar
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.base.common.text.resetLine
import info.spiralframework.base.jvm.outOrElseGet
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.data.SrdiMesh
import info.spiralframework.console.jvm.data.collada.*
import info.spiralframework.console.jvm.pipeline.PipelineContext
import info.spiralframework.console.jvm.pipeline.PipelineUnion
import info.spiralframework.console.jvm.pipeline.asFlattenedStringIfPresent
import info.spiralframework.console.jvm.pipeline.flattenIfPresent
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.*
import info.spiralframework.core.serialisation.SpiralSerialisation
import info.spiralframework.formats.common.archives.SpcArchive
import info.spiralframework.formats.common.archives.openDecompressedSource
import info.spiralframework.formats.common.archives.srd.*
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.games.UnsafeDr1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.abimon.kornea.img.DXT1PixelData
import org.abimon.kornea.img.bc7.BC7PixelData
import org.abimon.kornea.img.createPngImage
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.InputFlow.Companion.FROM_BEGINNING
import org.abimon.kornea.io.common.flow.WindowedInputFlow
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.jvm.files.FileDataSource
import org.abimon.kornea.io.jvm.files.FileOutputFlow
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
object GurrenPilot : CommandRegistrar {
    /** Helper Variables */
    var keepLooping = AtomicBoolean(true)
    var game: DrGame? = null

    val PERCENT_FORMAT = DecimalFormat("00.00")

    val helpCommands: MutableSet<String> = HashSet()

    suspend fun SpiralContext.extractFilesStub(pipelineContext: PipelineContext, filePath: PipelineUnion.VariableValue?, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        if (filePath is PipelineUnion.VariableValue.ArrayType<*>) {
            filePath.array.forEach { subPath ->
                if (subPath is PipelineUnion.VariableValue.DataSourceType<*>) {
                    subPath.dataSource.use { ds -> extractFiles(ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
                } else {
                    extractFiles(subPath.asString(this, pipelineContext), destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
                }
            }
        } else if (filePath is PipelineUnion.VariableValue.DataSourceType<*>) {
            filePath.dataSource.use { ds -> extractFiles(ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
        } else {
            extractFiles(filePath?.asString(this, pipelineContext), destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
        }
    }

    suspend fun SpiralContext.extractFiles(filePath: String?, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        if (filePath == null) {
            printlnLocale("commands.pilot.extract_files.err_no_file")
            return
        }

        val file = File(filePath)

        if (!file.exists()) {
            printlnLocale("error.file.does_not_exist", filePath)
            return
        }

        if (file.isDirectory) {
            // Directory was passed; this is a potential ambiguity, so don't do anything here
            printlnLocale("commands.pilot.extract_files.err_path_is_directory", filePath)
            return
        } else if (file.isFile) {
            return FileDataSource(file).use { ds -> extractFiles(ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
        } else {
            printlnLocale("commands.pilot.extract_files.err_path_not_file_or_directory", filePath)
            return
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun SpiralContext.extractFiles(archiveDataSource: DataSource<*>, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        if (destDir == null) {
            printlnLocale("commands.pilot.extract_files.err_no_dest_dir")
            return
        }

        val destination = File(destDir)

        if (destination.exists() && !destination.isDirectory) {
            printlnLocale("error.file.not_dir", destination)
            return
        }

        val (decompressedDataSource, archiveCompressionFormats) = if (archiveDataSource.reproducibility.isUnreliable() || archiveDataSource.reproducibility.isUnstable()) {
            archiveDataSource.cache(this).use { ds -> decompress(ds) }
        } else {
            decompress(archiveDataSource)
        }

        val readContext = DefaultFormatReadContext(decompressedDataSource.location?.replace("$(.+?)(?:\\+[0-9a-fA-F]+h|\\[[0-9a-fA-F]+h,\\s*[0-9a-fA-F]+h\\])".toRegex()) { result -> result.groupValues[1] }, game)
        val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_archive", loadedText = null) {
            GurrenShared.EXTRACTABLE_ARCHIVES.map { archive -> archive.identify(this, readContext, decompressedDataSource) }
                    .filter(FormatResult<*>::didSucceed)
                    .sortedBy(FormatResult<*>::chance)
                    .asReversed()
                    .firstOrNull()
        }

        if (result == null) {
            printlnLocale("commands.pilot.extract_files.err_no_format_for", archiveDataSource.location ?: constNull())
            return
        }

        val archive: Any = result.obj.outOrElseGet {
            @Suppress("UNCHECKED_CAST")
            (result.format as ReadableSpiralFormat<out Any>).read(this, readContext, decompressedDataSource).obj
        }

        if (archiveCompressionFormats.isEmpty()) {
            printLocale("commands.pilot.extract_files.archive_type", result.format.name)
        } else {
            printLocale("commands.pilot.extract_files.compressed_archive_type", archiveCompressionFormats.joinToString(" > ", transform = SpiralFormat::name), result.format.name)
        }

        extractFilesFromArchive(decompressedDataSource, archive, readContext.name, destination, filter.toRegex(), leaveCompressed, extractSubfiles, predictive, convert)

        println()
    }

    suspend fun SpiralContext.extractFilesFromArchive(archiveDataSource: DataSource<*>, archive: Any, archiveName: String? = null, destination: File, filter: Regex, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        @Suppress("RedundantIf")
        //This causes an AssertionError if the if else statement is removed
        val files = GurrenShared.getFilesForArchive(this, archive, filter, if (leaveCompressed) true else false, if (predictive) true else false, game
                ?: UnsafeDr1(), archiveName)
        if (files == null) {
            resetLine()
            printLocale("commands.pilot.extract_files.empty_archive")
            return
        }

        val fileCount = files.count().toLong()
        try {
            val progressTracker = ProgressTracker(this, loadingText = localise("commands.pilot.extract_files.extracting_files", fileCount, destination), loadedText = "")
            var copied = 0L

            files.onCompletion {
                progressTracker.finishedDownload()
                resetLine()
                printLocale("commands.pilot.extract_files.finished")
            }.collect { (name, source) ->
                use(source) {
                    val flow = source.openInputFlow()
                    if (flow != null) {
                        use(flow) {
                            val output = File(destination, name)
                            output.parentFile.mkdirs()

                            FileOutputFlow(output).use(flow::copyToOutputFlow)

                            if (extractSubfiles) {
                                val didSubOutput = FileDataSource(output).use subUse@{ subfileDataSource ->
                                    val readContext = DefaultFormatReadContext(name, game)
                                    val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_sub_archive", loadedText = null) {
                                        GurrenShared.READABLE_FORMATS.sortedBy { format ->
                                                    (format.extension ?: "").compareTo(name.substringAfter('.'))
                                                }
                                                .map { archiveFormat -> archiveFormat.identify(this, readContext, subfileDataSource) }
                                                .filter(FormatResult<*>::didSucceed)
                                                .sortedBy(FormatResult<*>::chance)
                                                .asReversed()
                                                .firstOrNull()
                                    }

                                    if (result != null && result.format in GurrenShared.EXTRACTABLE_ARCHIVES) {
                                        val subArchive: Any = result.obj.outOrElseGet {
                                            @Suppress("UNCHECKED_CAST")
                                            (result.format as ReadableSpiralFormat<out Any>).read(this, readContext, subfileDataSource).obj
                                        }

                                        val subOutput = File(destination, name.substringBeforeLast('.'))
                                        extractFilesFromArchive(subfileDataSource, subArchive, name, subOutput, filter, leaveCompressed, extractSubfiles, predictive, convert)

                                        return@subUse true
                                    }

                                    false
                                }

                                if (didSubOutput) output.delete()
                            }

                            if (convert && output.exists()) {
                                val didSubOutput = FileDataSource(output).use subUse@{ subfileDataSource ->
                                    val readContext = DefaultFormatReadContext(name, game)
                                    val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_sub_file", loadedText = null) {
                                        GurrenShared.CONVERTING_FORMATS.keys.sortedBy { format ->
                                                    (format.extension ?: "").compareTo(name.substringAfter('.'))
                                                }
                                                .map { archiveFormat -> archiveFormat.identify(this, readContext, subfileDataSource) }
                                                .filter(FormatResult<*>::didSucceed)
                                                .filter { result -> result.chance >= 0.90 }
                                                .sortedBy(FormatResult<*>::chance)
                                                .asReversed()
                                                .firstOrNull()
                                    }

                                    if (result?.nullableFormat != null) {
                                        @Suppress("UNCHECKED_CAST")
                                        val readFormat = (result.format as ReadableSpiralFormat<out Any>)
                                        val subfile: Any = requireNotNull(result.obj.outOrElseGet {
                                            readFormat.read(this, readContext, subfileDataSource).obj
                                        })

                                        val writeContext = DefaultFormatWriteContext(name, game)
                                        val writeFormat = GurrenShared.CONVERTING_FORMATS.getValue(readFormat)

                                        if (writeFormat.supportsWriting(this, writeContext, subfile)) {
                                            val existingExtension = name.substringAfterLast('.')
                                            val newOutput = File(destination,
                                                    if (existingExtension.equals(readFormat.extension, true) || existingExtension == "dat")
                                                        name.replaceAfterLast('.', writeFormat.extension
                                                                ?: writeFormat.name)
                                                    else
                                                        buildString {
                                                            append(name)
                                                            append('.')
                                                            append(writeFormat.extension ?: writeFormat.name)
                                                        }
                                            )

                                            val writeResult = FileOutputFlow(newOutput).use { out -> writeFormat.write(this, writeContext, subfile, out) }

                                            if (writeResult == FormatWriteResponse.SUCCESS) {
                                                return@subUse true
                                            } else {
                                                newOutput.delete()
                                                return@subUse false
                                            }
                                        } else {
                                            debug("Weird error; $writeFormat does not support writing $subfile")
                                        }
                                    }

                                    false
                                }

                                if (didSubOutput) output.delete()
                            }
                        }
                    }
                }

                progressTracker.trackDownload(copied++, fileCount)
            }
        } finally {
            files.collect { (_, source) ->
                source.close()
            }
        }
    }

    suspend fun SpiralContext.extractModels(pipelineContext: PipelineContext, sourceSpc: PipelineUnion.VariableValue): PipelineUnion.VariableValue? {
        if (sourceSpc is PipelineUnion.VariableValue.ArrayType<*>) {
            return PipelineUnion.VariableValue.ArrayType(
                    sourceSpc.array.mapNotNull { entry -> extractModels(pipelineContext, entry) }
                            .toTypedArray()
            )
        } else {
            return extractModels(sourceSpc.asString(this, pipelineContext))
        }
    }

    suspend fun SpiralContext.extractModels(path: String): PipelineUnion.VariableValue? {
        val fileDataSource = FileDataSource(File(path))
        val flipUVs = true
        val invertXAxis = true
        try {
            val spc = SpcArchive(fileDataSource)

            if (spc == null) {
                println("Invalid spc archive")
                return null
            }

            println("Identifying models...")

            val modelNames = spc.files.map { entry -> entry.name.substringBeforeLast('.') }
                    .distinct()
                    .mapNotNull { name ->
                        val srdEntry = spc["$name.srd"] ?: return@mapNotNull null
                        val srdiEntry = spc["$name.srdi"] ?: return@mapNotNull null

                        Pair(srdEntry, srdiEntry)
                    }

            println("Found: ${modelNames.joinToString { (srdEntry) -> srdEntry.name.substringBeforeLast('.') }}")

            modelNames.forEach { (srdEntry, srdiEntry) ->
                println("Extracting ${srdEntry.name.substringBeforeLast('.')}")
                val srdFile = UnsafeSrdArchive(requireNotNull(spc.openDecompressedSource(this, srdEntry)))
                val vertexMeshEntries = srdFile.entries.filterIsInstance<VTXSrdEntry>()
                val meshEntries = srdFile.entries.filterIsInstance<MeshSrdEntry>()
                val materialEntries = srdFile.entries.filterIsInstance<MaterialsSrdEntry>()
                val textureInfoEntries = srdFile.entries.filterIsInstance<TXISrdEntry>()

                val srdiSource = requireNotNull(spc.openDecompressedSource(this, srdiEntry))

                val meshes = trackProgress(loadingText = "Reading mesh...") {
                    vertexMeshEntries.mapIndexed { index, vtx ->
                        val vertices: MutableList<Vertex> = ArrayList()
                        val normals: MutableList<Vertex> = ArrayList()
                        val uvs: MutableList<UV> = ArrayList()
                        val faces: MutableList<TriFace> = ArrayList()

                        srdiSource.useInputFlow { srdiFlow ->
                            srdiFlow.seek(vtx.faceBlock.start.toLong(), FROM_BEGINNING)

                            for (i in 0 until vtx.faceBlock.length / 6) {
                                val a = requireNotNull(srdiFlow.readInt16LE())
                                val b = requireNotNull(srdiFlow.readInt16LE())
                                val c = requireNotNull(srdiFlow.readInt16LE())

                                faces.add(TriFace(a, b, c))
                            }
                        }

                        srdiSource.useInputFlow { srdiFlow ->
                            srdiFlow.seek(vtx.vertexBlock.start.toLong() + vtx.vertexSizeData[0].offset, FROM_BEGINNING)

                            for (i in 0 until vtx.vertexCount) {
                                val x = requireNotNull(srdiFlow.readFloatLE())
                                val y = requireNotNull(srdiFlow.readFloatLE())
                                val z = requireNotNull(srdiFlow.readFloatLE())

                                val nx = requireNotNull(srdiFlow.readFloatLE())
                                val ny = requireNotNull(srdiFlow.readFloatLE())
                                val nz = requireNotNull(srdiFlow.readFloatLE())

                                vertices.add(Vertex(x, y, z))
                                normals.add(Vertex(nx, ny, nz))

                                if (vtx.vertexSizeData.size > 1 || vtx.vertexSizeData[0].size < 32) {
                                    srdiFlow.skip(requireNotNull((vtx.vertexSizeData[0].size - 12).takeIf { remaining -> remaining >= 0 }).toULong())
                                } else {
                                    val u = requireNotNull(srdiFlow.readFloatLE())
                                    val v = requireNotNull(srdiFlow.readFloatLE())

                                    uvs.add(UV(u, v))
                                    srdiFlow.skip(requireNotNull((vtx.vertexSizeData[0].size - 32).takeIf { remaining -> remaining >= 0 }).toULong())
                                }
                            }
                        }

                        if (vtx.vertexSizeData.size > 2) {
                            srdiSource.useInputFlow { srdiFlow ->
                                srdiFlow.seek(vtx.vertexBlock.start.toLong() + vtx.vertexSizeData[2].offset, FROM_BEGINNING)

                                for (i in 0 until vtx.vertexCount) {
                                    val u = requireNotNull(srdiFlow.readFloatLE())
                                    val v = requireNotNull(srdiFlow.readFloatLE())

                                    uvs.add(UV(u, v))
                                    srdiFlow.skip(requireNotNull((vtx.vertexSizeData[2].size - 8).takeIf { remaining -> remaining >= 0 }).toULong())
                                }
                            }
                        }

                        val mesh = SrdiMesh(vertices.toTypedArray(), uvs.toTypedArray(), faces.toTypedArray())
                        mesh.normals = normals.toTypedArray()
                        mesh.name = vtx.rsiEntry.name

                        try {
                            val meshEntry = meshEntries.first { entry -> entry.meshName == vtx.rsiEntry.name }
                            val materialEntry = materialEntries.first { entry -> entry.rsiEntry.name == meshEntry.materialName }

                            mesh.materialName = meshEntry.materialName
                            mesh.textures = materialEntry.materials.mapValues { (_, textureName) ->
                                textureInfoEntries.first { entry -> entry.rsiEntry.name == textureName }
                            }
                        } catch (th: Throwable) {
                            throw th
                        }

                        trackDownload(index.toLong(), vertexMeshEntries.size.toLong())

                        mesh
                    }
                }

                val colladaMeshes = meshes.mapIndexed { index, mesh ->
                    val vertices: List<Vertex> = if (invertXAxis) mesh.vertices.map { (x, y, z) -> Vertex(x * -1, y, z) } else mesh.vertices.toList()
                    val uvs: List<UV> = if (flipUVs) mesh.uvs.map { (u, v) -> UV(u, 1.0f - v) } else mesh.uvs.toList()
                    val normals: List<Vertex> = if (invertXAxis) mesh.normals?.map { (x, y, z) -> Vertex(x * -1, y, z) }
                            ?: emptyList() else mesh.normals?.toList() ?: emptyList()

                    val verticeSource = ColladaSourcePojo(
                            "vertices_source_mesh_${index}", "vertices_array_${mesh.name ?: "mesh_$index"}",
                            ColladaFloatArrayPojo("vertices_array_mesh_${index}", vertices.flatMap(Vertex::toList).toFloatArray()),
                            ColladaTechniqueCommonPojo.vertexAccessorFor(vertices.size, "#vertices_array_mesh_${index}")
                    )

                    val textureSource = ColladaSourcePojo(
                            "uv_source_mesh_${index}", "uv_array_${mesh.name ?: "mesh_$index"}",
                            ColladaFloatArrayPojo("uv_array_mesh_${index}", uvs.flatMap(UV::toList).toFloatArray()),
                            ColladaTechniqueCommonPojo.uvAccessorFor(uvs.size, "#uv_array_mesh_${index}")
                    )

                    val normalSource = ColladaSourcePojo(
                            "normals_source_mesh_${index}", "normals_array_${mesh.name ?: "mesh_$index"}",
                            ColladaFloatArrayPojo("normals_array_mesh_${index}", normals.flatMap(Vertex::toList).toFloatArray()),
                            ColladaTechniqueCommonPojo.vertexAccessorFor(normals.size, "#normals_array_mesh_${index}")
                    )

                    val verticesPojo = ColladaVerticesPojo(
                            "vertices_mesh_${index}",
                            "vertices_${mesh.name ?: "mesh_$index"}",
                            listOf(ColladaInputUnsharedPojo("POSITION", "#vertices_source_mesh_${index}"))
                    )

                    val triangles: ColladaTrianglesPojo

                    if (mesh.faces.all { (a, b, c) -> a in uvs.indices && b in uvs.indices && c in uvs.indices }) {
                        if (mesh.faces.all { (a, b, c) -> a in normals.indices && b in normals.indices && c in normals.indices }) {
                            triangles = ColladaTrianglesPojo(
                                    listOf(
                                            ColladaInputSharedPojo("VERTEX", "#vertices_mesh_${index}", 0),
                                            ColladaInputSharedPojo("TEXCOORD", "#uv_source_mesh_${index}", 1),
                                            ColladaInputSharedPojo("NORMAL", "#normals_source_mesh_${index}", 2)
                                    ),
                                    mesh.faces.flatMap { (a, b, c) -> listOf(a, a, a, b, b, b, c, c, c) }.toIntArray(),
                                    mesh.materialName
                            )
                        } else {
                            triangles = ColladaTrianglesPojo(
                                    listOf(
                                            ColladaInputSharedPojo("VERTEX", "#vertices_mesh_${index}", 0),
                                            ColladaInputSharedPojo("TEXCOORD", "#uv_source_mesh_${index}", 1)
                                    ),
                                    mesh.faces.flatMap { (a, b, c) -> listOf(a, a, b, b, c, c) }.toIntArray(),
                                    mesh.materialName
                            )
                        }
                    } else {
                        triangles = ColladaTrianglesPojo(
                                listOf(ColladaInputSharedPojo("VERTEX", "#vertices_mesh_${index}", 0)),
                                mesh.faces.flatMap(TriFace::toList).toIntArray(),
                                mesh.materialName
                        )
                    }

                    return@mapIndexed ColladaGeometryPojo(id = "mesh_$index", name = mesh.name
                            ?: "mesh_$index", mesh = ColladaMeshPojo(listOf(verticeSource, textureSource, normalSource), verticesPojo, listOf(triangles)))
                }

                val collada = ColladaPojo(
                        asset = ColladaAssetPojo(contributor = ColladaContributorPojo(authoring_tool = "Spiral Framework", comments = "Autogenerated from ${srdiEntry.name.substringBeforeLast('.')}"), up_axis = ColladaUpAxis.Y_UP),
                        library_geometries = ColladaLibraryGeometriesPojo(geometry = colladaMeshes),
                        library_visual_scenes = ColladaLibraryVisualScenesPojo(visual_scene = listOf(ColladaVisualScenePojo(id = "Scene", node = colladaMeshes.mapIndexed { index, mesh ->
                            return@mapIndexed ColladaNodePojo(type = "NODE", instance_geometry = listOf(ColladaInstanceGeometryPojo(url = "#${mesh.id}", bind_material = ColladaBindMaterialPojo.bindMaterialFor(meshes[index].materialName, "material_${meshes[index].materialName}"))))
                        }))),
                        library_images = ColladaLibraryImagesPojo(textureInfoEntries.map { txi ->
                            ColladaImagePojo(id = txi.fileID, init_from = ColladaInitFromPojo(txi.textureNames[0]))
                        }),

                        library_effects = ColladaLibraryEffectsPojo(effect = materialEntries.filter { mat -> mat.materials.isNotEmpty() }.map { mat ->
                            ColladaEffectPojo(
                                    id = "material_${mat.rsiEntry.name}-effect",
                                    profile_COMMON = listOf(ColladaProfileCommonPojo(
                                            newparam = listOf(
                                                    ColladaNewParamPojo(
                                                            sid = "effect_${mat.rsiEntry.name}-surface",
                                                            surface = ColladaSurfacePojo(ColladaFxSurfaceType.TWO_D, init_from = ColladaInitFromPojo(mat.materials.entries.maxBy(Map.Entry<String, String>::key)!!.value))
                                                    ),
                                                    ColladaNewParamPojo(
                                                            sid = "effect_${mat.rsiEntry.name}-sampler",
                                                            sampler2D = ColladaSampler2DPojo("effect_${mat.rsiEntry.name}-surface")
                                                    )
                                            ),
                                            technique = ColladaTechniqueFxPojo(
                                                    sid = "technique_${mat.rsiEntry.name}",
                                                    phong = ColladaPhongPojo(
                                                            emission = ColladaCommonColorOrTextureTypePojo(Color(0f, 0f, 0f, 1f)),
                                                            ambient = ColladaCommonColorOrTextureTypePojo(Color(0f, 0f, 0f, 1f)),
                                                            diffuse = ColladaCommonFloatOrParamTypePojo(texture = ColladaTexturePojo("effect_${mat.rsiEntry.name}-sampler")),
                                                            specular = ColladaCommonColorOrTextureTypePojo(Color(0.5f, 0.5f, 0.5f, 1f)),
                                                            shininess = ColladaCommonFloatOrParamTypePojo(float = 50f),
                                                            index_of_refraction = ColladaCommonFloatOrParamTypePojo(float = 1f)
                                                    )
                                            )
                                    ))
                            )
                        }),

                        library_materials = ColladaLibraryMaterialsPojo(material = materialEntries.map { mat ->
                            ColladaMaterialPojo(
                                    id = "material_${mat.rsiEntry.name}",
                                    instance_effect = ColladaInstanceEffectPojo(sid = "material_${mat.rsiEntry.name}-instance_effect", url = "#material_${mat.rsiEntry.name}-effect")
                            )
                        }),

                        scene = ColladaScenePojo(
                                instance_visual_scene = ColladaInstanceVisualScenePojo(url = "#Scene")
                        )
                )

                val outputDir = File(File(path.substringBeforeLast('.')), srdEntry.name.substringBeforeLast('.'))
                outputDir.mkdirs()

                (this as SpiralSerialisation).xmlMapper.writeValue(File(outputDir, "model.dae"), collada)
            }
        } finally {
            fileDataSource.close()
        }
        return null
    }

    suspend fun SpiralContext.extractTextures(pipelineContext: PipelineContext, sourceSpc: PipelineUnion.VariableValue): PipelineUnion.VariableValue? {
        if (sourceSpc is PipelineUnion.VariableValue.ArrayType<*>) {
            return PipelineUnion.VariableValue.ArrayType(
                    sourceSpc.array.mapNotNull { entry -> extractTextures(pipelineContext, entry) }
                            .toTypedArray()
            )
        } else {
            return extractTextures(sourceSpc.asString(this, pipelineContext))
        }
    }

    suspend fun SpiralContext.extractTextures(path: String): PipelineUnion.VariableValue? {
        val fileDataSource = FileDataSource(File(path))
        try {
            val spc = SpcArchive(fileDataSource)

            if (spc == null) {
                println("Invalid spc archive")
                return null
            }

            println("Identifying texture sources...")

            val textureSourceNames = spc.files.map { entry -> entry.name.substringBeforeLast('.') }
                    .distinct()
                    .mapNotNull { name ->
                        val srdEntry = spc["$name.srd"] ?: return@mapNotNull null
                        val srdvEntry = spc["$name.srdv"] ?: return@mapNotNull null

                        Pair(srdEntry, srdvEntry)
                    }

            println("Found: ${textureSourceNames.joinToString { (srdEntry) -> srdEntry.name.substringBeforeLast('.') }}")

            textureSourceNames.forEach { (srdEntry, srdvEntry) ->
                val srdFile = UnsafeSrdArchive(requireNotNull(spc.openDecompressedSource(this, srdEntry)))
                val textureEntries = srdFile.entries.filterIsInstance<TextureSrdEntry>()

                val srdvSource = requireNotNull(spc.openDecompressedSource(this, srdvEntry))

                val outputDir = File(File(path.substringBeforeLast('.')), srdEntry.name.substringBeforeLast('.'))
                outputDir.mkdirs()

                textureEntries.forEach { textureEntry ->
                    val texture = srdvSource.useInputFlow { srdvFlow ->
                        val textureFlow = WindowedInputFlow(srdvFlow, textureEntry.rsiEntry.resources[0].start.toULong(), textureEntry.rsiEntry.resources[0].length.toULong())

                        val swizzled = textureEntry.swizzle and 1 != 1
                        if (textureEntry.format in arrayOf(0x01, 0x02, 0x05, 0x1A)) {
                            val bytespp: Int

                            when (textureEntry.format) {
                                0x01 -> bytespp = 4
                                0x02 -> bytespp = 2
                                0x05 -> bytespp = 2
                                0x1A -> bytespp = 4
                                else -> bytespp = 2
                            }

                            val width: Int = textureEntry.displayWidth //(scanline / bytespp).toInt()
                            val height: Int = textureEntry.displayHeight

                            val processing: InputFlow

                            if (swizzled) {
                                val processingData = textureFlow.readBytes()
                                println("ERR: DATA SWIZZLED")
//                                processingData.deswizzle(width / 4, height / 4, bytespp)
                                processing = BinaryInputFlow(processingData)
                            } else
                                processing = textureFlow

                            when (textureEntry.format) {
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

                                    return@useInputFlow resultingImage
                                }
                                else -> return@useInputFlow null
                            }
                        } else if (textureEntry.format in arrayOf(0x0F, 0x11, 0x14, 0x16, 0x1C)) {
                            val bytespp: Int

                            when (textureEntry.format) {
                                0x0F -> bytespp = 8
                                0x1C -> bytespp = 16
                                else -> bytespp = 8
                            }

                            var width: Int = textureEntry.displayWidth
                            var height: Int = textureEntry.displayHeight

                            if (width % 4 != 0)
                                width += 4 - (width % 4)

                            if (height % 4 != 0)
                                height += 4 - (height % 4)

                            val processingFlow: InputFlow

                            if (swizzled && width >= 4 && height >= 4) {
                                val processingData = textureFlow.readBytes()
//                                processingData.deswizzle(width / 4, height / 4, bytespp)
                                println("ERR: DATA SWIZZLED")
                                processingFlow = BinaryInputFlow(processingData)
                            } else
                                processingFlow = BinaryInputFlow(textureFlow.readBytes())

                            when (textureEntry.format) {
                                0x0F -> return@useInputFlow DXT1PixelData.read(width, height, processingFlow).createPngImage()
                                0x16 -> return@useInputFlow null
                                //                                        0x16 -> return BC4PixelData.read(width, height, processingFlow)
                                0x1C -> return@useInputFlow BC7PixelData.read(width, height, processingFlow).createPngImage()
                                else -> return@useInputFlow null
                            }
                        } else {
                            return@useInputFlow null
                        }
                    }

                    if (texture != null) {
                        withContext(Dispatchers.IO) {
                            ImageIO.write(texture, "PNG", File(outputDir, textureEntry.rsiEntry.name))
                        }
                    }
                }
            }
        } finally {
            fileDataSource.close()
        }
        return null
    }

    override suspend fun register(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
        with(pipelineContext) {
            register("extract_files") {
                addParameter("file_path", PipelineUnion.VariableValue.NullType)
                addParameter("dest_dir", PipelineUnion.VariableValue.NullType)
                addParameter("filter", PipelineUnion.VariableValue.StringType(".+"))

                addFlag("leave_compressed")
                addFlag("extract_subfiles")
                addFlag("predictive")
                addFlag("convert")

                setFunction { spiralContext, pipelineContext, parameters ->
                    spiralContext.extractFilesStub(
                            pipelineContext,
                            parameters["FILEPATH"]?.flattenIfPresent(spiralContext, pipelineContext),
                            parameters["DESTDIR"]?.asFlattenedStringIfPresent(spiralContext, pipelineContext),
                            parameters["FILTER"]?.asString(spiralContext, pipelineContext) ?: ".+",
                            parameters["LEAVECOMPRESSED"]?.asBoolean(spiralContext, pipelineContext) ?: false,
                            parameters["EXTRACTSUBFILES"]?.asBoolean(spiralContext, pipelineContext) ?: false,
                            parameters["PREDICTIVE"]?.asBoolean(spiralContext, pipelineContext) ?: false,
                            parameters["CONVERT"]?.asBoolean(spiralContext, pipelineContext) ?: false
                    )

                    null
                }
            }

            register("extract_files_wizard") {
                addParameter("file_path", PipelineUnion.VariableValue.NullType)
                addParameter("dest_dir", PipelineUnion.VariableValue.NullType)
                addParameter("filter", PipelineUnion.VariableValue.StringType(".+"))

                addFlag("leave_compressed")
                addFlag("extract_subfiles")
                addFlag("predictive")
                addFlag("convert")

                setFunction { spiralContext, pipelineContext, parameters ->
                    var filePath = parameters["FILEPATH"]?.flattenIfPresent(spiralContext, pipelineContext)
                    var destDir = parameters["DESTDIR"]?.asFlattenedStringIfPresent(spiralContext, pipelineContext)
                    val filter = parameters["FILTER"]?.asString(spiralContext, pipelineContext) ?: ".+"
                    val leaveCompressed = parameters["LEAVECOMPRESSED"]?.asBoolean(spiralContext, pipelineContext)
                            ?: false
                    val extractSubfiles = parameters["EXTRACTSUBFILES"]?.asBoolean(spiralContext, pipelineContext)
                            ?: false
                    val predictive = parameters["PREDICTIVE"]?.asBoolean(spiralContext, pipelineContext) ?: false
                    val convert = parameters["CONVERT"]?.asBoolean(spiralContext, pipelineContext) ?: false

                    if (filePath == null) {
                        print(spiralContext.localise("commands.pilot.extract.builder.extract"))
                        filePath = readLine()?.doublePadWindowsPaths()?.trim('"')?.let(PipelineUnion.VariableValue::StringType)
                    }

                    if (destDir == null) {
                        print(spiralContext.localise("commands.pilot.extract.builder.dest_dir"))
                        destDir = readLine()?.doublePadWindowsPaths()?.trim('"')
                    }

                    spiralContext.extractFilesStub(pipelineContext, filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)

                    null
                }
            }

            register("extract_models") {
                addParameter("spc_path")

                setFunction { spiralContext, pipelineContext, parameters ->
                    val spcPath = parameters["SPCPATH"]?.flattenIfPresent(spiralContext, pipelineContext)
                            ?: return@setFunction null

                    spiralContext.extractModels(pipelineContext, spcPath)
                }
            }

            register("extract_textures") {
                addParameter("spc_path")

                setFunction { spiralContext, pipelineContext, parameters ->
                    val spcPath = parameters["SPCPATH"]?.flattenIfPresent(spiralContext, pipelineContext)
                            ?: return@setFunction null

                    spiralContext.extractTextures(pipelineContext, spcPath)
                }
            }

            register("show_environment") {
                setFunction { spiralContext, _, _ -> GurrenShared.showEnvironment(spiralContext); null }
            }
        }
    }
}