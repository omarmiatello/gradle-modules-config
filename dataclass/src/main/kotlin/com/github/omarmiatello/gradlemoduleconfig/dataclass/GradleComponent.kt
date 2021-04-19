package com.github.omarmiatello.gradlemoduleconfig.dataclass

public fun String.toPackageName(): String =
    toLowerCase().replace("-", "")

public sealed class GradleComponent(
    public open val gradleName: String,
    public open val packageNamePart: String,
    public open val extra: Map<String, Any> = emptyMap(),
)

// GradleComponents

public data class Group(
    override val gradleName: String,
    val modules: List<GradleComponent>,
    override val packageNamePart: String = gradleName.toPackageName(),
    override val extra: Map<String, Any> = emptyMap(),
) : GradleComponent(gradleName, packageNamePart, extra)

public sealed class GradleModule(
    gradleName: String,
    packageNamePart: String,
    extra: Map<String, Any>,
    public open val templateName: String,
) : GradleComponent(gradleName, packageNamePart, extra)

// GradleModules

public data class App(
    override val gradleName: String,
    override val packageNamePart: String = gradleName.toPackageName(),
    override val extra: Map<String, Any> = emptyMap(),
    override val templateName: String = "app",
) : GradleModule(
    gradleName = gradleName,
    packageNamePart = packageNamePart,
    extra = extra,
    templateName = templateName,
)

public data class LibAndroid(
    override val gradleName: String,
    override val packageNamePart: String = gradleName.toPackageName(),
    override val extra: Map<String, Any> = emptyMap(),
    override val templateName: String = "lib-android",
) : GradleModule(
    gradleName = gradleName,
    packageNamePart = packageNamePart,
    extra = extra,
    templateName = templateName,
)

public data class LibKotlin(
    override val gradleName: String,
    override val packageNamePart: String = gradleName.toPackageName(),
    override val extra: Map<String, Any> = emptyMap(),
    override val templateName: String = "lib-kotlin",
) : GradleModule(
    gradleName = gradleName,
    packageNamePart = packageNamePart,
    extra = extra,
    templateName = templateName,
)

public fun String.toGradleComponent(
    leaf: (String) -> GradleModule,
): GradleComponent {
    val parts = split(":").filter { it.isNotEmpty() }
    var component: GradleComponent = leaf(parts.last())
    parts.dropLast(1).reversed().forEach {
        component = Group(it, listOf(component))
    }
    return component
}
