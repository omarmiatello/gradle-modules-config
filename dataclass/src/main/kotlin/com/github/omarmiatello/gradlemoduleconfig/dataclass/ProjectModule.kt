package com.github.omarmiatello.gradlemoduleconfig.dataclass

public data class ProjectModule(
    val configModule: GradleModule,
    val configGroups: List<Group>,
    val config: ScriptGradleModuleConfig,
) {
    public val templateName: String = configModule.templateName

    public val moduleNameParts: List<String> =
        configGroups.flatMap { it.gradleName.toModuleNameParts() } +
            configModule.gradleName.toModuleNameParts()

    public val gradlePathParts: List<String> =
        (configGroups.map { it.gradleName } + configModule.gradleName)
            .runningReduce { acc, s ->
                if (!config.forceNamingConvention || s.startsWith("$acc-")) s else "$acc-$s"
            }

    /**
     * Full gradle path, e.g., :feature:navigation:core
     */
    public val gradlePath: String = gradlePathParts.joinToString(":", prefix = ":")

    /**
     * Full package name, e.g., com.github.owner.projectname.myfeature
     */
    public val packageName: String = buildString {
        val prefix = listOf(config.gradleConfig.packagePrefix)
        val parts = (configGroups.map { it.packageNamePart } + configModule.packageNamePart).filterNotNull()
            .filter { it.isNotBlank() }
        append((prefix + parts).joinToString("."))
    }

    public val destinationDirPart: String = gradlePathParts.joinToString("/")

    private fun String.toModuleNameParts(): List<String> = toLowerCase().split("[^a-z0-9]".toRegex())
}

public fun List<GradleComponent>.toProjectModuleList(config: ScriptGradleModuleConfig): List<ProjectModule> =
    flatMap { it.toProjectModuleList(config) }

public fun GradleComponent.toProjectModuleList(config: ScriptGradleModuleConfig): List<ProjectModule> =
    when (this) {
        is Group -> modules.flatMap {
            it.toProjectModuleList(config)
                .map { (module, packages) ->
                    ProjectModule(module, listOf(this) + packages, config)
                }
        }
        is GradleModule -> listOf(ProjectModule(this, emptyList(), config))
    }
