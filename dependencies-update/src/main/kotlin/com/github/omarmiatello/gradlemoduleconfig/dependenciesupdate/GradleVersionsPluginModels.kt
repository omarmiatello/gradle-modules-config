package com.github.omarmiatello.gradlemoduleconfig.dependenciesupdate

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = false }

@Serializable
public data class DependenciesUpdate(
    val current: GroupInfo,
    val gradle: GradleInfo,
    val exceeded: GroupInfo,
    val outdated: GroupInfo,
    val unresolved: GroupInfo,
    val count: Long,
) {
    public fun toJson(): String = json.encodeToString(serializer(), this)

    public companion object {
        public fun fromJson(string: String): DependenciesUpdate = json.decodeFromString(serializer(), string)
    }
}

@Serializable
public data class GroupInfo(
    val dependencies: List<DependencyInfo>,
    val count: Long,
)

@Serializable
public data class DependencyInfo(
    val group: String,
    val name: String,
    val version: String,
    val userReason: String? = null,
    val projectUrl: String? = null,
    val available: AvailableInfo? = null,
)

@Serializable
public data class AvailableInfo(
    val milestone: String,
    val release: String? = null,
    val integration: String? = null,
)

@Serializable
public data class GradleInfo(
    val current: GradleVersionInfo,
    val nightly: GradleVersionInfo,
    val enabled: Boolean,
    val releaseCandidate: GradleVersionInfo,
    val running: GradleVersionInfo,
)

@Serializable
public data class GradleVersionInfo(
    val version: String,
    val reason: String,
    val isUpdateAvailable: Boolean,
    val isFailure: Boolean,
)
