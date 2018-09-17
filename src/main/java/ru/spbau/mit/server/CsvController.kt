package ru.spbau.mit.server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths

@RestController
class CsvController {

    @GetMapping("/db.csv")
    fun serve(): String {
        val classLoader = javaClass.classLoader
        val encoded = Files.readAllBytes(Paths.get(classLoader.getResource("population.csv").path))
        return String(encoded)
    }

}
