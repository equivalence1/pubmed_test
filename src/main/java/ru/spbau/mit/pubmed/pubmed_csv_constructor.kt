package ru.spbau.mit.pubmed

import org.apache.logging.log4j.LogManager
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

object PubmedCsvConstructor {

    private const val CSV_PATH = "/tmp/pubmed/db.csv"
    private val csvFile = File(CSV_PATH)
    private val log = LogManager.getLogger(PubmedCsvConstructor.javaClass.name)

    fun constructDailyCsv(forceRecreate: Boolean, xmlsMaxCnt: Int = 10): String {
        if (forceRecreate) {
            recreateDir()
        }

        if (csvAlreadyExists()) {
            log.info("%s file already exists. Aborting.".format(CSV_PATH))
            return CSV_PATH
        }

        createBasicCsv()

        PubmedRetriever.retrieveDailyXMLs(xmlsMaxCnt)
                .map { fPath ->
                    try {
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
                .forEach { fPath: String -> appendCsv(fPath) }

        log.info("Successfully created file %s".format(CSV_PATH))

        return CSV_PATH
    }

    private fun csvAlreadyExists() = csvFile.exists() && csvFile.isFile

    private fun recreateDir() {
        val dir = csvFile.parentFile
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    private fun createBasicCsv() {
        ensureDirExists(csvFile.parentFile)
        ensureFileExists(csvFile)

        // Task description does not specify the exact format of CSV file to generate.
        // Here I use only some of many pubmed citations fields, since we don't need most of them
        // for this particular task.
        csvFile.bufferedWriter().use { out ->
            out.write("year,title,abstract\n")
        }
    }

    private fun appendCsv(fPath: String) {
        log.info("Appending file %s to csv %s".format(fPath, CSV_PATH))

        try {
            val xmlFile = File(fPath)
            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
            val articleHandler = ArticleHandler()
            saxParser.parse(xmlFile, articleHandler)
        } catch (e: Exception) {
            log.error("Failed to append file %s to csv database file %s".format(fPath, CSV_PATH), e)
        }
    }

    /*
     * Simple parsing by converting xml to DOM won't work with such huge files.
     * On my laptop I can't even parse 20MB xml in 15 minutes. So parsing xmls with
     * SAX handlers, which is much faster. With this approach it takes only a few seconds
     * on my laptop.
     */

    private class ArticleHandler: DefaultHandler() {
        private var pubYear = ""
        private var title = ""
        private var abstract = ""

        enum class Element {
            NONE,
            DATE_COMPLETED,
            DATE_COMPLETED_YEAR,
            TITLE,
            ABSTRACT,
            CLOSE_ARTICLE
        }

        private var state: Element = Element.NONE

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            when (qName) {
                "AbstractText" -> state = Element.ABSTRACT
                "ArticleTitle" -> state = Element.TITLE
                "DateCompleted" -> state = Element.DATE_COMPLETED
                "Year" -> {
                    if (state == Element.DATE_COMPLETED) {
                        state = Element.DATE_COMPLETED_YEAR
                    }
                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            when (qName) {
                "Year" -> {
                    if (state == Element.DATE_COMPLETED_YEAR) {
                        state = Element.DATE_COMPLETED
                    }
                }
                "PubmedArticle" -> {
                    state = Element.CLOSE_ARTICLE
                }
                else -> state = Element.NONE
            }

        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            when (state) {
                Element.CLOSE_ARTICLE -> {
                    val csvStr = "%s,\"%s\",\"%s\"".format(pubYear, title, abstract)
                    csvFile.appendText("%s\n".format(csvStr))
                    pubYear = ""
                    title = ""
                    abstract = ""
                }
                Element.TITLE -> {
                    ch?.let { it ->
                        title = String(it, start, length)
                        val re = Regex("[\\t\\n\\r\"]")
                        title = re.replace(title, "")
                    }
                }
                Element.ABSTRACT -> {
                    ch?.let { it ->
                        abstract = String(it, start, length)
                        val re = Regex("[\\t\\n\\r\"]")
                        abstract = re.replace(abstract, "")
                    }
                }
                Element.DATE_COMPLETED_YEAR -> {
                    ch?.let { it -> pubYear = String(it, start, length) }
                }
                else -> {}
            }
        }

    }
}
