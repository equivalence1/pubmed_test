package ru.spbau.mit

import ru.spbau.mit.pubmed.PubmedCvsConstructor

fun main(args: Array<String>) {
    val cvsPath = PubmedCvsConstructor.constructDailyCvs(false, 1)
    System.out.println("Constructed cvs file %s".format(cvsPath))
}

