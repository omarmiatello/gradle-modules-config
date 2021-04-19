package com.github.omarmiatello.gradlemoduleconfig.script

import com.github.omarmiatello.gradlemoduleconfig.dataclass.ScriptConfig
import java.io.File

public class ProjectFiles(
    public val mainDir: File,
    config: ScriptConfig,
) {
    public val templatesDir: File = mainDir.resolve(config.gradleConfig.templatesDirPath)

    public val settingsGradleFile: File = mainDir.resolve(config.gradleConfig.settingsGradleFilename)

    public val dependenciesFile: File = mainDir.resolve(config.gradleConfig.dependenciesFilename)

    public fun getTemplateDir(templateName: String): File = templatesDir.resolve(templateName)
        .also { if (!it.exists() || !it.isDirectory) error("'$templateName' template does not exist!") }

    public companion object {
        public fun find(config: ScriptConfig): ProjectFiles {
            val templatesDirPath = config.gradleConfig.templatesDirPath
            var currentDir: File? = File("").absoluteFile
            while (currentDir != null) {
                val templatesDir = currentDir.resolve(templatesDirPath)
                if (templatesDir.exists()) {
                    return ProjectFiles(currentDir, config).also {
                        check(it.settingsGradleFile.exists()) {
                            "settingsGradleFile: file does not exist ${it.settingsGradleFile.absoluteFile}"
                        }
                        check(it.dependenciesFile.exists()) {
                            "dependenciesFile: file does not exist ${it.dependenciesFile.absoluteFile}"
                        }
                    }
                }
                currentDir = currentDir.parentFile
            }
            error("Cannot find ProjectDirs using '$templatesDirPath' in ${File("").absoluteFile}")
        }
    }
}
