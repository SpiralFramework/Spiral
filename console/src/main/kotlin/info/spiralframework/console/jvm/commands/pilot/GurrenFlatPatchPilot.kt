package info.spiralframework.console.jvm.commands.pilot

import info.spiralframework.base.common.locale.SpiralLocale
import java.io.File

class GurrenFlatPatchPilot(val locale: SpiralLocale) {
    companion object {
        val DR1_WAD_REGEX = "dr1_data(_keyboard)?(_[a-z]{2})?\\.wad".toRegex()
        val DR1_WAD_LANG_REGEX = "dr1_data(_keyboard)_[a-z]{2}\\.wad".toRegex()
        val DR1_WAD_KB_REGEX = "dr1_data_keyboard(_[a-z]{2})?\\.wad".toRegex()
        val DR1_FILE_SE_REGEX =
            "Dr(\\d|Common)/data/(all|[a-z]{2})/se/se\\d_\\d{3}\\.acb\\.files/HS_SE_\\d{3}\\.wav".toRegex()

        val DR1_EXECUTABLE_REGEX = "DR1_(us)\\.(exe)".toRegex()
        val DR1_STEAM_FOLDER_REGEX = Regex.fromLiteral("Danganronpa Trigger Happy Havoc")
        val DR1_CONTENT_FOLDER_REGEX = Regex.fromLiteral("content")

        val DR2_WAD_REGEX = "dr2_data(_keyboard)?(_[a-z]{2})?\\.wad".toRegex()
        val DR2_WAD_LANG_REGEX = "dr2_data(_keyboard)_[a-z]{2}\\.wad".toRegex()
        val DR2_WAD_KB_REGEX = "dr2_data_keyboard(_[a-z]{2})?\\.wad".toRegex()
        val DR2_FILE_SE_REGEX =
            "Dr(\\d|Common)/data/(all|[a-z]{2})/se/SE\\d_\\d\\.acb\\.files/DR2_SE_\\d{3}\\.wav".toRegex()

        val DR2_EXECUTABLE_REGEX = "DR2_(us)\\.(exe)".toRegex()
        val DR2_STEAM_FOLDER_REGEX = Regex.fromLiteral("Danganronpa 2 Goodbye Despair")
        val DR2_CONTENT_FOLDER_REGEX = Regex.fromLiteral("content")

        val UDG_CPK_REGEX = "a\\d\\.cpk".toRegex()
        val UDG_CPK_STRIPPING_REGEX = "\\D".toRegex()
        val UDG_STEAM_FOLDER_REGEX = Regex.fromLiteral("Danganronpa Another Episode Ultra Despair Girls")
        val UDG_EXECUTABLE_REGEX = Regex.fromLiteral("game.exe")
        val UDG_STEAM_FOLDER_DATA_REGEX = "[a-z]{2}".toRegex()
        val UDG_CONTENT_FOLDER_REGEX = Regex.fromLiteral("data")

        val DRV3_CPK_REGEX = "partition_(data|resident)_(win)(_demo)?(_[a-z]{2})?\\.cpk".toRegex()
        val DRV3_CPK_LANG_REGEX = "partition_(data|resident)_(win)(_demo)?_[a-z]{2}\\.cpk".toRegex()
        val DRV3_CPK_RESIDENT_REGEX = "partition_resident_(win)(_demo)?(_[a-z]{2})?\\.cpk".toRegex()

        val DRV3_EXECUTABLE_REGEX = "Dangan3(Win)\\.exe".toRegex()
        val DRV3_STEAM_FOLDER_REGEX = "Danganronpa V3 Killing Harmony( Demo)?".toRegex()
        val DRV3_CONTENT_FOLDER_REGEX = "win(_demo)?".toRegex()

        fun File.normaliseForDataFiles(): File {
            if (this.name.endsWith(".app") && this.isDirectory)
                return File(this, "Contents")
            else if (this.name.matches(DRV3_STEAM_FOLDER_REGEX))
                return File(this, "data${File.separator}win").takeIf(File::exists)
                        ?: File(this, "data${File.separator}win_demo")
            else if (this.name.matches(UDG_STEAM_FOLDER_REGEX))
                return listFiles().firstOrNull { file -> file.isDirectory && file.name.matches(UDG_STEAM_FOLDER_DATA_REGEX) }
                        ?: File(this, "en")
            else
                return this
        }

        fun File.normaliseForContentFile(): File {
            if (this.name.endsWith(".app") && this.isDirectory)
                return File(this, "Contents")
            else if (this.name.matches(DRV3_STEAM_FOLDER_REGEX))
                return File(this, "data")
            else if (this.name.matches(UDG_STEAM_FOLDER_REGEX))
                return File(this, "data")
            else
                return this
        }

        fun File.normaliseForExecutable(): File =
                if (this.name.endsWith(".app") && this.isDirectory) File(this, "Contents") else this

        fun String.normalisePath(): String = this.replace("/", File.separator).replace("\\", File.separator)
    }

//    val builders = CommandBuilders(parameterParser)
//
//    val prepareWorkspaceRule = makeRuleWith(::ExtractWorkspaceArgs) { argsVar ->
//        Sequence(
//            Localised("commands.pilot.flatpatch.prepare_workspace.base"),
//            Action<Any> { pushMarkerSuccessBase() },
//            Optional(
//                InlineWhitespace(),
//                FirstOf(
//                    Sequence(
//                        Localised("commands.pilot.flatpatch.prepare_workspace.builder"),
//                        Action<Any> { argsVar.get().builder = true; true }
//                    ),
//                    Sequence(
//                        ExistingFilePath(),
//                        Action<Any> { argsVar.get().workplacePath = pop() as? File; true }
//                    )
//                ),
//                ZeroOrMore(
//                    InlineWhitespace(),
//                    Sequence(
//                        Localised("commands.pilot.flatpatch.prepare_workspace.game"),
//                        InlineWhitespace(),
//                        Parameter(),
//                        Action<Any> {
//                            val gameStr = pop() as String
//                            when {
//                                DR1.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = DR1
//                                DR2.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = DR2
//                                V3.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = V3
//                                else -> return@Action pushMarkerFailedLocale(
//                                    locale.localise("commands.pilot.flatpatch.prepare_workspace.err_no_game_for_name", gameStr)
//                                )
//                            }
//
//                            return@Action true
//                        }
//                    )
//                ),
//                Action<Any> { pushMarkerSuccessCommand() }
//            )
//        )
//    }
//
//    val patchExecutableRule = makeRuleWith(::PatchExecutableArgs) { argsVar ->
//        Sequence(
//            Localised("commands.pilot.flatpatch.patch_executable.base"),
//            Action<Any> { pushMarkerSuccessBase() },
//            Optional(
//                InlineWhitespace(),
//                FirstOf(
//                    Sequence(
//                        Localised("commands.pilot.flatpatch.patch_executable.builder"),
//                        Action<Any> { argsVar.get().builder = true; true }
//                    ),
//                    Sequence(
//                        ExistingFilePath(),
//                        Action<Any> { argsVar.get().executablePath = pop() as? File; true }
//                    )
//                ),
//                ZeroOrMore(
//                    InlineWhitespace(),
//                    Sequence(
//                        Localised("commands.pilot.flatpatch.patch_executable.game"),
//                        InlineWhitespace(),
//                        Parameter(),
//                        Action<Any> {
//                            val gameStr = pop() as String
//                            when {
//                                DR1.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = DR1
//                                DR2.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = DR2
//                                V3.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = V3
//                                else -> return@Action pushMarkerFailedLocale(
//                                    locale.localise("commands.pilot.flatpatch.patch_executable.err_no_game_for_name", gameStr)
//                                )
//                            }
//
//                            return@Action true
//                        }
//                    )
//                ),
//                Action<Any> { pushMarkerSuccessCommand() }
//            )
//        )
//    }
//
//    val linkContentRule = makeRuleWith(::LinkContentArgs) { argsVar ->
//        Sequence(
//            FirstOf(
//                Sequence(
//                    Localised("commands.pilot.flatpatch.link_content.base"),
//                    Action<Any> { argsVar.get().createLink = true; true }
//                ),
//                Sequence(
//                    Localised("commands.pilot.flatpatch.link_content.unlink"),
//                    Action<Any> { argsVar.get().createLink = false; true }
//                )
//            ),
//            Action<Any> { pushMarkerSuccessBase() },
//            Optional(
//                InlineWhitespace(),
//                FirstOf(
//                    Sequence(
//                        Localised("commands.pilot.flatpatch.link_content.builder"),
//                        Action<Any> { argsVar.get().builder = true; true }
//                    ),
//                    Sequence(
//                        ExistingFilePath(),
//                        Action<Any> { argsVar.get().workspacePath = pop() as? File; true }
//                    )
//                ),
//                ZeroOrMore(
//                    InlineWhitespace(),
//                    FirstOf(
//                        Sequence(
//                            Localised("commands.pilot.flatpatch.link_content.linking_name"),
//                            InlineWhitespace(),
//                            Parameter(),
//                            Action<Any> { argsVar.get().linkingContentName = pop() as? String; true }
//                        ),
//                        Sequence(
//                            Localised("commands.pilot.flatpatch.link_content.linking_path"),
//                            InlineWhitespace(),
//                            ExistingFilePath(),
//                            Action<Any> { argsVar.get().linkingContentPath = pop() as? File; true }
//                        ),
//                        Sequence(
//                            Localised("commands.pilot.flatpatch.link_content.game"),
//                            InlineWhitespace(),
//                            Parameter(),
//                            Action<Any> {
//                                val gameStr = pop() as String
//                                when {
//                                    DR1.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = DR1
//                                    DR2.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = DR2
//                                    V3.names.any { str -> str.equals(gameStr, true) } -> argsVar.get().game = V3
//                                    else -> return@Action pushMarkerFailedLocale(
//                                        locale.localise("commands.pilot.flatpatch.link_content.err_no_game_for_name", gameStr)
//                                    )
//                                }
//
//                                return@Action true
//                            }
//                        ),
//                        Sequence(
//                            Localised("commands.pilot.flatpatch.link_content.filter"),
//                            InlineWhitespace(),
//                            Filter(),
//                            Action<Any> { argsVar.get().filter = pop() as? Regex; true }
//                        )
//                    )
//                ),
//                Action<Any> { pushMarkerSuccessCommand() }
//            )
//        )
//    }
//
//    val prepareWorkspace = ParboiledCommand(prepareWorkspaceRule) { stack ->
//        val workspaceBuilder = stack[0] as ExtractWorkspaceArgs
//
//        //Step 1. Check all our data's there
//
//        if (workspaceBuilder.workplacePath == null || workspaceBuilder.game == null || workspaceBuilder.builder) {
//            if (workspaceBuilder.workplacePath == null) {
//                printLocale("commands.pilot.flatpatch.prepare_workspace.builder.workspace")
//                workspaceBuilder.workplacePath = builders.filePath()
//            }
//
//            if (workspaceBuilder.game == null) {
//                workspaceBuilder.workplacePath?.takeIf(File::isDirectory)?.let { path ->
//                    workspaceBuilder.game = when {
//                        path.name.matches(DR1_STEAM_FOLDER_REGEX) -> DR1
//                        path.name.matches(DR2_STEAM_FOLDER_REGEX) -> DR2
//                        path.name.matches(UDG_STEAM_FOLDER_REGEX) -> UDG
//                        path.name.matches(DRV3_STEAM_FOLDER_REGEX) -> V3
//                        else -> null
//                    }
//                }
//            }
//
//            if (workspaceBuilder.game == null) {
//                printLocale("commands.pilot.flatpatch.prepare_workspace.builder.game")
//                workspaceBuilder.game = builders.parameter()?.let { gameStr ->
//                    when {
//                        DR1.names.any { str -> str.equals(gameStr, true) } -> return@let DR1
//                        DR2.names.any { str -> str.equals(gameStr, true) } -> return@let DR2
//                        UDG.names.any { str -> str.equals(gameStr, true) } -> return@let UDG
//                        V3.names.any { str -> str.equals(gameStr, true) } -> return@let V3
//                        else -> return@ParboiledCommand fail(
//                            "commands.pilot.flatpatch.prepare_workspace.builder.err_no_game_for_name",
//                            gameStr
//                        )
//                    }
//                } ?: return@ParboiledCommand fail(
//                    "commands.pilot.flatpatch.prepare_workspace.builder.err_no_game_for_name",
//                    ""
//                )
//            }
//        }
//
//        workspaceBuilder.workplacePath = workspaceBuilder.workplacePath?.normaliseForDataFiles()
//        val args = workspaceBuilder.makeImmutable()
//
//        if (args.workplacePath == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.prepare_workspace.err_no_workspace")
//
//        if (!args.workplacePath.exists())
//            return@ParboiledCommand fail("commands.pilot.flatpatch.prepare_workspace.err_workspace_doesnt_exist")
//
//        if (!args.workplacePath.isDirectory)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.prepare_workspace.err_workspace_not_directory")
//
//        if (args.game == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.prepare_workspace.err_no_game")
//
//        when (args.game) {
//            DR1 -> prepareWadGame(
//                args.workplacePath,
//                DR1_WAD_REGEX,
//                DR1_WAD_LANG_REGEX,
//                DR1_WAD_KB_REGEX,
//                DR1_FILE_SE_REGEX
//            )
//            DR2 -> prepareWadGame(
//                args.workplacePath,
//                DR2_WAD_REGEX,
//                DR2_WAD_LANG_REGEX,
//                DR2_WAD_KB_REGEX,
//                DR2_FILE_SE_REGEX
//            )
//            UDG -> prepareCpkGame(
//                args.workplacePath,
//                UDG_CPK_REGEX,
//                true,
//                true
//            ) { file -> file.nameWithoutExtension.replace(UDG_CPK_STRIPPING_REGEX, "").toIntOrNull() ?: 0 }
//            V3 -> prepareCpkGame(args.workplacePath, DRV3_CPK_REGEX, false, false) { file ->
//                var weight = 0
//                if (file.name.matches(DRV3_CPK_LANG_REGEX))
//                    weight = weight or 0b010
//                if (file.name.matches(DRV3_CPK_RESIDENT_REGEX))
//                    weight = weight or 0b100
//                weight
//            }
//            else -> TODO(args.game.toString())
//        }
//
//        return@ParboiledCommand SUCCESS
//    }
//
//    suspend fun SpiralContext.prepareWadGame(workplacePath: File, wadRegex: Regex, langRegex: Regex, kbRegex: Regex, seRegex: Regex) {
//        val wadFiles = workplacePath.listFiles().filter { file -> file.name.matches(wadRegex) }
//            .sortedBy { file ->
//                var weight = 0
//                if (file.name.matches(langRegex))
//                    weight = weight or 0b010
//                if (file.name.matches(kbRegex))
//                    weight = weight or 0b100
//                return@sortedBy weight
//            }
//
//        //We're gonna try something here:
//        //If we extract all the files to a folder - let's call it base_game - and then we create the operational folder called content, we should be able to create symlinks between them
//
//        val baseGamePath = File(workplacePath, "base_game")
//        baseGamePath.mkdir()
//
//        val contentPath = File(workplacePath, "content")
//        contentPath.mkdir()
//
//        val backupWadPath = File(workplacePath, "backup_wads")
//        backupWadPath.mkdir()
//
//        wadFiles.forEach { wadFile ->
//            val wad = WAD(this, wadFile::inputStream)
//                ?: return@forEach printlnLocale(
//                    "commands.pilot.flatpatch.prepare_workspace.err_not_wad",
//                    wadFile.name
//                )
//
//            if (wad.files.isEmpty())
//                return@forEach printlnLocale(
//                    "commands.pilot.flatpatch.prepare_workspace.err_wad_no_files",
//                    wadFile.name
//                )
//
//            val extractTime = measureTimeMillis {
//                val files = wad.files.sortedBy(WADFileEntry::offset)
//
//                arbitraryProgressBar(
//                    loadingText = localise(
//                        "commands.pilot.flatpatch.prepare_workspace.extracting",
//                        wadFile.name
//                    ), loadedText = ""
//                ) {
//                    wad.directories.forEach { entry ->
//                        val name = entry.name.normalisePath()
//                        val baseGameDir = File(baseGamePath, name)
//                        val contentDir = File(contentPath, name)
//
//                        baseGameDir.mkdirs()
//                        contentDir.mkdirs()
//                    }
//                }
//
////                val totalCount = files.size.toLong()
////                var extracted: Long = 0
//                arbitraryProgressBar(
//                    loadingText = localise(
//                        "commands.pilot.flatpatch.prepare_workspace.extracting",
//                        wadFile.name
//                    ), loadedText = ""
//                ) {
//                    //trackDownload(0, totalCount)
//                    files.forEach { entry ->
//                        val file = File(baseGamePath, entry.name.normalisePath())
//                        entry.inputStream.use { stream -> FileOutputStream(file).use(stream::copyToStream) }
//                        //trackDownload(++extracted, totalCount)
//                    }
//                }
//            }
//            val linkTime = measureTimeMillis {
//                arbitraryProgressBar(
//                    loadingText = localise(
//                        "commands.pilot.flatpatch.prepare_workspace.linking",
//                        wadFile.name
//                    ), loadedText = ""
//                ) {
//                    wad.files.forEach { entry ->
//                        val name = entry.name.normalisePath()
//                        val contentFile = File(contentPath, name)
//                        val baseGameFile = File(baseGamePath, name)
//
//                        if (!contentFile.exists()) {
//                            try {
//                                Files.createLink(contentFile.toPath(), baseGameFile.toPath())
//                            } catch (io: IOException) {
//                                io.printStackTrace()
//                                return@arbitraryProgressBar
//                            }
//                        }
//
//                        if (entry.name.matches(seRegex)) {
//                            val oggContentFile = File(contentPath, name.replace(".wav", ".ogg"))
//                            if (!oggContentFile.exists()) {
//                                try {
//                                    Files.createLink(oggContentFile.toPath(), contentFile.toPath())
//                                } catch (io: IOException) {
//                                    io.printStackTrace()
//                                    return@arbitraryProgressBar
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            printlnLocale(
//                "commands.pilot.flatpatch.prepare_workspace.extracted_wad",
//                wadFile.name,
//                extractTime,
//                linkTime
//            )
//
//            val empty = customWAD {
//                major = wad.major
//                minor = wad.minor
//            }
//
//            val backupWadDest = File(backupWadPath, wadFile.name)
//            if (!backupWadDest.exists())
//                wadFile.renameTo(backupWadDest)
//
//            FileOutputStream(wadFile).use(empty::compile)
//        }
//    }
//
//    suspend fun SpiralContext.prepareCpkGame(
//        workplacePath: File,
//        cpkRegex: Regex,
//        makeEmptyArchives: Boolean,
//        contentIsParent: Boolean,
//        weighting: (File) -> Int
//    ) {
//        val cpkFiles = workplacePath.listFiles().filter { file -> file.name.matches(cpkRegex) }
//            .sortedBy(weighting)
//
//        //V3 uses a slightly different system to the previous games; for one, it already has a folder to put the files in
//        //Our path has been normalised, and what we want to do is create a parent to store our files in
//
//        val parentPath = workplacePath.absoluteFile.parentFile
//
//        val baseGamePath = File(parentPath, "base_game")
//        baseGamePath.mkdir()
//
//        val contentPath = if (contentIsParent) parentPath else workplacePath
//
//        val backupCpkPath = File(parentPath, "backup_cpks")
//        backupCpkPath.mkdir()
//
//        cpkFiles.forEach { cpkFile ->
//            val cpk = CPK(this, cpkFile::inputStream)
//                ?: return@forEach printlnLocale(
//                    "commands.pilot.flatpatch.prepare_workspace.err_not_cpk",
//                    cpkFile.name
//                )
//
//            if (cpk.files.isEmpty())
//                return@forEach printlnLocale(
//                    "commands.pilot.flatpatch.prepare_workspace.err_cpk_no_files",
//                    cpkFile.name
//                )
//
//            val extractTime = measureTimeMillis {
//                val files = cpk.files.sortedBy(CPKFileEntry::offset)
//
//                ProgressTracker(
//                        downloadingText = localise(
//                                "commands.pilot.flatpatch.prepare_workspace.extracting",
//                                cpkFile.name
//                        ), downloadedText = ""
//                ) {
//                    //Due to the **hefty** nature of these files, we're gonna chunk them to give some feedback to the user
//
//                    files.map(CPKFileEntry::directoryName).distinct().forEach { originalName ->
//                        val name = originalName.normalisePath()
//                        val baseGameDir = File(baseGamePath, name)
//                        val contentDir = File(contentPath, name)
//
//                        baseGameDir.mkdirs()
//                        contentDir.mkdirs()
//                    }
//
//                    val chunks = files.chunked(maxOf(files.size / 100, 1))
//                    chunks.forEachIndexed { index, chunk ->
//                        chunk.forEach { entry ->
//                            val file = File(baseGamePath, entry.name.normalisePath())
//                            entry.inputStream.use { stream -> FileOutputStream(file).use(stream::copyToStream) }
//                        }
//                        trackDownload(index.toLong(), chunks.size.toLong())
//                    }
//                }
//            }
//            val linkTime = measureTimeMillis {
//                arbitraryProgressBar(
//                    loadingText = localise(
//                        "commands.pilot.flatpatch.prepare_workspace.linking",
//                        cpkFile.name
//                    ), loadedText = ""
//                ) {
//                    cpk.files.forEach { entry ->
//                        val name = entry.name.normalisePath()
//                        val contentFile = File(contentPath, name)
//                        val baseGameFile = File(baseGamePath, name)
//
//                        if (!contentFile.exists()) {
//                            try {
//                                Files.createLink(contentFile.toPath(), baseGameFile.toPath())
//                            } catch (io: IOException) {
//                                io.printStackTrace()
//                                return@arbitraryProgressBar
//                            }
//                        }
//                    }
//                }
//            }
//            printlnLocale(
//                "commands.pilot.flatpatch.prepare_workspace.extracted_cpk",
//                cpkFile.name,
//                extractTime,
//                linkTime
//            )
//
//            val backupCpkDest = File(backupCpkPath, cpkFile.name)
//            if (!backupCpkDest.exists())
//                cpkFile.renameTo(backupCpkDest)
//
//            if (makeEmptyArchives) {
//                GurrenFlatPatchPilot::class.java.classLoader.getResourceAsStream("empty.cpk")?.use { empty ->
//                    FileOutputStream(cpkFile).use(empty::copyToStream)
//                } ?: this.error("commands.pilot.flatpatch.prepare_workspace.err_no_empty_cpk")
//            }
//        }
//    }
//
//    val patchExecutable = ParboiledCommand(patchExecutableRule) { stack ->
//        if (this !is SpiralSerialisation)
//            throw IllegalStateException(localise("gurren.errors.invalid_context", localise(this::class.qualifiedName ?: "constants.null"), localise(SpiralSerialisation::class.qualifiedName ?: "constants.null")))
//
//        val executableBuilder = stack[0] as PatchExecutableArgs
//
//        //Step 1. Check all our data's there
//
//        executableBuilder.executablePath = executableBuilder.executablePath?.normaliseForExecutable()
//        if (executableBuilder.executablePath == null || executableBuilder.game == null || executableBuilder.builder) {
//            if (executableBuilder.executablePath == null) {
//                printLocale("commands.pilot.flatpatch.patch_executable.builder.executable")
//                executableBuilder.executablePath = builders.filePath()?.normaliseForExecutable()
//            }
//
//            if (executableBuilder.game == null) {
//                executableBuilder.executablePath?.takeIf(File::isFile)?.let { path ->
//                    executableBuilder.game = when {
//                        path.name.matches(DR1_EXECUTABLE_REGEX) -> DR1
//                        path.name.matches(DR2_EXECUTABLE_REGEX) -> DR2
//                        path.name.matches(DRV3_EXECUTABLE_REGEX) -> V3
//                        else -> null
//                    }
//                }
//
//                executableBuilder.executablePath?.takeIf(File::isDirectory)?.let { path ->
//                    executableBuilder.game = when {
//                        path.name.matches(DR1_STEAM_FOLDER_REGEX) -> DR1
//                        path.name.matches(DR2_STEAM_FOLDER_REGEX) -> DR2
//                        path.name.matches(DRV3_STEAM_FOLDER_REGEX) -> V3
//                        else -> null
//                    }
//                }
//            }
//
//            if (executableBuilder.game == null) {
//                printLocale("commands.pilot.flatpatch.patch_executable.builder.game")
//                executableBuilder.game = builders.parameter()?.let { gameStr ->
//                    if (DR1.names.any { str -> str.equals(gameStr, true) })
//                        return@let DR1
//                    if (DR2.names.any { str -> str.equals(gameStr, true) })
//                        return@let DR2
//                    if (V3.names.any { str -> str.equals(gameStr, true) })
//                        return@let V3
//                    return@ParboiledCommand fail(
//                        "commands.pilot.flatpatch.patch_executable.builder.err_no_game_for_name",
//                        gameStr
//                    )
//                } ?: return@ParboiledCommand fail(
//                    "commands.pilot.flatpatch.patch_executable.builder.err_no_game_for_name",
//                    ""
//                )
//            }
//        }
//
//        executableBuilder.executablePath?.takeIf(File::isDirectory)?.let { executablePath ->
//            executableBuilder.game?.let { game ->
//                val regex = when (game) {
//                    DR1 -> DR1_EXECUTABLE_REGEX
//                    DR2 -> DR2_EXECUTABLE_REGEX
//                    UDG -> UDG_EXECUTABLE_REGEX
//                    V3 -> DRV3_EXECUTABLE_REGEX
//                    else -> TODO("Make regex for $game executable")
//                }
//                executableBuilder.executablePath = executablePath.listFiles()
//                    .firstOrNull { file -> file.name.matches(regex) }
//                    ?: return@ParboiledCommand fail("commands.pilot.flatpatch.patch_executable.err_no_executable_child")
//            }
//        }
//
//        val args = executableBuilder.makeImmutable()
//
//        if (args.executablePath == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.patch_executable.err_no_workspace")
//
//        if (!args.executablePath.exists())
//            return@ParboiledCommand fail("commands.pilot.flatpatch.patch_executable.err_workspace_doesnt_exist")
//
//        if (args.game == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.patch_executable.err_no_game")
//
//        when (args.game) {
//            in arrayOf(DR1, DR2) -> {
//                //First thing's first, we make a backup
//                val backup = File(args.executablePath.absolutePath + ".backup").let { backup ->
//                    return@let if (backup.exists()) {
//                        File(
//                            args.executablePath.absolutePath + ".sha256_${FileInputStream(args.executablePath).use(
//                                InputStream::sha256Hash
//                            )}.backup"
//                        )
//                    } else {
//                        backup
//                    }
//                }
//                Files.copy(args.executablePath.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)
//
//                RandomAccessFile(args.executablePath, "rw").use { raf ->
//                    //Next up, check for an existing map
//                    val executableMapFile = File(args.executablePath.absolutePath + ".smap")
//                    val executableMap: SpiralDR1ExecutableMap
//
//                    if (!executableMapFile.exists()) {
//                        val strings: MutableMap<Long, String> = HashMap()
//                        val builder = StringBuilder()
//                        var startingAddress: Long = -1
//                        val testedRange = 32..126 //We only really care about ASCII strings
//
//                        //Find all the strings
//                        arbitraryProgressBar(
//                            loadingText = localise(
//                                "commands.pilot.flatpatch.patch_executable.mapping",
//                                args.executablePath.name
//                            ), loadedText = "commands.pilot.flatpatch.patch_executable.finished_mapping"
//                        ) {
//                            for (i in 0 until raf.length()) {
//                                val chr = raf.read()
//                                if (chr in testedRange) {
//                                    if (builder.isEmpty())
//                                        startingAddress = i
//                                    builder.append(chr.toChar())
//                                } else if (chr == 0x00) {
//                                    builder.toString().takeIf(String::isNotEmpty)
//                                        ?.let { str -> strings[startingAddress] = str }
//                                    builder.clear()
//                                } else {
//                                    builder.clear()
//                                }
//                            }
//                        }
//
//                        executableMap = SpiralDR1ExecutableMap(
//                            archiveLocations = strings.mapValues { (_, value) ->
//                                value.replace(
//                                    "content/",
//                                    "archive:"
//                                )
//                            }.filterValues { value -> value.contains("archive:") }.map { (addr, str) ->
//                                val startIndex = str.indexOf("archive:")
//                                return@map (addr + startIndex) to str.substring(startIndex)
//                            }.toMap(),
//                            sfxFormatLocation = strings.entries.let { entries ->
//                                entries.firstOrNull { (_, value) -> value == "wav" }
//                                    ?.let { (addr, str) -> Pair(addr, str) }
//                                    ?: entries.firstOrNull { (_, value) -> value == "ogg" }?.let { (addr, value) ->
//                                        addr to value.replace(
//                                            "ogg",
//                                            "wav"
//                                        )
//                                    }
//                            }
//                        )
//
//                        jsonMapper.writeValue(executableMapFile, executableMap)
//                    } else {
//                        executableMap = jsonMapper.readValue(executableMapFile)
//                    }
//
//                    val size = executableMap.archiveLocations.size + 1L
//                    val patchWarnings: MutableList<String> = ArrayList()
//                    ProgressTracker(
//                        downloadingText = "commands.pilot.flatpatch.patch_executable.patching",
//                        downloadedText = "commands.pilot.flatpatch.patch_executable.finished_patching"
//                    ) {
//                        executableMap.archiveLocations.entries.forEachIndexed { index, (addr, original) ->
//                            raf.seek(addr)
//                            val current = raf.readZeroString(original.length)
//                            if (current == original) {
//                                raf.seek(addr)
//                                raf.write(current.replace("archive:", "content/").toByteArray(Charsets.US_ASCII))
//                            } else {
//                                patchWarnings.add(
//                                    localise(
//                                        "commands.pilot.flatpatch.patch_executable.warn_differing_content",
//                                        "0x${addr.toString(16)}",
//                                        original,
//                                        current
//                                    )
//                                )
//                            }
//
//                            trackDownload(index.toLong(), size)
//                        }
//
//                        executableMap.sfxFormatLocation?.let { (addr, original) ->
//                            raf.seek(addr)
//                            val current = raf.readZeroString(original.length)
//                            if (current == original) {
//                                raf.seek(addr)
//                                raf.write(current.replace("wav", "ogg").toByteArray(Charsets.US_ASCII))
//                            } else {
//                                patchWarnings.add(
//                                    localise(
//                                        "commands.pilot.flatpatch.patch_executable.warn_differing_content",
//                                        "0x${addr.toString(16)}",
//                                        original,
//                                        current
//                                    )
//                                )
//                            }
//                        } ?: warn(
//                            "commands.pilot.flatpatch.patch_executable.warn_no_sfx"
//                        )
//
//                        trackDownload(size, size)
//                    }
//
//                    patchWarnings.forEach { warn(it) }
//                }
//            }
//        }
//
//        return@ParboiledCommand SUCCESS
//    }
//
//    val linkContent = ParboiledCommand(linkContentRule) { stack ->
//        val builder = stack[0] as LinkContentArgs
//
//        //Step 1. Check all our data's there
//
//        builder.workspacePath = builder.workspacePath?.normaliseForContentFile()
//        if (builder.workspacePath == null || builder.game == null || (builder.linkingContentPath == null && builder.linkingContentName == null) || builder.builder) {
//            if (builder.workspacePath == null) {
//                printLocale("commands.pilot.flatpatch.link_content.builder.workspace")
//                builder.workspacePath = builders.filePath()?.normaliseForContentFile()
//            }
//
//            if (builder.game == null) {
//                builder.workspacePath?.takeIf(File::isDirectory)?.let { path ->
//                    builder.game = when {
//                        path.name.matches(DR1_STEAM_FOLDER_REGEX) -> DR1
//                        path.name.matches(DR2_STEAM_FOLDER_REGEX) -> DR2
//                        path.name.matches(DRV3_STEAM_FOLDER_REGEX) -> V3
//                        else -> null
//                    }
//                }
//            }
//
//            if (builder.game == null) {
//                printLocale("commands.pilot.flatpatch.link_content.builder.game")
//                builder.game = builders.parameter()?.let { gameStr ->
//                    if (DR1.names.any { str -> str.equals(gameStr, true) })
//                        return@let DR1
//                    if (DR2.names.any { str -> str.equals(gameStr, true) })
//                        return@let DR2
//                    if (V3.names.any { str -> str.equals(gameStr, true) })
//                        return@let V3
//                    return@ParboiledCommand fail(
//                        "commands.pilot.flatpatch.link_content.builder.err_no_game_for_name",
//                        gameStr
//                    )
//                } ?: return@ParboiledCommand fail(
//                    "commands.pilot.flatpatch.link_content.builder.err_no_game_for_name",
//                    ""
//                )
//            }
//
//            if (builder.linkingContentPath == null && builder.linkingContentName == null) {
//                printLocale("commands.pilot.flatpatch.link_content.builder.link_content_name")
//                builder.linkingContentName = builders.parameter()?.takeIf(String::isNotBlank)
//
//                if (builder.linkingContentName == null) {
//                    printLocale("commands.pilot.flatpatch.link_content.builder.link_content_path")
//                    builder.linkingContentPath = builders.filePath()
//                }
//            }
//
//            if (builder.filter == null) {
//                printLocale("commands.pilot.flatpatch.link_content.builder.filter")
//                builder.filter = builders.filter()
//            }
//        }
//
//        if (builder.linkingContentPath == null && builder.linkingContentName != null) {
//            builder.linkingContentPath = builder.workspacePath?.takeIf(File::isDirectory)
//                ?.let { workspacePath -> File(workspacePath, builder.linkingContentName) }
//        }
//
//        builder.workspacePath?.takeIf(File::isDirectory)?.let { workspacePath ->
//            builder.game?.let { game ->
//                val regex = when (game) {
//                    DR1 -> DR1_CONTENT_FOLDER_REGEX
//                    DR2 -> DR2_CONTENT_FOLDER_REGEX
//                    UDG -> UDG_CONTENT_FOLDER_REGEX
//                    V3 -> DRV3_CONTENT_FOLDER_REGEX
//                    else -> TODO("Make regex for $game content folder")
//                }
//
//                builder.workspacePath = workspacePath.listFiles()
//                    .firstOrNull { file -> file.name.matches(regex) }
//                    ?: return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_no_content_child")
//
//                builder.baseGamePath = File(workspacePath, "base_game")
//            }
//        }
//
//        val args = builder.makeImmutable(defaultFilter = ".*".toRegex())
//
//        if (args.workspacePath == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_no_workspace")
//
//        if (!args.workspacePath.exists())
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_workspace_doesnt_exist")
//
//        if (!args.workspacePath.isDirectory)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_workspace_not_directory")
//
//        if (args.game == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_no_game")
//
//        if (args.linkingContentPath == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_no_link")
//
//        if (!args.linkingContentPath.exists())
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_link_doesnt_exist")
//
//        if (!args.linkingContentPath.isDirectory)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_link_not_directory")
//
//        if (!args.createLink && args.baseGamePath == null)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_no_base")
//
//        if (!args.createLink && !args.baseGamePath!!.exists())
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_base_doesnt_exist")
//
//        if (!args.createLink && !args.baseGamePath!!.isDirectory)
//            return@ParboiledCommand fail("commands.pilot.flatpatch.link_content.err_base_not_directory")
//
//        val filesToLink = args.linkingContentPath.walk().filter { file ->
//            file.isFile && !(file.name.startsWith(".") || file.name.startsWith("_") || file.parentFile.name.startsWith(".") || file.parentFile.name.startsWith(
//                "_"
//            ))
//        }
//            .map { file -> file relativePathFrom args.linkingContentPath }
//            .filter(args.filter!!::matches)
//
//        arbitraryProgressBar(loadingText = "commands.pilot.flatpatch.link_content.in_progress", loadedText = "") {
//            if (args.createLink) {
//                filesToLink.forEach { str ->
//                    val source = File(args.linkingContentPath, str)
//                    val dest = File(args.workspacePath, str)
//
//                    if (dest.exists())
//                        dest.delete()
//
//                    if (!source.exists())
//                        warn("commands.pilot.flatpatch.link_content.warn_missing_source", source)
//
//                    Files.createLink(dest.toPath(), source.toPath())
//                }
//            } else {
//                filesToLink.forEach { str ->
//                    val source = File(args.baseGamePath, str)
//                    val dest = File(args.workspacePath, str)
//
//                    if (dest.exists())
//                        dest.delete()
//
//                    if (!source.exists())
//                        warn("commands.pilot.flatpatch.link_content.warn_missing_source", source)
//                    else {
//                        Files.createLink(dest.toPath(), source.toPath())
//                    }
//                }
//            }
//        }
//
//        printlnLocale("commands.pilot.flatpatch.link_content.complete")
//
//        return@ParboiledCommand SUCCESS
//    }
}