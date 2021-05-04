package com.github.omarmiatello.gradlemoduleconfig.dependenciesupdate

public data class SimpleDependencies(
    val current: List<Dependency>,
    val exceeded: List<Dependency>,
    val outdated: List<Dependency>,
    val unresolved: List<Dependency>,
) {
    public fun allDependencies(): List<Dependency> = (current + exceeded + outdated + unresolved).distinct()
}

public data class Dependency(
    val group: String,
    val name: String,
    val version: String,
    val projectUrl: String? = null,
) {
    val dependencyGradlePrefix: String = "$group:$name"
    val dependencyGradleFull: String = "$group:$name:$version"
}
