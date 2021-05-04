package com.github.omarmiatello.gradlemoduleconfig.dependenciesupdate

import java.io.File

// DependenciesUpdate converters

public fun String.toDependenciesUpdate(): DependenciesUpdate = DependenciesUpdate.fromJson(this)

public fun File.toDependenciesUpdate(): DependenciesUpdate = readText().toDependenciesUpdate()

public fun DependenciesUpdate.toSimpleDependencies(): SimpleDependencies = SimpleDependencies(
    current = current.dependencies.map { it.toDependency() },
    exceeded = exceeded.dependencies.map { it.toDependency() },
    outdated = outdated.dependencies.map { it.toDependency() },
    unresolved = unresolved.dependencies.map { it.toDependency() },
)

public fun DependencyInfo.toDependency(): Dependency = Dependency(
    group = group,
    name = name,
    version = available?.milestone ?: version,
    projectUrl = projectUrl,
)

// SimpleDependencies converters

public fun File.toSimpleDependencies(): SimpleDependencies = toDependenciesUpdate().toSimpleDependencies()

public fun List<SimpleDependencies>.allDependencies(): List<Dependency> = flatMap { it.allDependencies() }.distinct()

public fun List<Dependency>.toMap(): Map<String, String> =
    map { it.dependencyGradlePrefix to it.version }
        .sortedBy { it.first }
        .toMap()
