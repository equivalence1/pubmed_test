package ru.spbau.mit.server

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths

@RestController
class CsvController(@Autowired @Qualifier("csvPath") private val csvPath: String) {

    @GetMapping("/db.csv")
    fun serve(): String {
        val encoded = Files.readAllBytes(Paths.get(csvPath))
        return String(encoded)
    }

}
