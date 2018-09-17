package ru.spbau.mit.server

import com.opencsv.CSVReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Paths
import java.nio.file.Files



@RestController
class AppController(@Autowired @Qualifier("csvPath") private val csvPath: String) {

    private data class CountPair(val agingCnt: Int, val notchCnt: Int)

    private fun countSubstring(s: String, sub: String): Int = s.split(sub).size - 1

    @GetMapping("/")
    fun serve(): String {
//        val classLoader = javaClass.classLoader
//        val encoded = Files.readAllBytes(Paths.get(classLoader.getResource("index.html").path))
//        return String(encoded)

        val reader = Files.newBufferedReader(Paths.get(csvPath))
        val csvReader = CSVReader(reader)

        val counts = mutableMapOf<String, CountPair>()
        var nextRecord: Array<String>?

        csvReader.readNext()
        while (true) {
            nextRecord = csvReader.readNext()
            if (nextRecord == null) {
                break
            }
            if (nextRecord.size < 3) {
                continue
            }
            val curCnt = counts.getOrDefault(nextRecord[0], CountPair(0, 0))
            val recordAgingCnt = countSubstring(nextRecord[1].toLowerCase(), "aging")
            val recordNotchCnt = countSubstring(nextRecord[2].toLowerCase(), "notch")
            counts[nextRecord[0]] = CountPair(curCnt.agingCnt + recordAgingCnt, curCnt.notchCnt + recordNotchCnt)
        }

        val sb = StringBuilder()
        sb.append("year aging notch\n<br />\n")
        counts.entries.sortedBy { e -> e.key }.forEach { entry ->
            sb.append("<p>" + entry.key + " " + entry.value.agingCnt + " " + entry.value.notchCnt + "</p>\n")
        }

        return sb.toString()
    }

}
