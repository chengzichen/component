/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.conf

import com.intellij.facet.FacetManager
import com.intellij.openapi.module.Module
import com.dhc.plugin.util.isGradleModule

object Gradle : BuildSystemType()
object AndroidGradle : BuildSystemType()

class GradleDetector : BuildSystemTypeDetector {
    override fun detectBuildSystemType(module: Module): BuildSystemType? {
        if (module.isGradleModule()) {
            if (FacetManager.getInstance(module).allFacets.any { it.name == "Android" }) {
                return AndroidGradle
            }
            return Gradle
        }
        return null
    }
}
