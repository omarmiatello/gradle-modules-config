package com.github.omarmiatello.gradlemoduleconfig.script

import com.github.omarmiatello.gradlemoduleconfig.dataclass.App
import com.github.omarmiatello.gradlemoduleconfig.dataclass.ProjectModule
import com.github.omarmiatello.gradlemoduleconfig.dataclass.ScriptGradleModuleConfig
import com.github.omarmiatello.gradlemoduleconfig.dataclass.toProjectModuleList
import java.io.File

public fun ScriptGradleModuleConfig.buildModules(
    onContentReplace: (ProjectModule) -> Map<String, String>,
    onDirReplace: (ProjectModule) -> Map<String, String>,
    onModuleDestination: (ProjectModule, ProjectFiles) -> Unit = { module, projectFiles ->
        val templateName = module.templateName
        val templateDir = projectFiles.getTemplateDir(templateName)
        val destinationDir = File("${projectFiles.mainDir.absolutePath}/${module.destinationDirPart}")

        val moduleFullName = module.moduleNameParts.joinToString(" ") { it.capitalize() }

        logD(
            """|
               |Create module [$templateName] $moduleFullName 
               | - destinationDir: ${destinationDir.absolutePath}
               | - packageName:    ${module.packageName}
               | - gradlePath:     ${module.gradlePath}""".trimMargin()
        )

        copyFromTemplate(
            source = templateDir,
            destination = destinationDir,
            contentReplace = onContentReplace(module),
            dirReplace = onDirReplace(module),
            config = this,
        )
    },
    onGradleDependencies: (ProjectModule, ProjectFiles) -> Unit = { module, projectFiles ->
        if (module.configModule !is App) {
            appendSorted(
                destinationFile = projectFiles.dependenciesFile,
                newString = "import org.gradle.api.artifacts.dsl.DependencyHandler",
                appendAfter = "import ",
                appendFallback = { 0 },
                config = module.config,
            )
            val prefix = module.config.gradleConfig.dependenciesPrefix
            val moduleName = module.moduleNameParts.joinToString("") { it.capitalize() }
            appendSorted(
                destinationFile = projectFiles.dependenciesFile,
                newString = "val DependencyHandler.$prefix$moduleName get() = project(\"${module.gradlePath}\")",
                appendAfter = "val DependencyHandler.$prefix",
                appendFallback = { it.lastIndex + 1 },
                config = module.config,
            )
        }
    },
    onGradleSettings: (ProjectModule, ProjectFiles) -> Unit = { module, projectFiles ->
        appendSorted(
            destinationFile = projectFiles.settingsGradleFile,
            newString = "include(\"${module.gradlePath}\")",
            appendAfter = "include(\"",
            config = this,
        )
    },
) {
    val projectFiles: ProjectFiles = ProjectFiles.find(this)

    if (hasPrompt && !promptPreviewWithConfirmation()) {
        println("Command canceled!")
        return
    }

    if (!writeOnDisk) {
        logD(importantMessage("*** writeOnDisk is false, this IS A PREVIEW of the changes ***"))
    }

    modules.toProjectModuleList(this).forEach { module ->
        onModuleDestination(module, projectFiles)
        onGradleDependencies(module, projectFiles)
        onGradleSettings(module, projectFiles)
    }

    if (!writeOnDisk) {
        logI(importantMessage("*** writeOnDisk is false, this WAS A PREVIEW of the changes ***"))
    }
}

@Suppress("MagicNumber")
public fun ScriptGradleModuleConfig.promptPreviewWithConfirmation(): Boolean =
    askUserBoolean(
        buildString {
            val modules = modules.toProjectModuleList(this@promptPreviewWithConfirmation)
            appendLine("This script will create the following module${if (modules.size == 1) "" else "s"}:")
            appendLine(
                modules.joinToString("\n") {
                    " > ${"[${it.templateName}]  ".padEnd(20)}${it.gradlePath}"
                }
            )
            append("Do you want to continue?")
        }
    )

@Suppress("NestedBlockDepth")
public fun copyFromTemplate(
    source: File,
    destination: File,
    contentReplace: Map<String, String>,
    dirReplace: Map<String, String>,
    config: ScriptGradleModuleConfig,
) {
    fun Map<String, String>.replaceOn(source: String) = toList().fold(source) { str, (key, value) ->
        str.replace(key, value)
    }

    val writeOnDisk = config.writeOnDisk
    val keywordsLowerCase = contentReplace.keys.map { it.toLowerCase() }.distinct()

    source.walkTopDown()
        .onEnter { it.name != "build" } // avoid build folders
        .forEach { fileOrigin: File ->
            val fileDestination = destination.resolve(
                dirReplace.replaceOn(fileOrigin.relativeTo(source).path)
            )
            if (fileOrigin.isDirectory) {
                if (!fileDestination.exists()) {
                    config.logV("Create dir: $fileDestination")
                    if (writeOnDisk) {
                        fileDestination.mkdirs()
                    }
                }
            } else {
                val text = fileOrigin.readText()
                if (text.findAnyOf(keywordsLowerCase, ignoreCase = true) != null) {
                    config.logV("Create file: $fileDestination")
                    if (writeOnDisk) fileDestination.writeText(contentReplace.replaceOn(text))
                } else {
                    config.logV("Copy file: $fileDestination")
                    if (writeOnDisk) fileOrigin.copyTo(fileDestination, overwrite = true)
                }
            }
        }
}

public fun appendSorted(
    destinationFile: File,
    newString: String,
    appendAfter: String,
    appendFallback: (List<String>) -> Int = { it.lastIndex + 1 },
    config: ScriptGradleModuleConfig,
) {
    val lines = destinationFile.readLines()
    if (lines.firstOrNull { newString in it } == null) { // no duplicates
        val fileText = lines.toMutableList().apply {
            val insertIndex = (lines.indexOfLast { it.startsWith(appendAfter) && it < newString } + 1)
                .takeIf { it != 0 } ?: appendFallback(lines)
            add(insertIndex, newString)
            config.logV("Update ${destinationFile.toPath().fileName}, added: $newString")
        }.joinToString("\n")
        if (config.writeOnDisk) destinationFile.writeText(fileText)
    }
}
