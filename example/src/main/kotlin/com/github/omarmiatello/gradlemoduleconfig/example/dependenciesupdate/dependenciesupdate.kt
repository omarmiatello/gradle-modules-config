@file:Suppress("ComplexMethod", "TopLevelPropertyNaming", "WildcardImport", "MaxLineLength")

package com.github.omarmiatello.gradlemoduleconfig.example.dependenciesupdate

import com.github.omarmiatello.gradlemoduleconfig.dependenciesupdate.allDependencies
import com.github.omarmiatello.gradlemoduleconfig.dependenciesupdate.toMap
import com.github.omarmiatello.gradlemoduleconfig.dependenciesupdate.toSimpleDependencies
import java.io.File

fun main() {
    val reports = File("example/reports").listFiles()!!
        .map { it.toSimpleDependencies() }
        .allDependencies()
        .toMap()

    reports.forEach {
        println("${it.key} -> ${it.value}")
    }
}
