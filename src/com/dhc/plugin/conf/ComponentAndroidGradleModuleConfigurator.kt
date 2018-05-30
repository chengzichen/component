/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.conf

import com.dhc.plugin.platform.JvmPlatform
import com.dhc.plugin.platform.TargetPlatform
import com.dhc.plugin.util.PathUtil.MVP_NAME
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiFile

class ComponentAndroidGradleModuleConfigurator internal constructor() : ComponentWithGradleConfigurator() {

    override val name: String = NAME

    override val targetPlatform: TargetPlatform = JvmPlatform

    override val presentableText: String = "MVP"

    public override fun isApplicable(module: Module): Boolean = module.getBuildSystemType() == AndroidGradle

    override val kotlinPluginName: String = COMPONENT_ANDROID

    override fun addElementsToFile(file: PsiFile, isTopLevelProjectFile: Boolean, version: String): Boolean {
        val manipulator = getManipulator(file)
        val sdk = ModuleUtil.findModuleForPsiElement(file)?.let { ModuleRootManager.getInstance(it).sdk }
        val jvmTarget = getJvmTarget(sdk, version)

        return if (isTopLevelProjectFile) {
            manipulator.configureProjectBuildScript(version)
        }
        else {
            manipulator.configureModuleBuildScript(
                    kotlinPluginName,
                    getStdlibArtifactName(sdk, version),
                    version,
                    jvmTarget
            )
        }
    }

    override fun getStdlibArtifactName(sdk: Sdk?, version: String): String {
        return MVP_NAME
    }

    companion object {
        private val NAME = "android-gradle"

        private val COMPONENT_ANDROID = "com.dhc.comgradle"
    }
}
