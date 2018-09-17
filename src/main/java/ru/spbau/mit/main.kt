package ru.spbau.mit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import ru.spbau.mit.pubmed.PubmedCsvConstructor

@SpringBootApplication
class Application {

    @Bean(name = ["csvPath"])
    fun csvPath(): String = PubmedCsvConstructor.constructDailyCsv(false, 10)

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
