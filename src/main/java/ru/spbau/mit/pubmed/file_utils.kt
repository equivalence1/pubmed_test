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

///**
// * Converts a single .xml file into .cvs file. If operation fails,
// * it just silently logs an error and returns false.
// *
// * @param file
// *        .xml file to convert into .csv file
// * @return
// *        true -- if file was successfully converted from .xml to .cvs format
// *        false -- otherwise
// */
//fun xml2csvFile(file: File): Boolean {
//    val factory = DocumentBuilderFactory.newInstance()
//    val builder = factory.newDocumentBuilder()
//    val document = builder.parse(xmlSource)
//
//    val stylesource = StreamSource(stylesheet)
//    val transformer = TransformerFactory.newInstance()
//            .newTransformer(stylesource)
//    val source = DOMSource(document)
//    val outputTarget = StreamResult(File("/tmp/x.csv"))
//    transformer.transform(source, outputTarget)
//}

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
