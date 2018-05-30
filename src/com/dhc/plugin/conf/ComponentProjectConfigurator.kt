/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dhc.plugin.conf

import com.dhc.plugin.platform.TargetPlatform
import com.intellij.internal.statistic.libraryJar.LibraryJarDescriptor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ExternalLibraryDescriptor
import com.intellij.psi.PsiElement

enum class ConfigureKotlinStatus {
    CONFIGURED,
    NON_APPLICABLE,
    CAN_BE_CONFIGURED,
    BROKEN
}

interface ComponentProjectConfigurator {

    fun getStatus(moduleSourceRootGroup: ModuleSourceRootGroup): ConfigureKotlinStatus

    @JvmSuppressWildcards fun configure(project: Project, excludeModules: Collection<Module>)

    val presentableText: String

    val name: String

    val targetPlatform: TargetPlatform

    fun changeCoroutineConfiguration(module: Module, state:State)

    companion object {
        val EP_NAME = ExtensionPointName.create<ComponentProjectConfigurator>("com.dhc.plugin.conf.projectConfigurator")
    }

}
public enum class State(val description: String)  {
    ENABLED("Enabled"),
    ENABLED_WITH_WARNING("Enabled with warning"),
    ENABLED_WITH_ERROR("Disabled"), // TODO: consider dropping this and using DISABLED instead
    DISABLED("Disabled");
}