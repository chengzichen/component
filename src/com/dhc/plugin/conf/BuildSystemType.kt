/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.conf

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.module.Module

abstract class BuildSystemType {
    object JPS : BuildSystemType()
}

interface BuildSystemTypeDetector {
    fun detectBuildSystemType(module: Module): BuildSystemType?

    companion object {
        val EP_NAME = ExtensionPointName.create<BuildSystemTypeDetector>("com.dhc.plugin.conf.buildSystemTypeDetector")
    }
}

fun Module.getBuildSystemType(): BuildSystemType {
    for (extension in Extensions.getExtensions(BuildSystemTypeDetector.EP_NAME)) {
        val result = extension.detectBuildSystemType(this)
        if (result != null) {
            return result
        }
    }
    return BuildSystemType.JPS
}
