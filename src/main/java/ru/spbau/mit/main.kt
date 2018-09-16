package ru.spbau.mit

import ru.spbau.mit.pubmed.PubmedCsvConstructor

fun main(args: Array<String>) {
    val cvsPath = PubmedCsvConstructor.constructDailyCsv(false, 1)
    System.out.println("Constructed cvs file %s".format(cvsPath))
}

