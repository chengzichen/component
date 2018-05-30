/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.action

import com.dhc.plugin.conf.ComponentProjectConfigurator
import com.dhc.plugin.conf.checkHideNonConfiguredNotifications
import com.dhc.plugin.platform.JvmPlatform
import com.dhc.plugin.util.getAbleToRunConfigurators
import com.dhc.plugin.util.getConfigurableModules
import com.dhc.plugin.util.isModuleConfigured
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.EditorNotifications

abstract class ConfigureComponentInProjectAction : AnAction() {

    abstract fun getApplicableConfigurators(project: Project): Collection<ComponentProjectConfigurator>

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val modules = getConfigurableModules(project)
        if (modules.all(::isModuleConfigured)) {
            Messages.showInfoMessage("All modules with Kotlin files are configured", e.presentation.text!!)
            return
        }

        val configurators = getApplicableConfigurators(project)

        when {
            configurators.size == 1 -> configurators.first().configure(project, emptyList())
            configurators.isEmpty() -> Messages.showErrorDialog("There aren't configurators available", e.presentation.text!!)
            else -> {
//                val configuratorsPopup = KotlinSetupEnvironmentNotificationProvider.createConfiguratorsPopup(project, configurators.toList())
//                configuratorsPopup.showInBestPositionFor(e.dataContext)
                pop(configurators, project, e)

            }
        }
    }
    private fun ComponentProjectConfigurator.apply(project: Project) {
        configure(project, emptyList())
        EditorNotifications.getInstance(project).updateAllNotifications()
        checkHideNonConfiguredNotifications(project)
    }
    private fun pop(configurators: Collection<ComponentProjectConfigurator>, project: Project, e: AnActionEvent) {
        val step = object : BaseListPopupStep<ComponentProjectConfigurator>("Choose Configurator", configurators.toList()) {
            override fun getTextFor(value: ComponentProjectConfigurator?) = value?.presentableText ?: "<none>"

            override fun onChosen(selectedValue: ComponentProjectConfigurator?, finalChoice: Boolean): PopupStep<*>? {
                return doFinalStep {
                    selectedValue?.apply(project)
                }
            }
        }
        JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(e.dataContext)
    }


}



class ConfigureComponentJavaInProjectAction: ConfigureComponentInProjectAction() {
    override fun getApplicableConfigurators(project: Project) = getAbleToRunConfigurators(project).filter {
        it.targetPlatform is JvmPlatform
    }
}