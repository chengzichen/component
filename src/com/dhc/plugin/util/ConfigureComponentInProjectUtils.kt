/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.util

import com.dhc.plugin.conf.*
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.DependencyScope

data class RepositoryDescription(val id: String, val name: String, val url: String, val bintrayUrl: String?, val isSnapshot: Boolean)

val MAVEN_CENTRAL = "mavenCentral()"

val JCENTER = "jcenter()"

val KOTLIN_GROUP_ID = "com.dhc.component"


val EAP_REPOSITORY = RepositoryDescription(
        "",
        "",
        "https://jitpack.io",
        "",
        isSnapshot = false)
fun isRepositoryConfigured(repositoriesBlockText: String): Boolean =
        repositoriesBlockText.contains(MAVEN_CENTRAL) && repositoriesBlockText.contains(JCENTER)

fun DependencyScope.toGradleCompileScope(isAndroidModule: Boolean) = when (this) {
    DependencyScope.COMPILE -> "compile"
// TODO: We should add testCompile or androidTestCompile
    DependencyScope.TEST -> if (isAndroidModule) "compile" else "testCompile"
    DependencyScope.RUNTIME -> "runtime"
    DependencyScope.PROVIDED -> "compile"
    else -> "compile"
}

fun RepositoryDescription.toGroovyRepositorySnippet() = "maven {\n    url '$url'\n}"

fun RepositoryDescription.toKotlinRepositorySnippet() = "maven {\n    setUrl(\"$url\")\n}"


fun isModuleConfigured(moduleSourceRootGroup: ModuleSourceRootGroup): Boolean {
    return allConfigurators().any {
        it.getStatus(moduleSourceRootGroup) == ConfigureKotlinStatus.CONFIGURED
    }
}

fun getRepositoryForVersion(): RepositoryDescription? = EAP_REPOSITORY


fun getAbleToRunConfigurators(project: Project): Collection<ComponentProjectConfigurator> {
    val modules = getConfigurableModules(project)

    return allConfigurators().filter { configurator ->
        modules.any {
            configurator.getStatus(it) == ConfigureKotlinStatus.CAN_BE_CONFIGURED
        }
    }
}

fun getConfigurableModules(project: Project): List<ModuleSourceRootGroup> {
    return ModuleSourceRootMap(project).groupByBaseModules(project.allModules())
}
fun getCanBeConfiguredHostModules(project: Project, configurator: ComponentProjectConfigurator): List<Module> {
    return ModuleSourceRootMap(project).groupByBaseModules(project.allModules())
            .filter { configurator.canSetHostConfigure(it) }
            .map { it.baseModule }
}

fun getConfiguratorByName(name: String): ComponentProjectConfigurator? {
    return allConfigurators().firstOrNull { it.name == name }
}

fun allConfigurators() = Extensions.getExtensions(ComponentProjectConfigurator.EP_NAME)

fun getCanBeConfiguredModules(project: Project, configurator: ComponentProjectConfigurator): List<Module> {
    return ModuleSourceRootMap(project).groupByBaseModules(project.allModules())
            .filter { configurator.canConfigure(it) }
            .map { it.baseModule }
}

private fun ComponentProjectConfigurator.canConfigure(moduleSourceRootGroup: ModuleSourceRootGroup) =
        getStatus(moduleSourceRootGroup) == ConfigureKotlinStatus.CAN_BE_CONFIGURED &&
                (allConfigurators().toList() - this).none { it.getStatus(moduleSourceRootGroup) == ConfigureKotlinStatus.CONFIGURED }

private fun ComponentProjectConfigurator.canSetHostConfigure(moduleSourceRootGroup: ModuleSourceRootGroup) =
        getStatus(moduleSourceRootGroup) != ConfigureKotlinStatus.NON_APPLICABLE



fun isNotConfiguredNotificationRequired(moduleGroup: ModuleSourceRootGroup): Boolean {
    return !SuppressNotificationState.isKotlinNotConfiguredSuppressed(moduleGroup) && !isModuleConfigured(moduleGroup)
}



