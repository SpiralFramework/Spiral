package org.abimon.spiral.modding

import org.abimon.spiral.core.archives.IArchive
import org.abimon.visi.io.DataSource
import org.abimon.visi.security.sha512Hash
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList

object BackupManager {
    fun backupOverridingEntries(archive: IArchive, newEntries: List<Pair<String, DataSource>>) {
        //FIRST OF ALL
        //We need to backup any and all files that **do not match**
        //So we perform a batch query for all the data presently *in the archive*

        val fingerprintToName = archive.fileEntries.filter { (name) -> newEntries.any { (newName) -> name == newName } }.map { (filename, ds) -> ds().use { stream -> stream.sha512Hash() } to filename }.toMap()
        val fingerprintsForWADFiles = ModManager.getModsForFingerprints(fingerprintToName.keys.toTypedArray())

        //Next up, we need to match the fingerprint values to the file with the name.
        val fingerprintMap = fingerprintToName.map { (fingerprint, name) -> name to fingerprintsForWADFiles[fingerprint]!!.filter { fingerprintObj -> fingerprintObj.filename == name } }.toMap()

        //Next, we need to backup the files themselves.
        val backupFile = File(archive.archiveFile.absolutePath.replaceAfterLast('.', "zip"))
        val tmp = File.createTempFile("backup-${UUID.randomUUID()}", ".zip")
        val alreadyAdded: MutableList<String> = ArrayList()
        FileOutputStream(tmp).use { fos ->
            val zipOut = ZipOutputStream(fos)

            if(backupFile.exists()) {
                val zipBackup = ZipFile(backupFile)
                for (entry in zipBackup.entries()) {
                    alreadyAdded.add(entry.name)
                    zipOut.putNextEntry(entry)
                    zipBackup.getInputStream(entry).use { stream -> stream.copyTo(zipOut) }
                }
            }

            fingerprintMap.forEach { name, fingerprints ->
                if (fingerprints.none { fingerprint -> fingerprint.mod_uid in ModManager.OFFICIAL_DR_MODS })
                    return@forEach

                val (_, fileData) = (archive.fileEntries.firstOrNull { (fileName) -> fileName == name } ?: return@forEach)
                if(name in alreadyAdded)
                    return@forEach

                zipOut.putNextEntry(ZipEntry(name))
                alreadyAdded.add(name)
                fileData().use { stream -> stream.copyTo(zipOut) }
            }

            zipOut.close()
        }

        backupFile.delete()
        tmp.renameTo(backupFile)
    }
}