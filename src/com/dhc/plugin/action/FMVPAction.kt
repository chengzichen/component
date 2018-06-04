package com.dhc.plugin.action

import com.dhc.plugin.conf.ComponentWithGradleConfigurator
import com.dhc.plugin.conf.ComponentWithGradleConfigurator.Companion.getBuildScriptPsiFile
import com.dhc.plugin.conf.createConfigureComponentNotificationCollector
import com.dhc.plugin.util.FileUtil
import com.dhc.plugin.util.allModules
import com.dhc.plugin.util.executeWriteCommand
import com.intellij.execution.util.JavaParametersUtil.configureModule
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.ui.tree.Navigatable
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import java.io.*

class FMVPAction : AnAction() {
    internal var project: Project? = null
    internal var selectGroup: VirtualFile? = null

    override fun update(event: AnActionEvent?) {
        super.update(event)
        val presentation = event!!.presentation
        presentation.isEnabledAndVisible = isRightPackage(event)
    }


    override fun actionPerformed(e: AnActionEvent) {
        project = e.project
        val module = selectGroup?.let { ProjectRootManager.getInstance(project!!).fileIndex.getModuleForFile(it) }
        val projectFileIndexs = selectGroup?.let { ProjectRootManager.getInstance(project!!).fileIndex.getPackageNameByDirectory(it) }
        //TODO 使用该api来实现
        val className = Messages.showInputDialog(project, "input template name", "Create FMVP Template", Messages.getQuestionIcon())
        if (isEmpty(className == null, className == "")) {
            Messages.showErrorDialog(
                    "You have to type in something.",
                    "content is empty")
            return
        }
        project?.executeWriteCommand("Create FMVP Template ", null) {
            val collector = createConfigureComponentNotificationCollector(this.project!!)
            createClassMvp(className!!)
            configGradle(module)
            collector.addMessage("Create FMVP Template was modified")
        }
        project!!.baseDir.refresh(false, true)
    }

    private fun configGradle(module: Module?) {

        val file = module!!.getBuildScriptPsiFile()
        if (file != null && ComponentWithGradleConfigurator.canConfigureFile(file)) {
            ComponentWithGradleConfigurator.getManipulator(file).configureModuleBuildScriptWithMVP("dagger-compiler","arouter-compiler")
        } else {
            ComponentWithGradleConfigurator.showErrorMessage(module.project, "Cannot find build.gradle file for module " + module.name)
        }

    }

    private fun isEmpty(b: Boolean, equals: Boolean): Boolean {
        return b || equals
    }


    private fun isRightPackage(actionEvent: AnActionEvent): Boolean {
        selectGroup = DataKeys.VIRTUAL_FILE.getData(actionEvent.dataContext)
        val packageName = selectGroup!!.path
        if (packageName == null || packageName == "")
            return false
        val subPackages = packageName.split("\\/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (subPackage in subPackages) {
            if (subPackage.endsWith("java")) {
                return true
            }
        }
        return false
    }

    /**
     * 根据模块生成代码
     *
     * @param className
     */
    private fun createClassMvp(className: String) {
        var className = className
        val isFragment = isEmpty(className.endsWith("Fragment"), className.endsWith("fragment"))
        val isActivity = isEmpty(className.endsWith("Activity"), className.endsWith("activity"))
        if (className.endsWith("Fragment") || className.endsWith("fragment") || className.endsWith("Activity") || className.endsWith("activity")) {
            className = className.substring(0, className.length - 8)
        }
        val contractPath = selectGroup!!.path + "/presenter/contract"
        val presenterPath = selectGroup!!.path + "/presenter"
        val uiPath = selectGroup!!.path + "/ui"
        val modlePath = selectGroup!!.path + "/modle"
        val diPath = selectGroup!!.path + "/di"
        val componentPath = selectGroup!!.path + "/di/component"
        className = className.substring(0, 1).toUpperCase() + className.substring(1)
        val fileUtil= FileUtil()
        val contract =fileUtil. readFile("Contract.txt")
                .replace("&package&", getPackageName(contractPath))
                .replace("&Contract&", "I" + className + "Contract")
        val presenter = fileUtil.readFile("Presenter.txt")
                .replace("&package&", getPackageName(presenterPath))
                .replace("&Module&", className)
                .replace("&Contract&", "I" + className + "Contract")
                .replace("&ContractPackageName&", getPackageName(contractPath))
                .replace("&DataServicePackageName&", getPackageName(modlePath))
                .replace("&Presenter&", className + "Presenter")
        val dataService =fileUtil. readFile("DataService.txt")
                .replace("&package&", getPackageName(modlePath))
                .replace("&Module&", className)
                .replace("&ContractPackageName&", getPackageName(contractPath))
                .replace("&Contract&", "I" + className + "Contract")

        if (isFragment) {
            val fragment = fileUtil.readFile("Fragment.txt")
                    .replace("&package&", getPackageName(uiPath))
                    .replace("&Fragment&", className + "Fragment")
                    .replace("&ContractPackageName&", getPackageName(contractPath))
                    .replace("&Contract&", "I" + className + "Contract")
                    .replace("&Presenter&", className + "Presenter")
            fileUtil. writetoFile(fragment, uiPath, className + "Fragment.java")
        } else if (isActivity) {
            val activity = fileUtil.readFile("Activity.txt")
                    .replace("&package&", getPackageName(uiPath))
                    .replace("&Activity&", className + "Activity")
                    .replace("&ContractPackageName&", getPackageName(contractPath))
                    .replace("&Contract&", "I" + className + "Contract")
                    .replace("&Presenter&", className + "Presenter")
            fileUtil. writetoFile(activity, uiPath, className + "Activity.java")
        }
        val activityComponent =fileUtil. readFile("ActivityComponent.txt")
                .replace("&package&", getPackageName(componentPath))
        val fragmentComponent =fileUtil. readFile("FragmentComponent.txt")
                .replace("&package&", getPackageName(componentPath))
        val diHelper =fileUtil. readFile("DiHelper.txt")
                .replace("&package&", getPackageName(diPath))
                .replace("&diComponentPackageName&", getPackageName(componentPath))

        fileUtil. writetoFile(contract, contractPath, "I" + className + "Contract.java")
        fileUtil.  writetoFile(presenter, presenterPath, className + "Presenter.java")
        fileUtil.  writetoFile(dataService, modlePath, className + "RemoteDataService.java")
        fileUtil.  writetoFile(activityComponent, componentPath, "ActivityComponent.java")
        fileUtil.  writetoFile(fragmentComponent, componentPath, "FragmentComponent.java")
        fileUtil.  writetoFile(diHelper, diPath, "DiHelper.java")
    }

    private fun getPackageName(path: String): String {
        return path.substring(path.indexOf("java") + 5, path.length).replace("/", ".")
    }


}
