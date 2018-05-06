package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.models.ColladaModelFormat
import org.abimon.spiral.core.formats.models.OBJModelFormat
import org.abimon.spiral.core.objects.archives.ICustomArchive
import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.objects.customPak
import org.abimon.spiral.core.objects.customSPC
import org.abimon.spiral.core.objects.customWAD
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.models.SRDIModel
import org.abimon.spiral.core.objects.models.collada.*
import org.abimon.spiral.core.readInt
import org.abimon.spiral.core.utils.*
import org.abimon.spiral.mvc.gurren.Gurren
import org.abimon.spiral.util.toInt
import org.abimon.visi.util.zip.forEach
import java.io.*
import java.util.HashMap
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.forEach

object ZIPFormat : SpiralFormat {
    override val name = "ZIP"
    override val extension = "zip"
    override val conversions: Array<SpiralFormat> = arrayOf(PAKFormat, SPCFormat, WADFormat, OBJModelFormat, ColladaModelFormat)

    val archiveConversions = arrayOf(PAKFormat, SPCFormat, WADFormat)

    val VALID_HEADERS = intArrayOf(
            toInt(byteArrayOf(0x50, 0x4B, 0x03, 0x04), little = true),
            toInt(byteArrayOf(0x50, 0x4B, 0x05, 0x06), little = true),
            toInt(byteArrayOf(0x50, 0x4B, 0x07, 0x08), little = true)
    )

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
//        try {
//            return source.use { stream ->
//                val zip = ZipInputStream(stream)
//                var count = 0
//                while (zip.nextEntry != null)
//                    count++
//                zip.close()
//                return@use count > 0
//            }
//        } catch (e: NullPointerException) {
//        } catch (e: IOException) {
//        }


        return dataSource().use { stream -> stream.readInt(little = true).toInt() in VALID_HEADERS }
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params)) return true

        when (format) {
            in archiveConversions -> {
                if (format === PAKFormat) {
                    val convert = "${params["pak:convert"] ?: false}".toBoolean()
                    dataSource().use { stream ->
                        val cacheFiles = ArrayList<File>()
                        val customPak = customPak {
                            val zipIn = ZipInputStream(stream)
                            val entries = HashMap<String, () -> InputStream>().apply {
                                zipIn.use { zip ->
                                    zip.forEach { entry ->
                                        if (entry.name.startsWith('.') || entry.name.startsWith("__"))
                                            return@forEach

                                        val (out, data) = CacheHandler.cacheStream()
                                        out.use { stream -> zip.copyTo(stream) }

                                        if (convert) {
                                            val innerFormat = SpiralFormats.formatForData(game, data, "$name/${entry.name}")
                                            val convertTo = innerFormat?.conversions?.firstOrNull()

                                            if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drArchiveFormats) {
                                                val (convOut, convData) = CacheHandler.cacheStream()
                                                innerFormat.convert(game, convertTo, entry.name, context, data, convOut, params)

                                                put(entry.name.substringBeforeLast('.'), convData)
                                                return@forEach
                                            }
                                        }

                                        put(entry.name.substringBeforeLast('.'), data)
                                    }
                                }
                            }

                            entries.filterKeys { key -> key.toIntOrNull() != null }.toSortedMap(Comparator { o1, o2 -> o1.toInt().compareTo(o2.toInt()) }).forEach { i, data ->
                                val (_, cacheFile) = data().use(CacheHandler::cacheStream)

                                add(i.toInt(), cacheFile)
                            }
                        }

                        try {
                            customPak.compile(output)
                        } finally {
                            cacheFiles.forEach { file -> file.delete() }
                        }
                    }

                    return true
                }

                val archiveOperation: ICustomArchive.() -> Unit = {
                    val convert = "${params["${format.extension}:convert"] ?: false}".toBoolean()
                    val zipIn = ZipInputStream(dataSource())
                    zipIn.use { zip ->
                        zip.forEach { entry ->
                            if (entry.name.startsWith(".") || entry.name.startsWith("__"))
                                return@forEach

                            val (data, file) = CacheHandler.cacheStream(zip)

                            if (convert) {
                                val innerFormat = SpiralFormats.formatForData(game, data, "$name/${entry.name}")
                                val convertTo = innerFormat?.conversions?.firstOrNull()

                                if (innerFormat != null && convertTo != null && innerFormat !in SpiralFormats.drArchiveFormats) {
                                    val cacheFile = CacheHandler.newCacheFile()
                                    FileOutputStream(cacheFile).use { convOut -> innerFormat.convert(game, convertTo, entry.name, context, data, convOut, params) }
                                    add(entry.name.replace(innerFormat.extension ?: "unk", convertTo.extension
                                            ?: "unk"), cacheFile)
                                    return@forEach
                                }
                            }

                            add(entry.name, file)
                        }
                    }
                }

                val archive: ICustomArchive = when (format) {
                    SPCFormat -> customSPC(archiveOperation)
                    WADFormat -> customWAD(archiveOperation)
                    else -> TODO("NYI PAK -> ${format::class.simpleName}")
                }

                archive.compile(output)
            }

            OBJModelFormat -> {
                val file = CacheHandler.newCacheFile()
                dataSource().use { stream -> FileOutputStream(file).use { out -> stream.copyTo(out) } }
                try {
                    val zip = ZipFile(file)
                    val entries = zip.entries().toList()
                    val baseNames = entries.map { entry -> entry.name.substringBeforeLast('.') }.distinct()
                    val modelName = baseNames.firstOrNull { rootName -> entries.any { entry -> entry.name == "$rootName.srd" } && entries.any { entry -> entry.name == "$rootName.srdi" } }
                            ?: return false

                    val srdEntry = entries.first { entry -> entry.name == "$modelName.srd" }
                    val srdiEntry = entries.first { entry -> entry.name == "$modelName.srdi" }
                    val srd = SRD { zip.getInputStream(srdEntry) }
                    val srdi = SRDIModel(srd) { zip.getInputStream(srdiEntry) }

                    val flipUVs = "${params["srdi:flipUVs"] ?: true}".toBoolean()
                    val invertXAxis = "${params["srdi:invertX"] ?: true}".toBoolean()

                    val out = PrintStream(output)

                    out.println("# SPIRAL v${Gurren.version}")
                    out.println("# Autogenerated")
                    out.println()

                    var vertexOffset = 0
                    var uvOffset = 0
                    var normalOffset = 0

                    srdi.meshes.forEachIndexed { index, mesh ->
                        out.println("g ${mesh.name ?: "mesh_$index"}")
                        out.println("# ${mesh::class.simpleName}")

                        val vertices: List<Vertex> = if (invertXAxis) mesh.vertices.map { (x, y, z) -> Vertex(x * -1, y, z) } else mesh.vertices.toList()
                        val uvs: List<UV> = if (flipUVs) mesh.uvs.map { (u, v) -> UV(u, 1.0f - v) } else mesh.uvs.toList()
                        val normals: List<Vertex> = if (invertXAxis) mesh.normals?.map { (x, y, z) -> Vertex(x * -1, y, z) }
                                ?: emptyList() else mesh.normals?.toList() ?: emptyList()

                        vertices.map(SPCFormat.decimalFormat::formatTriple).forEach { (x, y, z) -> out.println("v $x $y $z") }
                        uvs.map(SPCFormat.decimalFormat::formatPair).forEach { (u, v) -> out.println("vt $u $v") }
                        normals.map(SPCFormat.decimalFormat::formatTriple).forEach { (x, y, z) -> out.println("vn $x $y $z") }

                        mesh.faces.map { (a, b, c) -> TriFace(c, b, a) }.forEach { (a, b, c) ->
                            if (a in uvs.indices && b in uvs.indices && c in uvs.indices) {
                                if (a in normals.indices && b in normals.indices && c in normals.indices) {
                                    //    out.println("f ${a + 1 + offset}/${a + 1 + offset}/${a + 1 + offset} ${b + 1 + offset}/${b + 1 + offset}/${b + 1 + offset} ${c + 1 + offset}/${c + 1 + offset}/${c + 1 + offset}")
                                    out.println("f ${a + 1 + vertexOffset}/${a + 1 + uvOffset}/${a + 1 + normalOffset} ${b + 1 + vertexOffset}/${b + 1 + uvOffset}/${b + 1 + normalOffset} ${c + 1 + vertexOffset}/${c + 1 + uvOffset}/${c + 1 + normalOffset}")
                                } else {
                                    out.println("f ${a + 1 + vertexOffset}/${a + 1 + uvOffset} ${b + 1 + vertexOffset}/${b + 1 + uvOffset} ${c + 1 + vertexOffset}/${c + 1 + uvOffset}")
                                }
                            } else {
                                out.println("f ${a + 1 + vertexOffset} ${b + 1 + vertexOffset} ${c + 1 + vertexOffset}")
                            }
                        }

                        vertexOffset += vertices.size
                        uvOffset += uvs.size
                        normalOffset = normals.size

                        out.println()
                    }
                } finally {
                    file.delete()
                }
            }

            ColladaModelFormat -> {
                val file = CacheHandler.newCacheFile()
                dataSource().use { stream -> FileOutputStream(file).use { out -> stream.copyTo(out) } }
                try {
                    val zip = ZipFile(file)
                    val entries = zip.entries().toList()
                    val baseNames = entries.map { entry -> entry.name.substringBeforeLast('.') }.distinct()
                    val modelName = baseNames.firstOrNull { rootName -> entries.any { entry -> entry.name == "$rootName.srd" } && entries.any { entry -> entry.name == "$rootName.srdi" } }
                            ?: return false

                    val srdEntry = entries.first { entry -> entry.name == "$modelName.srd" }
                    val srdiEntry = entries.first { entry -> entry.name == "$modelName.srdi" }
                    val srd = SRD { zip.getInputStream(srdEntry) }
                    val srdi = SRDIModel(srd) { zip.getInputStream(srdiEntry) }

                    val flipUVs = "${params["srdi:flipUVs"] ?: true}".toBoolean()
                    val invertXAxis = "${params["srdi:invertX"] ?: true}".toBoolean()

                    val out = PrintStream(output)

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

                        val verticesPojo = ColladaVerticesPojo(
                                "vertices_mesh_${index}",
                                "vertices_${mesh.name ?: "mesh_$index"}",
                                listOf(ColladaInputUnsharedPojo("POSITION", "#vertices_source_mesh_${index}"))
                        )

                        val triangles: ColladaTrianglesPojo

                        if (mesh.faces.all { (a, b, c) -> a in uvs.indices && b in uvs.indices && c in uvs.indices }) {
                            triangles = ColladaTrianglesPojo(
                                    listOf(
                                            ColladaInputSharedPojo("VERTEX", "#vertices_mesh_${index}", 0),
                                            ColladaInputSharedPojo("TEXCOORD", "#uv_source_mesh_${index}", 1)
                                    ),
                                    mesh.faces.flatMap { (a, b, c) -> listOf(a, a, b, b, c, c) }.toIntArray()
                            )
                        } else {
                            triangles = ColladaTrianglesPojo(
                                    listOf(ColladaInputSharedPojo("VERTEX", "#vertices_mesh_${index}", 0)),
                                    mesh.faces.flatMap(TriFace::toList).toIntArray()
                            )
                        }

                        return@mapIndexed ColladaGeometryPojo(id = "mesh_$index", name = mesh.name
                                ?: "mesh_$index", mesh = ColladaMeshPojo(listOf(verticeSource, textureSource), verticesPojo, listOf(triangles)))
//                        out.println("g ${mesh.name ?: "mesh_$index"}")
//                        out.println("# ${mesh::class.simpleName}")
//
//                        val vertices: List<Vertex> = if (invertXAxis) mesh.vertices.map { (x, y, z) -> Vertex(x * -1, y, z) } else mesh.vertices.toList()
//                        val uvs: List<UV> = if (flipUVs) mesh.uvs.map { (u, v) -> UV(u, 1.0f - v) } else mesh.uvs.toList()
//                        val normals: List<Vertex> = if (invertXAxis) mesh.normals?.map { (x, y, z) -> Vertex(x * -1, y, z) }
//                                ?: emptyList() else mesh.normals?.toList() ?: emptyList()
//
//                        vertices.map(SPCFormat.decimalFormat::formatTriple).forEach { (x, y, z) -> out.println("v $x $y $z") }
//                        uvs.map(SPCFormat.decimalFormat::formatPair).forEach { (u, v) -> out.println("vt $u $v") }
//                        normals.map(SPCFormat.decimalFormat::formatTriple).forEach { (x, y, z) -> out.println("vn $x $y $z") }
//
//                        mesh.faces.map { (a, b, c) -> TriFace(c, b, a) }.forEach { (a, b, c) ->
//                            if (a in uvs.indices && b in uvs.indices && c in uvs.indices) {
//                                if (a in normals.indices && b in normals.indices && c in normals.indices) {
//                                    //    out.println("f ${a + 1 + offset}/${a + 1 + offset}/${a + 1 + offset} ${b + 1 + offset}/${b + 1 + offset}/${b + 1 + offset} ${c + 1 + offset}/${c + 1 + offset}/${c + 1 + offset}")
//                                    out.println("f ${a + 1 + vertexOffset}/${a + 1 + uvOffset}/${a + 1 + normalOffset} ${b + 1 + vertexOffset}/${b + 1 + uvOffset}/${b + 1 + normalOffset} ${c + 1 + vertexOffset}/${c + 1 + uvOffset}/${c + 1 + normalOffset}")
//                                } else {
//                                    out.println("f ${a + 1 + vertexOffset}/${a + 1 + uvOffset} ${b + 1 + vertexOffset}/${b + 1 + uvOffset} ${c + 1 + vertexOffset}/${c + 1 + uvOffset}")
//                                }
//                            } else {
//                                out.println("f ${a + 1 + vertexOffset} ${b + 1 + vertexOffset} ${c + 1 + vertexOffset}")
//                            }
//                        }
//
//                        vertexOffset += vertices.size
//                        uvOffset += uvs.size
//                        normalOffset = normals.size
//3
//                        out.println()
                    }

                    val collada = ColladaPojo(
                            asset = ColladaAssetPojo(contributor = ColladaContributorPojo(authoring_tool = "SPIRAL v${Gurren.version}", comments = "Autogenerated from $name"), up_axis = ColladaUpAxis.Y_UP),
                            library_geometries = ColladaLibraryGeometriesPojo(geometry = colladaMeshes),
                            library_visual_scenes = ColladaLibraryVisualScenesPojo(visual_scene = listOf(ColladaVisualScenePojo(id = "Scene", node = colladaMeshes.map { mesh ->
                                return@map ColladaNodePojo(type = "NODE", instance_geometry = listOf(ColladaInstanceGeometryPojo(url = "#${mesh.id}")))
                            }))),

                            scene = ColladaScenePojo(
                                    instance_visual_scene = ColladaInstanceVisualScenePojo(url = "#Scene")
                            )
                    )

                    SpiralData.XML_MAPPER.writeValue(out, collada)
                } finally {
                    file.delete()
                }
            }
        }

        return true
    }
}