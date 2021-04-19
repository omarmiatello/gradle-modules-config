package com.github.omarmiatello.gradlemoduleconfig.script

import com.github.omarmiatello.gradlemoduleconfig.dataclass.App
import com.github.omarmiatello.gradlemoduleconfig.dataclass.ProjectModule
import com.github.omarmiatello.gradlemoduleconfig.dataclass.ScriptConfig
import com.github.omarmiatello.gradlemoduleconfig.dataclass.toProjectModuleList
import java.io.File

public fun ScriptConfig.buildModules(
    onReplaceMap: (ProjectModule) -> Map<String, String>,
    onCreateModuleDestination: (ProjectModule, ProjectFiles) -> Unit = { module, projectFiles ->
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

        module.createModuleDestination(
            sourceDir = templateDir,
            destinationDir = destinationDir,
            replaceMap = onReplaceMap(module)
        )
    },
    onGradleSettings: (ProjectModule, ProjectFiles) -> Unit = { module, projectFiles ->
        module.addGradleSettingsInclude(settingsGradleFile = projectFiles.settingsGradleFile)
    },
    onModuleInDependency: (ProjectModule, ProjectFiles) -> Unit = { module, projectFiles ->
        if (module.configModule !is App) {
            module.addModuleInDependency(dependenciesFile = projectFiles.dependenciesFile)
        }
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

    modules.toProjectModuleList(this)
        .forEach { module ->
            onCreateModuleDestination(module, projectFiles)

            onGradleSettings(module, projectFiles)

            onModuleInDependency(module, projectFiles)
        }

    if (!writeOnDisk) {
        logI(importantMessage("*** writeOnDisk is false, this WAS A PREVIEW of the changes ***"))
    }
}

@Suppress("MagicNumber")
public fun ScriptConfig.promptPreviewWithConfirmation(): Boolean =
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
public fun ProjectModule.createModuleDestination(
    sourceDir: File,
    destinationDir: File,
    replaceMap: Map<String, String>,
) {
    fun String.replaceKeywords(onReplaceValue: (String) -> String = { it }) =
        replaceMap.toList().fold(this) { str, (key, value) -> str.replace(key, onReplaceValue(value)) }

    val writeOnDisk = config.writeOnDisk
    val keywordsLowerCase = replaceMap.keys.map { it.toLowerCase() }.distinct()

    sourceDir.walkTopDown()
        .onEnter { it.name != "build" }
        .forEach { fileOrigin: File ->
            val fileDestination = destinationDir.resolve(
                fileOrigin.relativeTo(sourceDir).path.replaceKeywords { it.replace('.', '/') }
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
                    if (writeOnDisk) fileDestination.writeText(text.replaceKeywords())
                } else {
                    config.logV("Copy file: $fileDestination")
                    if (writeOnDisk) fileOrigin.copyTo(fileDestination, overwrite = true)
                }
            }
        }
}

public fun ProjectModule.addGradleSettingsInclude(settingsGradleFile: File) {
    val includeText = "include(\"$gradlePath\")"
    val lines = settingsGradleFile.readLines()
    if (lines.firstOrNull { includeText in it } == null) {
        val fileText = lines.toMutableList().apply {
            val insertIndex = lines.indexOfLast { it.startsWith("include") && it < includeText }
            add(insertIndex + 1, includeText)
            config.logV("Update ${settingsGradleFile.toPath().fileName}, added: $includeText")
        }.joinToString("\n")
        if (config.writeOnDisk) settingsGradleFile.writeText(fileText)
    }
}

public fun ProjectModule.addModuleInDependency(dependenciesFile: File) {
    val dependenciesPrefix = config.gradleConfig.dependenciesPrefix
    val moduleName = moduleNameParts.joinToString("") { it.capitalize() }
    val lines = dependenciesFile.readLines()
    val import = "import org.gradle.api.artifacts.dsl.DependencyHandler"
    val shouldAddImport = lines.firstOrNull { import in it } == null

    val dependency = "val DependencyHandler.$dependenciesPrefix$moduleName get() = project(\"$gradlePath\")"

    if (lines.firstOrNull { dependency in it } == null) {
        val fileText = lines.toMutableList().also { newLines ->
            if (shouldAddImport) newLines.add(0, import)

            val isDependencyLine: (String) -> Boolean = { "val DependencyHandler.$dependenciesPrefix" in it }
            val insertIndex = lines
                .indexOfLast { isDependencyLine(it) && it.substringAfter(":") < gradlePath.substring(1) }
                .takeIf { it != -1 }
                ?: (lines.indexOfFirst(isDependencyLine) - 1).takeIf { it >= 0 }
                ?: lines.lastIndex
            newLines.add(insertIndex + 1, dependency)
            config.logV("Update ${dependenciesFile.toPath().fileName}, added: $dependency")
        }.joinToString("\n")
        if (config.writeOnDisk) dependenciesFile.writeText(fileText)
    }
}
