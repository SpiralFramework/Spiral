package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.objects.archives.*
import org.abimon.spiral.core.objects.archives.srd.MATEntry
import org.abimon.spiral.core.objects.archives.srd.TXIEntry
import org.abimon.spiral.core.objects.archives.srd.TXREntry
import org.abimon.spiral.core.objects.models.SRDIModel
import org.abimon.spiral.core.objects.models.collada.*
import org.abimon.spiral.core.utils.use
import org.abimon.spiral.modding.HookManager
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.spiral.util.TriFace
import org.abimon.spiral.util.UV
import org.abimon.spiral.util.Vertex
import org.abimon.spiral.util.readTexture
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.relativePathFrom
import java.awt.Color
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO

@Suppress("unused")
object GurrenFileOperation {
    private var backingFile: Any? = null
    private var backingFileList: List<Pair<String, () -> InputStream>>? = null

    val fileList: List<Pair<String, () -> InputStream>>
        get() = backingFileList
                ?: throw IllegalStateException("Attempt to get the archive while operating is null, this is a bug!")

    val info = Command("info", "file-operate") { (params) ->
        val regex = (if (params.size == 1) ".*" else params[1]).toRegex()

        val matching = fileList.filter { (name) -> name.matches(regex) }.map(Pair<String, () -> InputStream>::first)
        if (matching.isEmpty())
            errPrintln("No files matching ${regex.pattern}")
        else
            println(matching.joinToString("\n") { str -> "\t$str" })
    }

    val extractModel = Command("extract_model") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No output provided")

        val output = params[1]
        val models = (if (params.size <= 2) ".*" else params[2]).toRegex()

        val baseNames = fileList.map { (name) -> name.substringBeforeLast('.') }.distinct().filter(models::matches)

        val invertXAxis = true
        val flipUVs = true

        when (output.substringAfterLast('.').toUpperCase()) {
            "DAE" -> {
                val modelName = baseNames.first { name -> fileList.any { (fileName) -> fileName == "$name.srd" } && fileList.any { (fileName) -> fileName == "$name.srdi" } }

                val srd = SRD(fileList.first { (name) -> name == "$modelName.srd" }.second)
                val srdi = SRDIModel(srd, fileList.first { (name) -> name == "$modelName.srdi" }.second)

                SpiralData.XML_MAPPER.writeValue(File(output), ColladaPojo(srdi))
            }
            "ZIP" -> {
                val modelNames = baseNames.filter { name -> fileList.any { (fileName) -> fileName == "$name.srd" } && fileList.any { (fileName) -> fileName == "$name.srdi" } }

                ZipOutputStream(FileOutputStream(File(output))).use { stream ->
                    for (modelName in modelNames) {
                        val srd = SRD(fileList.first { (name) -> name == "$modelName.srd" }.second)
                        val srdi = SRDIModel(srd, fileList.first { (name) -> name == "$modelName.srdi" }.second)
                        val textureEntries = srd.entries.filterIsInstance(TXREntry::class.java)
                        val textureInfoEntries = srd.entries.filterIsInstance(TXIEntry::class.java)
                        val materialEntries = srd.entries.filterIsInstance(MATEntry::class.java)

                        val remappedTextureNames: MutableMap<String, String> = HashMap()

                        if (textureEntries.isNotEmpty()) {
                            val srdv = fileList.firstOrNull { (name) -> name == "$modelName.srdv" }?.second ?: continue

                            textureEntries.forEach { txr ->
                                val name = txr.rsiEntry.name.substringBeforeLast('.')
                                val img = txr.readTexture(srdv)

                                if (img != null) {
                                    val newName = "$name.png"
                                    remappedTextureNames[txr.rsiEntry.name] = newName
                                    stream.putNextEntry(ZipEntry(newName))
                                    ImageIO.write(img, "PNG", stream)
                                    stream.closeEntry()
                                }
                            }
                        }
                        stream.putNextEntry(ZipEntry("$modelName.dae"))

                        val colladaMeshes = srdi.meshes.mapIndexed { index, mesh ->
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
                                asset = ColladaAssetPojo(contributor = ColladaContributorPojo(authoring_tool = "SPIRAL v${Gurren.version}", comments = "Autogenerated from ${SpiralModel.fileOperation?.name}"), up_axis = ColladaUpAxis.Y_UP),
                                library_geometries = ColladaLibraryGeometriesPojo(geometry = colladaMeshes),
                                library_visual_scenes = ColladaLibraryVisualScenesPojo(visual_scene = listOf(ColladaVisualScenePojo(id = "Scene", node = colladaMeshes.mapIndexed { index, mesh ->
                                    return@mapIndexed ColladaNodePojo(type = "NODE", instance_geometry = listOf(ColladaInstanceGeometryPojo(url = "#${mesh.id}", bind_material = ColladaBindMaterialPojo.bindMaterialFor(srdi.meshes[index].materialName, "material_${srdi.meshes[index].materialName}"))))
                                }))),
                                library_images = ColladaLibraryImagesPojo(textureInfoEntries.map { txi ->
                                    ColladaImagePojo(id = txi.fileID, init_from = ColladaInitFromPojo(remappedTextureNames[txi.filename]
                                            ?: txi.filename))
                                }),

                                library_effects = ColladaLibraryEffectsPojo(effect = materialEntries.filter { mat -> "COLORMAP0" in mat.materials }.map { mat ->
                                    ColladaEffectPojo(
                                            id = "material_${mat.rsiEntry.name}-effect",
                                            profile_COMMON = listOf(ColladaProfileCommonPojo(
                                                    newparam = listOf(
                                                            ColladaNewParamPojo(
                                                                    sid = "effect_${mat.rsiEntry.name}-surface",
                                                                    surface = ColladaSurfacePojo(ColladaFxSurfaceType.TWO_D, init_from = ColladaInitFromPojo(mat.materials["COLORMAP0"]
                                                                            ?: error("Material ${mat.rsiEntry.name} has no COLORMAP0")))
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

                        SpiralData.XML_MAPPER.writeValue(stream, collada)
                        stream.closeEntry()
                    }
                }
            }
        }
    }

    val extractTexture = Command("extract_texture") {

    }

    val dump = Command("dump") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No dir specified")

        val dir = File(params[1])

        if (!dir.exists())
            if (!dir.mkdirs())
                return@Command errPrintln("Error creating directory $dir")

        fileList.forEach { (name, ds) ->
            val file = File(dir, name)
            file.parentFile.mkdirs()

            FileOutputStream(file).use { stream -> ds.use { inStream -> inStream.copyTo(stream) } }
        }
    }

    val exit = Command("exit", "file-operate") {
        SpiralModel.scope = "> " to "default"
        SpiralModel.fileOperation = null
    }

    val operateOnFile = Command("operate_file", "default") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file specified")

        val file = File(params[1])

        if (file.exists()) {
            SpiralModel.fileOperation = file

            if (backingFile != null) {
                SpiralModel.scope = "[File Operation ${file.nameWithoutExtension}]|> " to "file-operate"
                println("Now operating on ${file.nameWithoutExtension}")
            }
        }
    }

    fun onArchiveChange(old: File?, new: File?, proceed: Boolean): Boolean {
        if (!proceed || new == null || !new.exists()) {
            backingFile = null
            backingFileList = null
            return false
        }

        if (new.isDirectory) {
            backingFile = new
            backingFileList = new.walkTopDown().map { file -> file relativePathFrom new to file::inputStream }.toList()
            return true
        }

        if (!new.isFile) {
            backingFile = null
            backingFileList = null
            return false
        }

        val ds = new::inputStream
        val pak = Pak(ds)

        if (pak != null) {
            backingFile = pak
            backingFileList = pak.files.map { entry -> entry.index.toString() to entry::inputStream }
            return true
        }

        val wad = WAD(ds)

        if (wad != null) {
            backingFile = wad
            backingFileList = wad.files.map { entry -> entry.name to entry::inputStream }
            return true
        }

        val cpk = try {
            CPK(ds)
        } catch (iae: IllegalArgumentException) {
            null
        }

        if (cpk != null) {
            backingFile = cpk
            backingFileList = cpk.files.map { entry -> "${entry.directoryName}/${entry.fileName}" to entry::inputStream }
            return true
        }

        val spc = try {
            SPC(ds)
        } catch (iae: IllegalArgumentException) {
            null
        }

        if (spc != null) {
            backingFile = spc
            backingFileList = spc.files.map { entry -> entry.name to entry::inputStream }
            return true
        }

        val zip = try {
            ZipFile(new)
        } catch (io: IOException) {
            null
        }

        if (zip != null) {
            backingFile = zip
            backingFileList = zip.entries().toList().map { entry -> entry.name to { zip.getInputStream(entry) } }
            return true
        }

        backingFile = null
        backingFileList = null
        return false
    }

    init {
        HookManager.BEFORE_FILE_OPERATING_CHANGE.add(SpiralData.BASE_PLUGIN to this::onArchiveChange)
        onArchiveChange(null, SpiralModel.fileOperation, true)
    }
}