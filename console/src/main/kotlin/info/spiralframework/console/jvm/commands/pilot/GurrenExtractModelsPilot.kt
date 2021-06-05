package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.objectTypeParameter
import dev.brella.knolus.types.KnolusArray
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.knolus.types.asString
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.BinaryInputFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.extensions.readFloatLE
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.readAndClose
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.coroutines.ascii.progressBar
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.logging.trace
import info.spiralframework.base.common.nullBlock
import info.spiralframework.base.common.nulled
import info.spiralframework.console.jvm.commands.CommandRegistrar
//import info.spiralframework.console.jvm.data.SrdiMesh
//import info.spiralframework.console.jvm.data.collada.*
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.serialisation.SpiralSerialisation
import info.spiralframework.formats.common.archives.SpcArchive
import info.spiralframework.formats.common.archives.openDecompressedSource
import info.spiralframework.formats.common.archives.srd.MaterialsSrdEntry
import info.spiralframework.formats.common.archives.srd.MeshSrdEntry
import info.spiralframework.formats.common.archives.srd.SCNSrdEntry
import info.spiralframework.formats.common.archives.srd.SrdArchive
import info.spiralframework.formats.common.archives.srd.TRESrdEntry
import info.spiralframework.formats.common.archives.srd.TXISrdEntry
import info.spiralframework.formats.common.archives.srd.VTXSrdEntry
import info.spiralframework.formats.common.archives.srd.traverse
import java.awt.Color
import java.io.File
import kotlin.time.ExperimentalTime

object GurrenExtractModelsPilot : CommandRegistrar {
    suspend fun SpiralContext.extractModels(knolusContext: KnolusContext, sourceSpc: KnolusTypedValue) {
        if (sourceSpc is KnolusArray<*>) {
            sourceSpc.array.forEach { entry ->
                extractModels(knolusContext, entry)
            }
        } else {
            sourceSpc.asString(knolusContext).doOnSuccess {
                extractModels(it)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun SpiralContext.extractModels(path: String) {
        val fileDataSource = AsyncFileDataSource(File(path))
        val flipUVs = true
        val invertXAxis = true
        /*try {
            val spc = SpcArchive(fileDataSource)
                .getOrBreak {
                    println("Invalid spc archive")
                    return
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
                val srdFile = spc.openDecompressedSource(this, srdEntry)
                    .useAndFlatMap { src -> SrdArchive(src) }
                    .get()

                val vertexMeshEntries = srdFile.entries.filterIsInstance<VTXSrdEntry>()
                val meshEntries = srdFile.entries.filterIsInstance<MeshSrdEntry>()
                val materialEntries = srdFile.entries.filterIsInstance<MaterialsSrdEntry>()
                val textureInfoEntries = srdFile.entries.filterIsInstance<TXISrdEntry>()

                val sceneNode = srdFile.entries.first { it is SCNSrdEntry } as SCNSrdEntry

                val treeNodes = srdFile.entries.filterIsInstance<TRESrdEntry>()

                val srdiSource = spc.openDecompressedSource(this, srdiEntry)
                    .get()

                val meshes = progressBar(vertexMeshEntries.size.toLong(), loadingText = "Reading mesh...") {
                    srdiSource
                        .openInputFlow()
                        .filterToInstance<InputFlow, SeekableInputFlow> { flow -> KorneaResult.success(BinaryInputFlow(flow.readAndClose())) }
                        .useAndMap { srdiFlow ->
                            vertexMeshEntries.mapIndexedNotNull { index, vtx ->
                                try {
                                    val vertices: MutableList<Vertex> = ArrayList()
                                    val normals: MutableList<Vertex> = ArrayList()
                                    val uvs: MutableList<UV> = ArrayList()
                                    val faces: MutableList<TriFace> = ArrayList()

                                    val meshEntry = meshEntries.firstOrNull { entry -> entry.meshName == vtx.rsiEntry.name }
                                    if (meshEntry == null) {
                                        printlnLocale("No mesh entry found for name {0}", vtx.rsiEntry.name)
                                        return@mapIndexedNotNull null
                                    }
                                    val materialEntry = materialEntries.firstOrNull { entry -> entry.rsiEntry.name == meshEntry.materialName }
                                    if (materialEntry == null) {
                                        printlnLocale("No material entry found for name {0}", meshEntry.materialName)
                                        return@mapIndexedNotNull null
                                    }

                                    vtx.vertexSizeData.forEachIndexed { vtxIndex, (vertexBlockOffset, vertexBlockSize) ->
                                        srdiFlow.seek(vtx.vertexBlock.start + vertexBlockOffset.toLong(), EnumSeekMode.FROM_BEGINNING)

                                        when (vtxIndex) {
                                            //Vertex / Normal data (And Texture UV for boneless models)
                                            0 -> {
                                                info("vtx {0} at {1}, size {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}", vtx.rsiEntry.name, vtx.vertexBlock.start + vertexBlockOffset, vertexBlockSize, vtx.unk2, vtx.unk3, vtx.unk6, vtx.unk7, vtx.unkA, vtx.unkB, vtx.unkC)
                                                val skipSize = requireNotNull((if (vtx.vertexSubBlockCount == 1) {
                                                    if (vtx.unk3 and 0xFF == 5) vertexBlockSize - 44
                                                    else vertexBlockSize - 32
                                                } else {
                                                    vertexBlockSize - 24
                                                }).takeIf { remaining -> remaining >= 0 }) { " vertex data size too small " }.toULong()

                                                repeat(vtx.vertexCount) {
                                                    val x = requireNotNull(srdiFlow.readFloatLE()) { "x was null" }
                                                    val y = requireNotNull(srdiFlow.readFloatLE()) { "y was null" }
                                                    val z = requireNotNull(srdiFlow.readFloatLE()) { "z was null" }

                                                    val nx = requireNotNull(srdiFlow.readFloatLE()) { "nx was null" }
                                                    val ny = requireNotNull(srdiFlow.readFloatLE()) { "ny was null" }
                                                    val nz = requireNotNull(srdiFlow.readFloatLE()) { "nz was null" }

                                                    vertices.add(Vertex(x, y, z))
                                                    normals.add(Vertex(nx, ny, nz))

                                                    if (vtx.vertexSubBlockCount == 1) {
                                                        if (vtx.unk3 and 0xFF == 5) {
                                                            val ax = requireNotNull(srdiFlow.readFloatLE()) { "ax was null" }
                                                            val ay = requireNotNull(srdiFlow.readFloatLE()) { "ax was null" }
                                                            val az = requireNotNull(srdiFlow.readFloatLE()) { "ax was null" }
                                                        }

                                                        val u = requireNotNull(srdiFlow.readFloatLE()) { "u was null" }
                                                        val v = requireNotNull(srdiFlow.readFloatLE()) { "v was null" }

                                                        uvs.add(UV(u, v))
                                                    }

                                                    srdiFlow.skip(skipSize)
                                                }
                                            }

                                            // Bone weights
                                            1 -> {
                                                info("Bone weights for {0}", vtx.rsiEntry.name)
                                            }

                                            //Texture UVs (Only for models with bones)
                                            2 -> {
                                                val u = requireNotNull(srdiFlow.readFloatLE()) { "u was null" }
                                                val v = requireNotNull(srdiFlow.readFloatLE()) { "v was null" }

                                                uvs.add(UV(u, v))

                                                srdiFlow.skip(requireNotNull((vertexBlockSize - 8).takeIf { remaining -> remaining >= 0 }) { " vertex data size too small " }.toULong())
                                            }

                                            else -> error("Unknown vtx index {0}:{1}", vtx.rsiEntry.name, vtxIndex)
                                        }
                                    }

                                    srdiFlow.seek(vtx.faceBlock.start.toLong(), EnumSeekMode.FROM_BEGINNING)

                                    for (i in 0 until vtx.faceBlock.length / 6) {
                                        val a = requireNotNull(srdiFlow.readInt16LE()) { "a was null" }
                                        val b = requireNotNull(srdiFlow.readInt16LE()) { "b was null" }
                                        val c = requireNotNull(srdiFlow.readInt16LE()) { "c was null" }

//                                        faces.add(TriFace(a, b, c))
                                        faces.add(TriFace(c, b, a))
                                    }

                                    val mesh = SrdiMesh(vertices.toTypedArray(), uvs.toTypedArray(), faces.toTypedArray())
                                    mesh.normals = normals.toTypedArray()
//                                    mesh.name = vtx.rsiEntry.name

                                    try {
                                        mesh.name = meshEntry.rsiEntry.name
                                        mesh.materialName = meshEntry.materialName
                                        mesh.textures = materialEntry.materials.mapValues { (_, textureName) ->
                                            textureInfoEntries.firstOrNull { entry -> entry.rsiEntry.name == textureName }
                                        }.filterValues { it != null }.mapValues { (_, v) -> v!! }
                                    } catch (th: Throwable) {
                                        th.printStackTrace()

                                        throw th
                                    }

                                    return@mapIndexedNotNull mesh
                                } finally {
                                    trackProgress(index)
                                }
                            }
                        }
                }.get()

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

                    return@mapIndexed ColladaGeometryPojo(
                        id = "mesh_$index", name = mesh.name
                                                   ?: "mesh_$index", mesh = ColladaMeshPojo(listOf(verticeSource, textureSource, normalSource), verticesPojo, listOf(triangles))
                    )
                }

                fun TRESrdEntry.TreeNode.toNode(): ColladaNodePojo? = when (this) {
                    is TRESrdEntry.TreeNode.Branch -> children.mapNotNull { it.toNode() }.takeUnless(List<*>::isEmpty)?.let { nodes ->
                        ColladaNodePojo(
                            type = "NODE",
                            name = string.substringAfterLast('|'),
                            node = nodes,
//                        instance_geometry = children.filterIsInstance<TRESrdEntry.TreeNode.Leaf>().mapNotNull { node ->
//                            colladaMeshes.withIndex().firstOrNull { (index, mesh) -> mesh.name == node.string }
//                                ?.let { (index, mesh) ->
//                                    ColladaInstanceGeometryPojo(
//                                        url = "#${mesh.id}",
//                                        bind_material = ColladaBindMaterialPojo.bindMaterialFor(meshes[index].materialName, "material_${meshes[index].materialName}")
//                                    )
//                                }
//                        }
                        )
                    }
                    is TRESrdEntry.TreeNode.Leaf -> colladaMeshes.withIndex().firstOrNull {
                            (index, mesh) -> mesh.name == string
                    }?.let { (index, mesh) ->
                        ColladaNodePojo(
                            type = "NODE",
                            name = string.substringAfterLast('|'),
                            instance_geometry = listOf(
                                ColladaInstanceGeometryPojo(
                                    url = "#${mesh.id}",
                                    bind_material = ColladaBindMaterialPojo.bindMaterialFor(meshes[index].materialName, "material_${meshes[index].materialName}")
                                )
                            )
                        )
                    } ?: nullBlock {
                        debug("Node $string does not seem to have any associated mesh")
                    }
                }

                trace {
                    treeNodes.forEach { node ->
                        trace("== Sapling ==")

                        node.tree.traverse().forEach { node ->
                            when (node) {
                                is TRESrdEntry.TreeNode.Branch -> trace("${StringBuilder().apply { repeat(node.nodeDepth) { append('\t') } }}|+ ${node.string.substringAfterLast('|')}")
                                is TRESrdEntry.TreeNode.Leaf -> println("${StringBuilder().apply { repeat(node.parent?.nodeDepth?.plus(1) ?: 0) { append('\t') } }}|* ${node.string.substringAfterLast('|')}")
                            }
                        }

                        trace("== Sapling ==")
                    }
                }

                val collada = ColladaPojo(
                    asset = ColladaAssetPojo(contributor = ColladaContributorPojo(authoring_tool = "Spiral Framework", comments = "Autogenerated from ${srdiEntry.name.substringBeforeLast('.')}"), up_axis = ColladaUpAxis.Y_UP),
                    library_geometries = ColladaLibraryGeometriesPojo(geometry = colladaMeshes),
                    library_visual_scenes = ColladaLibraryVisualScenesPojo(visual_scene = listOf(ColladaVisualScenePojo(id = "Scene", node = listOf(sceneNode.sceneRootNodes.let { sceneRootNodes ->
                        val children = treeNodes.filter { node -> node.tree.string in sceneRootNodes }

                        ColladaNodePojo(
                            type = "NODE",
                            name = "Root",
                            node = children.mapNotNull { it.tree.toNode() }
                        )
                    })))),
                    library_images = ColladaLibraryImagesPojo(textureInfoEntries.map { txi ->
                        ColladaImagePojo(id = txi.fileID, init_from = ColladaInitFromPojo(txi.textureNames[0]))
                    }),

                    library_effects = ColladaLibraryEffectsPojo(effect = materialEntries.filter { mat -> mat.materials.isNotEmpty() }.map { mat ->
                        ColladaEffectPojo(
                            id = "material_${mat.rsiEntry.name}-effect",
                            profile_COMMON = listOf(
                                ColladaProfileCommonPojo(
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
                                )
                            )
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
        } catch (th: Throwable) {
            th.printStackTrace()

            return
        } finally {
            fileDataSource.close()
        }*/

        return
    }

    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        with(knolusContext) {
            registerFunctionWithContextWithoutReturn("extract_models", objectTypeParameter("spc_path")) { context, spcPath ->
                context.spiralContext().doOnSuccess { it.extractModels(context, spcPath) }
            }
        }

        GurrenPilot.help("extract_models")
    }
}