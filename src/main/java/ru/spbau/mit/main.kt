package ru.spbau.mit

import ru.spbau.mit.pubmed.PubmedCsvConstructor

fun main(args: Array<String>) {
    val cvsPath = PubmedCsvConstructor.constructDailyCsv(false, 2)
    System.out.println("Constructed cvs file %s".format(cvsPath))
}

