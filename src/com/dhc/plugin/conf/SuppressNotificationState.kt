/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.conf

import com.dhc.plugin.conf.ModuleSourceRootGroup
import com.dhc.plugin.conf.toModuleGroup
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "SuppressABINotification")
class SuppressNotificationState : PersistentStateComponent<SuppressNotificationState> {
    var isSuppressed: Boolean = false
    var modulesWithSuppressedNotConfigured = sortedSetOf<String>()

    override fun getState(): SuppressNotificationState = this

    override fun loadState(state: SuppressNotificationState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project) = ServiceManager.getService(project, SuppressNotificationState::class.java)

        fun isKotlinNotConfiguredSuppressed(moduleGroup: ModuleSourceRootGroup): Boolean {
            val baseModule = moduleGroup.baseModule
            return baseModule.name in getInstance(baseModule.project).modulesWithSuppressedNotConfigured
        }

        fun suppressKotlinNotConfigured(module: Module) {
            getInstance(module.project).modulesWithSuppressedNotConfigured.add(module.toModuleGroup().baseModule.name)
        }
    }
}
