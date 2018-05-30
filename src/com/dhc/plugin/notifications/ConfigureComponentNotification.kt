/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.dhc.plugin.util.getConfiguratorByName
import javax.swing.event.HyperlinkEvent

data class ConfigureKotlinNotificationState(
    val debugProjectName: String,
    val notificationString: String,
    val notConfiguredModules: Collection<String>
)

class ConfigureKotlinNotification(
    project: Project,
    excludeModules: List<Module>,
    val notificationState: ConfigureKotlinNotificationState
) : Notification(
        "Configure Component in Project", "Configure Component",
    notificationState.notificationString,
    NotificationType.WARNING,
    NotificationListener { notification, event ->
        if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            val configurator = getConfiguratorByName(event.description) ?: throw AssertionError("Missed action: " + event.description)
            notification.expire()

            configurator.configure(project, excludeModules)
        }
    }
) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ConfigureKotlinNotification) return false

        if (content != o.content) return false

        return true
    }
    override fun hashCode(): Int {
        return content.hashCode()
    }


}
