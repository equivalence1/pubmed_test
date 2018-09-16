package ru.spbau.mit.pubmed

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.logging.log4j.LogManager
import java.io.*

object PubmedRetriever {

    private const val HOST = "ftp.ncbi.nlm.nih.gov"
    private const val PORT = 21
    private const val USER = "anonymous"
    private const val PASS = "ftp@example.com"
    private const val DAILY_PATH = "/pubmed/updatefiles/"
    private const val FILES_SUFFIX = ".xml.gz"
    private const val destPath = "/tmp/pubmed" // might be made configurable in the future

    private val log = LogManager.getLogger(PubmedRetriever.javaClass.name)!!

    fun retrieveDailyXMLs(maxCnt: Int): List<String> {
        return retrieveXMLs(DAILY_PATH, maxCnt)
    }

    /**
     * retrieves all .xml.gz files from @host using ftp protocol.
     *
     * @param hostPath
     *        path to folder on host server from which to download xmls
     *
     * @param maxCnt
     *        maximum number of xmls to retrieve. Defaults to 10
     *
     * @return
     *        list of downloaded file paths
     */
    private fun retrieveXMLs(hostPath: String, maxCnt: Int = 10): List<String> {
        val resFiles: MutableList<String> = ArrayList()

        try {
            ensureDirExists(File(destPath))

            // destPath might already contain some xmls.
            // Don't download more than necessary.
            findAlreadyDownloaded(resFiles)

            if (resFiles.size >= maxCnt) {
                log.info("path %s already contains at least %d xmls. Aborting.".format(destPath, maxCnt))
                return resFiles
            }

            log.info("""retrieving maxCnt=%d files from %s%s to destPath=%s""".format(maxCnt, HOST, hostPath, destPath))

            val client = FTPClient()
            client.connect(HOST, PORT)
            client.login(USER, PASS)
            client.enterLocalPassiveMode()
            client.setFileType(FTP.BINARY_FILE_TYPE)
            client.changeWorkingDirectory(hostPath)

            val xmlFiles = client.listFiles()
                    .filter { file -> file.isFile && file.name.endsWith(FILES_SUFFIX) }

            for (xml in xmlFiles) {
                if (resFiles.size == maxCnt) {
                    break
                }

                log.info("trying to retrieve file %s".format(xml.name))

                val destFile = File(destPath, xml.name)
                val destOutStream = BufferedOutputStream(FileOutputStream(destFile))

                try {
                    destOutStream.use {
                        val res = client.retrieveFile(xml.name, destOutStream)
                        if (!res) {
                            if (destFile.exists()) {
                                destFile.delete()
                            }
                            // ok, failed to download some xml file. Log it, skip it, and try to download the next one
                            log.error("retrieveFile for file %s failed silently. Skipping.".format(xml.name))
                        } else {
                            resFiles.add(destFile.canonicalPath)
                        }
                    }
                } catch (e: IOException) {
                    // if file was created but not fully downloaded, remove it
                    if (destFile.exists()) {
                        destFile.delete()
                    }
                    // ok, failed to download some xml file. Log it, skip it, and try to download the next one
                    log.error("Error while attempting to load file %s. Skipping.".format(xml.name), e)
                }
            }
        } catch (e: Exception) {
            log.error("Failed to retrieve pubmed files.", e)
        }

        return resFiles
    }

    private fun findAlreadyDownloaded(resFiles: MutableList<String>) {
        val dir = File(destPath)
        return dir.list().filter{ fName -> fName.endsWith(FILES_SUFFIX) }.forEach {
            fName -> resFiles.add(File(destPath, fName).canonicalPath)
        }
    }

}
