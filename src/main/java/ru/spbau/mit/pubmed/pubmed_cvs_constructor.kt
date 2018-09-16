package ru.spbau.mit.pubmed

import org.apache.logging.log4j.LogManager
import java.io.File

object PubmedCvsConstructor {

    private const val CVS_PATH = "/tmp/pubmed/db.cvs"
    private val cvsFile = File(CVS_PATH)
    private val log = LogManager.getLogger(PubmedCvsConstructor.javaClass.name)

    fun constructDailyCvs(forceRecreate: Boolean, xmlsMaxCnt: Int = 10): String {
        if (forceRecreate) {
            recreateDir()
        }

        if (cvsAlreadyExists()) {
            return CVS_PATH
        }

        createBasicCvs()

        PubmedRetriever.retrieveDailyXMLs(xmlsMaxCnt)
                .map { fPath ->
                    return try {
                        log.info("Gunzipping file %s".format(fPath))
                        val res = gunzipFile(fPath)
                        log.info("Successfully gunzipped file %s".format(res))
                        res
                    } catch (e: Exception) {
                        log.error("Failed to gunzip file %s".format(fPath), e)
                        ""
                    }
                }
                .filter { fPath: String -> fPath != "" }
                .forEach { fPath: String -> appendCvs(fPath) }

        return CVS_PATH
    }

    private fun cvsAlreadyExists() = cvsFile.exists() && cvsFile.isFile

    private fun recreateDir() {
        val dir = cvsFile.parentFile
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    private fun createBasicCvs() {
        ensureDirExists(cvsFile.parentFile)
        ensureFileExists(cvsFile)
        // TODO setup desc fields
    }

    private fun appendCvs(fPath: String) {
        log.info("Appending file %s to cvs %s".format(fPath, CVS_PATH))
    }

}
