/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.dhc.plugin.conf

import com.intellij.codeInsight.CodeInsightUtilCore
import com.intellij.ide.actions.OpenFileAction
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.WritingAccessProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
//import com.dhc.plugin.util.hasAnyKotlinRuntimeInScope
import com.dhc.plugin.ui.ConfigureDialogWithModulesAndVersion
import com.dhc.plugin.util.*
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.util.*
import kotlin.collections.HashMap
import java.io.FileOutputStream
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.TransformerFactory
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


abstract class ComponentWithGradleConfigurator : ComponentProjectConfigurator {
    val KRY_ISRUNALONE = "isRunAlone"
    val KRY_DEBUGCOMPONENT = "debugComponent"
    val KRY_COMPILECOMPONENT = "compileComponent"
    val KRY_MAINMODULENAME = "mainmodulename"

    private var packageName = "";
    override fun getStatus(moduleSourceRootGroup: ModuleSourceRootGroup): ConfigureKotlinStatus {
        val module = moduleSourceRootGroup.baseModule
        if (!isApplicable(module)) {
            return ConfigureKotlinStatus.NON_APPLICABLE
        }

//        if (moduleSourceRootGroup.sourceRootModules.all(::hasAnyKotlinRuntimeInScope)) {
//            return ConfigureKotlinStatus.CONFIGURED
//        }

        val buildFiles = runReadAction {
            listOf(
                    module.getBuildScriptPsiFile(),
                    module.project.getTopLevelBuildScriptPsiFile()
            ).filterNotNull()
        }


        if (buildFiles.isEmpty()) {
            return ConfigureKotlinStatus.NON_APPLICABLE
        }

        if (buildFiles.none { it.isConfiguredByAnyGradleConfigurator() }) {
            return ConfigureKotlinStatus.CAN_BE_CONFIGURED
        }

        return ConfigureKotlinStatus.BROKEN
    }

    private fun PsiFile.isConfiguredByAnyGradleConfigurator(): Boolean {
        return Extensions.getExtensions(ComponentProjectConfigurator.EP_NAME)
                .filterIsInstance<ComponentWithGradleConfigurator>()
                .any { it.isFileConfigured(this) }
    }

    protected open fun isApplicable(module: Module): Boolean =
            module.getBuildSystemType() == Gradle

    protected open fun getMinimumSupportedVersion() = "1.0.4"

    private fun isFileConfigured(buildScript: PsiFile): Boolean = getManipulator(buildScript).isConfigured(kotlinPluginName)

    @JvmSuppressWildcards
    override fun configure(project: Project, excludeModules: Collection<Module>) {
        val dialog = ConfigureDialogWithModulesAndVersion(project, this, excludeModules, getMinimumSupportedVersion())

        dialog.show()
        if (!dialog.isOK) return

        project.executeCommand("Configure Component") {
            val collector = createConfigureComponentNotificationCollector(project)
            val changedFiles = configureWithVersion(project, dialog.modulesToConfigure, dialog.kotlinVersion, collector)
            val changedPropertyFile = configPropertyFile(project, dialog.modulesToConfigure, collector, dialog.hostModule)
            for (file in changedFiles) {
                OpenFileAction.openFile(file.virtualFile, project)
            }
            collector.showNotification()
        }
    }

    /**
     *     <activity
    android:name="com.dhc.flyabbit.my.MyDebugActivity"
    android:configChanges="keyboardHidden|orientation|screenSize"
    android:windowSoftInputMode="adjustResize|stateHidden"
    android:label="@string/app_name">
    <intent-filter>
    <action android:name="android.intent.action.MAIN"/>

    <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
    </activity>
     */
    fun changeAndroidManifest(file: File, module: Module) {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isIgnoringElementContentWhitespace = true
        val db: DocumentBuilder
        try {
            db = factory.newDocumentBuilder()
            val doc = db.parse(file)
            //application在改变包名的时候要改为manifest
            var manifest = getNodeOrCreate(doc, "manifest")
            var application = getNodeOrCreate(doc, "application")
            if(!isHave(doc,"application"))
            manifest.appendChild(application)
            packageName = manifest.attributes.getNamedItem("package").nodeValue
            val activity = doc.createElement("activity")
            val filter = doc.createElement("intent-filter")
            val action = doc.createElement("action")
            action.setAttribute("android:name", "android.intent.action.MAIN")
            val category = doc.createElement("category")
            category.setAttribute("android:name", "android.intent.category.LAUNCHER")
            filter.appendChild(action)
            filter.appendChild(category)
            activity.setAttribute("android:name", packageName + ".DebugActivity")
            activity.setAttribute("android:label", module.name)
            activity.appendChild(filter)
            application.appendChild(activity)
            // 写
            saveXml(getIndependentFile(file.parent), doc)
        } catch (e: Exception) {
            e.printStackTrace()
        }finally {

        }
        generatedActivity(module)
    }


    private fun generatedActivity(module: Module) {
        module. project.executeCommand("Configure Activity") {
           val fileUtil= FileUtil()
            val modlePath = File(module.moduleFilePath).parent
            val dataService = fileUtil.readFile("DebugActivity.txt")
                    .replace("&package&", packageName)
            val srcService = fileUtil.readFile("activity_debug.txt")
            fileUtil. writetoFile(dataService, getJavaFile(modlePath), "DebugActivity.java")
            fileUtil.writetoFile(srcService, getResFile(modlePath), "activity_debug.xml")
        }
    }

    private fun getIndependentFile(path: String): File {
        val file = File("$path/independent/AndroidManifest.xml")
        if (!file.exists()) {
            if( !file.parentFile.exists())
                file.parentFile .mkdirs()
            file.createNewFile()
        }
        return file
    }
    private fun getResFile(path: String): String {
        val file = File("$path/src/main/independent/res/layout")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.path
    }

    private fun getJavaFile(path: String): String {
        val file = File("$path/src/main/independent/java/$packageName")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.path
    }



    private fun configPropertyFile(project: Project, modulesToConfigure: MutableList<Module>, collector: NotificationMessageCollector, hostModule: List<Module>): HashSet<PsiFile> {
        val filesToOpen = HashSet<PsiFile>()
        project.executeWriteCommand("ConfigureProperty ", null) {
            val psiFile = project.getTopLevelPropertyPsiFile()
            if (psiFile != null) {
                val map = HashMap<String, String>()
                map[KRY_MAINMODULENAME] = hostModule[0].name
                putProperty(psiFile, map)
                filesToOpen.add(psiFile?.getPsiFile(project)!!)
            }
        }
        var i: Int = 0
        while (i < modulesToConfigure.size) {
            val tpModle = modulesToConfigure[i]
            configurePropertyModule(tpModle, tpModle.name == hostModule[0].name, collector, filesToOpen, modulesToConfigure)
            i++
        }
        return filesToOpen
    }

    private fun configurePropertyModule(module: Module, isTopLevelProjectFile: Boolean, collector: NotificationMessageCollector, filesToOpen: HashSet<PsiFile>, modulesToConfigure: MutableList<Module>): PsiFile? {
        val isModified = module.project.executeWriteCommand("ConfigureProperty ", null) {
            val file = module.getPropertyFile()
            val map = HashMap<String, String>()
            map.put(KRY_ISRUNALONE, "true")
            if (isTopLevelProjectFile) {
                var modules = ""
                modulesToConfigure.forEach { modules += it.name + "," }
                map.put(KRY_DEBUGCOMPONENT, modules)
                map.put(KRY_COMPILECOMPONENT, modules)
            }
            putProperty(file, map)
            val psiFile = file!!.getPsiFile(module.project)
            psiFile?.let { filesToOpen.add(it) }
            if (!isTopLevelProjectFile){
                val androidManifestfile = module.getAndroidManifestFile()
                changeAndroidManifest(androidManifestfile!!, module)
            }
            CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(psiFile!!)
        }
        return isModified

    }


    fun configureWithVersion(
            project: Project,
            modulesToConfigure: List<Module>,
            kotlinVersion: String,
            collector: NotificationMessageCollector
    ): HashSet<PsiFile> {
        val filesToOpen = HashSet<PsiFile>()
        val buildScript = project.getTopLevelBuildScriptPsiFile()
        if (buildScript != null && canConfigureFile(buildScript)) {
            val isModified = configureBuildScript(buildScript, true, kotlinVersion, collector)
            if (isModified) {
                filesToOpen.add(buildScript)
            }
        }
        for (module in modulesToConfigure) {
            val file = module.getBuildScriptPsiFile()
            if (file != null && canConfigureFile(file)) {
                configureModule(module, file, false, kotlinVersion, collector, filesToOpen)
            } else {
                showErrorMessage(project, "Cannot find build.gradle file for module " + module.name)
            }
        }
        return filesToOpen
    }

    private fun putProperty(propertyPsiFile: File?, map: HashMap<String, String>) {
        val properties = Properties()
        propertyPsiFile?.setReadable(true)
        propertyPsiFile?.setWritable(true)
        val input = BufferedInputStream(FileInputStream(propertyPsiFile))
        properties.load(input)
        input.close()
        val output = FileOutputStream(propertyPsiFile)
        val newMap = map.filter {
            properties.getProperty(it.key, "") != it.value
        }
        newMap.forEach { t, u -> properties.setProperty(t, u) }
        properties.store(output, null)
        output.close()
    }

    open fun configureModule(
            module: Module,
            file: PsiFile,
            isTopLevelProjectFile: Boolean,
            version: String,
            collector: NotificationMessageCollector,
            filesToOpen: MutableCollection<PsiFile>
    ) {
        val isModified = configureBuildScript(file, isTopLevelProjectFile, version, collector)
        if (isModified) {
            filesToOpen.add(file)
        }
    }

    protected fun configureModuleBuildScript(file: PsiFile, version: String): Boolean {
        val sdk = ModuleUtil.findModuleForPsiElement(file)?.let { ModuleRootManager.getInstance(it).sdk }
        val jvmTarget = getJvmTarget(sdk, version)
        return getManipulator(file).configureModuleBuildScript(
                kotlinPluginName,
                getStdlibArtifactName(sdk, version),
                version,
                jvmTarget
        )
    }

    protected open fun getStdlibArtifactName(sdk: Sdk?, version: String) = getStdlibArtifactId(sdk, version)

    protected open fun getJvmTarget(sdk: Sdk?, version: String): String? = null

    protected abstract val kotlinPluginName: String

    protected open fun addElementsToFile(
            file: PsiFile,
            isTopLevelProjectFile: Boolean,
            version: String
    ): Boolean {
        if (!isTopLevelProjectFile) {
            var wasModified = configureProjectFile(file, version)
            wasModified = wasModified or configureModuleBuildScript(file, version)
            return wasModified
        }
        return false
    }

    private fun configureBuildScript(
            file: PsiFile,
            isTopLevelProjectFile: Boolean,
            version: String,
            collector: NotificationMessageCollector
    ): Boolean {
        val isModified = file.project.executeWriteCommand("Configure ${file.name}", null) {
            val isModified = addElementsToFile(file, isTopLevelProjectFile, version)

            CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(file)
            isModified
        }

        val virtualFile = file.virtualFile
        if (virtualFile != null && isModified) {
            collector.addMessage(virtualFile.path + " was modified")
        }
        return isModified
    }

//    override fun updateLanguageVersion(
//        module: Module,
//        languageVersion: String?,
//        apiVersion: String?,
//        requiredStdlibVersion: ApiVersion,
//        forTests: Boolean
//    ) {
//        val runtimeUpdateRequired = getRuntimeLibraryVersion(module)?.let { ApiVersion.parse(it) }?.let { runtimeVersion ->
//            runtimeVersion < requiredStdlibVersion
//        } ?: false
//
//        if (runtimeUpdateRequired) {
//            Messages.showErrorDialog(
//                module.project,
//                "This language feature requires version $requiredStdlibVersion or later of the Kotlin runtime library. " +
//                        "Please update the version in your build script.",
//                "Update Language Version"
//            )
//            return
//        }
//
//        val element = changeLanguageVersion(module, languageVersion, apiVersion, forTests)
//
//        element?.let {
//            OpenFileDescriptor(module.project, it.containingFile.virtualFile, it.textRange.startOffset).navigate(true)
//        }
//    }

    override fun changeCoroutineConfiguration(module: Module, state: State) {
        val element = changeCoroutineConfiguration(module, getCompilerArgument(state))
        if (element != null) {
            OpenFileDescriptor(module.project, element.containingFile.virtualFile, element.textRange.startOffset).navigate(true)
        }
    }

    fun getCompilerArgument(state: State): String = when (state) {
        State.ENABLED -> "enable"
        State.ENABLED_WITH_WARNING -> "warn"
        State.ENABLED_WITH_ERROR, State.DISABLED -> "error"
    }

    companion object {
        fun getManipulator(file: PsiFile): GradleBuildScriptManipulator = when (file) {
            is GroovyFile -> GroovyBuildScriptManipulator(file)
            else -> error("Unknown build script file type!")
        }

        private val KOTLIN_BUILD_SCRIPT_NAME = "build.gradle.kts"


        fun changeCoroutineConfiguration(module: Module, coroutineOption: String): PsiElement? = changeBuildGradle(module) {
            getManipulator(it).changeCoroutineConfiguration(coroutineOption)
        }


        private fun changeBuildGradle(module: Module, body: (PsiFile) -> PsiElement?): PsiElement? {
            val buildScriptFile = module.getBuildScriptPsiFile()
            if (buildScriptFile != null && canConfigureFile(buildScriptFile)) {
                return buildScriptFile.project.executeWriteCommand("Change build.gradle configuration", null) {
                    body(buildScriptFile)
                }
            }
            return null
        }


        fun configureProjectFile(file: PsiFile, version: String): Boolean = getManipulator(file).configureProjectBuildScript(version)

        private fun canConfigureFile(file: PsiFile): Boolean = WritingAccessProvider.isPotentiallyWritable(file.virtualFile, null)

        private fun Module.getBuildScriptPsiFile() = getBuildScriptFile()?.getPsiFile(project)

        private fun Project.getTopLevelBuildScriptPsiFile() = basePath?.let { findBuildGradleFile(it)?.getPsiFile(this) }

        private fun Project.getTopLevelPropertyPsiFile() = basePath?.let { findPropertyPsiFile(it) }

        private fun Module.getPropertyPsiFile() = getPropertyFile()

        private fun Module.getPropertyFile(): File? {
            val moduleDir = File(moduleFilePath).parent
            findPropertyPsiFile(moduleDir).let {
                return it
            }
        }

        private fun Module.getAndroidManifestFile(): File? {
            val moduleDir = File(moduleFilePath).parent
            findAndroidManifestFile(moduleDir).let {
                return it
            }
        }

        private fun Module.getBuildScriptFile(): File? {
            val moduleDir = File(moduleFilePath).parent
            findBuildGradleFile(moduleDir)?.let {
                return it
            }

            ModuleRootManager.getInstance(this).contentRoots.forEach { root ->
                findBuildGradleFile(root.path)?.let {
                    return it
                }
            }

            ExternalSystemApiUtil.getExternalProjectPath(this)?.let { externalProjectPath ->
                findBuildGradleFile(externalProjectPath)?.let {
                    return it
                }
            }

            return null
        }

        private fun findPropertyPsiFile(path: String): File {
            val file = File(path + "/" + "gradle.properties")
            if (!file.exists()) {
                file.getParentFile().mkdirs()
                file.createNewFile()
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            }
            return file
        }

        private fun findAndroidManifestFile(path: String): File {
            val file = File(path + "/src/main/" + "AndroidManifest.xml")
            if (!file.exists()) {
                file.getParentFile().mkdirs()
                file.createNewFile()
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            }
            return file
        }


        private fun findBuildGradleFile(path: String): File? =
                File(path + "/" + "build.gradle").takeIf { it.exists() }
                        ?: File(path + "/" + KOTLIN_BUILD_SCRIPT_NAME).takeIf { it.exists() }

        private fun File.getPsiFile(project: Project) = VfsUtil.findFileByIoFile(this, true)?.let {
            PsiManager.getInstance(project).findFile(it)
        }

        private fun File.getVirtualFile(project: Project) = VfsUtil.findFileByIoFile(this, true)

        private fun showErrorMessage(project: Project, message: String?) {
            Messages.showErrorDialog(
                    project,
                    "<html>Couldn't configure  component-gradle plugin automatically.<br/>" +
                            (if (message != null) message + "<br/>" else "") +
                            "<br/>See manual installation instructions <a href=\"https://github.com/chengzichen/Flyabbit\">here</a>.</html>",
                    "Configure  Component-Gradle Plugin"
            )
        }
    }
}
