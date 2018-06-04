/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.conf

import com.intellij.notification.Notification
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.dhc.plugin.notifications.ConfigureComponentNotification
import com.dhc.plugin.util.isNotConfiguredNotificationRequired
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
object ConfigureComponentNotificationManager:ComponentSingleNotificationManager<ConfigureComponentNotification> {

    fun getVisibleNotifications(project: Project): Array<out ConfigureComponentNotification> {
        return NotificationsManager.getNotificationsManager().getNotificationsOfType(ConfigureComponentNotification::class.java, project)
    }

    fun expireOldNotifications(project: Project) {
        expireOldNotifications(project, ConfigureComponentNotification::class)
    }
}

interface ComponentSingleNotificationManager<in T: Notification> {

    fun expireOldNotifications(project: Project, notificationClass: KClass<out T>, notification: T? = null): Boolean {
        val notificationsManager = NotificationsManager.getNotificationsManager()
        var isNotificationExists = false

        val notifications = notificationsManager.getNotificationsOfType(notificationClass.java, project)
        for (oldNotification in notifications) {
            if (oldNotification == notification) {
                isNotificationExists = true
            } else {
                oldNotification?.expire()
            }
        }
        return isNotificationExists
    }
}

private val checkInProgress = AtomicBoolean(false)

fun checkHideNonConfiguredNotifications(project: Project) {
    if (checkInProgress.get()) return
    val notification = ConfigureComponentNotificationManager.getVisibleNotifications(project).firstOrNull() ?: return

    ApplicationManager.getApplication().executeOnPooledThread {
        if (!checkInProgress.compareAndSet(false, true)) return@executeOnPooledThread

        DumbService.getInstance(project).waitForSmartMode()
        val moduleSourceRootMap = ModuleSourceRootMap(project)

        if (notification.notificationState.debugProjectName != project.name) {
            LOG.error("Bad notification check for project: ${project.name}\n${notification.notificationState}")
        }

        val hideNotification =
            if (!ApplicationManager.getApplication().isUnitTestMode) {
                try {
                    val moduleSourceRootGroups = notification.notificationState.notConfiguredModules
                        .mapNotNull { ModuleManager.getInstance(project).findModuleByName(it) }
                        .map { moduleSourceRootMap.getWholeModuleGroup(it) }
                    moduleSourceRootGroups.none(::isNotConfiguredNotificationRequired)
                } catch (e: IndexNotReadyException) {
                    checkInProgress.set(false)
                    ApplicationManager.getApplication().invokeLater {
                        checkHideNonConfiguredNotifications(project)
                    }
                    return@executeOnPooledThread
                }
            } else {
                true
            }

        if (hideNotification) {
            ApplicationManager.getApplication().invokeLater {
                ConfigureComponentNotificationManager.expireOldNotifications(project)
                checkInProgress.set(false)
            }
        } else {
            checkInProgress.set(false)
        }
    }
}

private val LOG = Logger.getInstance(ConfigureComponentNotificationManager::class.java)