package ru.spbau.mit.pubmed

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream

/**
 * gunzips a single file.
 *
 * @param fPath
 *        Path pointing to .gz file
 * @return
 *        path to gunzipped file
 */
fun gunzipFile(fPath: String): String {
    val file = File(fPath)
    val outFile = File(fPath.removeSuffix(".gz"))

    // already guzipped, skip
    if (outFile.exists()) {
        return outFile.canonicalPath
    }

    val gzis = GZIPInputStream(FileInputStream(file))
    gzis.use {
        val out = FileOutputStream(outFile)
        out.use {
            var len: Int
            val buffer = ByteArray(1024)
            while (true) {
                len = gzis.read(buffer)
                if (len <= 0) {
                    break
                }
                out.write(buffer, 0, len)
            }
        }
    }

    return outFile.canonicalPath
}

fun ensureFileExists(file: File) {
    if (file.exists()) {
        return
    }
    val res = file.createNewFile()
    if (!res) {
        throw RuntimeException("failed to create file %s".format(file.canonicalPath))
    }
}

fun ensureDirExists(dir: File) {
    if (dir.exists() && dir.isDirectory) {
        return
    }
    val res = dir.mkdir()
    if (!res) {
        throw RuntimeException("failed to create dir %s".format(dir.canonicalPath))
    }
}
