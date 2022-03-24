package com.ezteam.windowslauncher.viewmodel

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.callback.MultipleFileCallback
import com.anggrayudi.storage.file.copyTo
import com.anggrayudi.storage.file.makeFolder
import com.anggrayudi.storage.file.moveTo
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.viewmodel.BaseViewModel
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.database.repository.LauncherRepository
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.QuickAccessModel
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.utils.PresKey
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils
import com.ezteam.windowslauncher.utils.launcher.LauncherType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

class FileManagerViewModel(
    application: Application,
    private var repository: LauncherRepository
) :
    BaseViewModel(application) {

    enum class StageFile {
        None, Cut, Copy
    }

    private var scope = CoroutineScope(Dispatchers.IO)
    var stackFolderUndo = MutableLiveData(Stack<String>())
    var stackFolderRedo = MutableLiveData(Stack<String>())
    var lstFolderSelected = MutableLiveData(mutableListOf<File>())
    var totalFileInFolder = MutableLiveData(mutableListOf<File>())
    var lstFolderSave = MutableLiveData(mutableListOf<File>())
    var expendLeftLayout = MutableLiveData(false)
    var stageFileSave = StageFile.None
    var lstQuickAccess = mutableListOf<File>()
    val liveDataQuickAccess: LiveData<MutableList<File>> =
        Transformations.map(repository.getQuickAccess()) {
            val datas = mutableListOf<File>()
            it.forEach { itemQuickAccess ->
                if (itemQuickAccess.path == Config.FileManager.desktopPath(application)
                    || File(itemQuickAccess.path).exists()
                ) {
                    datas.add(File(itemQuickAccess.path))
                } else {
                    scope.launch {
                        repository.deleteQuickAccess(mutableListOf(itemQuickAccess))
                    }
                }
            }
            datas
        }

    init {
        if (!PreferencesUtils.getBoolean(Config.Preferences.initDataQuickAccess, false)) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.insertQuickAccess(
                    mutableListOf<QuickAccessModel>().apply {
                        add(QuickAccessModel(Config.FileManager.desktopPath(application)))
                        add(QuickAccessModel(Config.FileManager.downloadPath!!))
                        add(QuickAccessModel(Config.FileManager.documentPath!!))
                        add(QuickAccessModel(Config.FileManager.picturePath!!))
                    }
                )
            }
            PreferencesUtils.putBoolean(Config.Preferences.initDataQuickAccess, true)
        }
        liveDataQuickAccess.observeForever {
            lstQuickAccess.clear()
            lstQuickAccess.addAll(it ?: mutableListOf())
        }
    }


    var folderIsGridView =
        MutableLiveData(PreferencesUtils.getBoolean(Config.Preferences.folderIsGridView, true))

    fun resetViewModel() {
        stackFolderUndo.postValue(Stack<String>().apply {
            push(Config.FileManager.thisPCPath)
        })
        stackFolderRedo.postValue(Stack<String>())
        lstFolderSelected.postValue(mutableListOf())
        totalFileInFolder.postValue(mutableListOf())
        lstFolderSave.postValue(mutableListOf())
        expendLeftLayout.postValue(false)
    }

    fun setStageFile(stage: StageFile) {
        stageFileSave = stage
        val data = mutableListOf<File>()
        if (stage != StageFile.None) {
            data.addAll(lstFolderSelected.value ?: mutableListOf())
        }
        lstFolderSave.postValue(data)
        cleanStackSelected()
    }

    fun getCurrentFolder(): String {
        stackFolderUndo.value?.let {
            if (it.isEmpty())
                return ""
            return it.peek()
        }
        return ""
    }

    fun getKeyBundleStageRcv(): String {
        val key = StringBuffer()
        stackFolderUndo.value?.forEach {
            key.append(it)
        }
        return key.toString()
    }

    fun pushFolderSelected(path: File) {
        path.let {
            val data = lstFolderSelected.value
            data?.let { lst ->
                if (lst.contains(path)) {
                    lst.remove(path)
                } else {
                    lst.add(path)
                }
                lstFolderSelected.postValue(lst)
            }
        }
    }

    fun cleanStackSelected() {
        lstFolderSelected.postValue(mutableListOf())
    }

    fun pushDataUndo(path: String?) {
        val stack = stackFolderUndo.value
        stack?.let {
            if (!it.isEmpty() && path == it.peek())
                return
            it.push(path)
            stackFolderUndo.postValue(it)
            stackFolderRedo.value?.clear()
        }
    }

    fun popDataStack(popUndo: Boolean) {
        val undo = stackFolderUndo.value
        val redo = stackFolderRedo.value
        if (popUndo)
            redo?.push(undo?.pop())
        else
            undo?.push(redo?.pop())
        stackFolderRedo.postValue(redo)
        stackFolderUndo.postValue(undo)
    }

    fun moveFile(callback: MultipleFileCallback) {
        scope.launch {
            val lstData = mutableListOf<DocumentFile>()
            lstFolderSave.value?.forEach {
                lstData.add(DocumentFile.fromFile(it))
                repository.checkQuickAccessExist(it.path)?.let {
                    repository.deleteQuickAccess(mutableListOf(QuickAccessModel(it.path)))
                }
            }
            if (stageFileSave == StageFile.Cut) {
                lstData.moveTo(
                    getApplication(),
                    DocumentFile.fromFile(File(getCurrentFolder())),
                    false,
                    callback
                )
            } else {
                lstData.copyTo(
                    getApplication(),
                    DocumentFile.fromFile(File(getCurrentFolder())),
                    false,
                    callback
                )
            }
        }
    }

    fun deleteFile() {
        val lstDelete = mutableListOf<QuickAccessModel>()
        lstFolderSelected.value?.forEach {
            if (!Config.FileManager.isPrimaryFolder(it.path, getApplication())) {
                DocumentFile.fromFile(it).delete()
                repository.checkQuickAccessExist(it.path)?.let { model ->
                    lstDelete.add(QuickAccessModel(model.path))
                }
            }
            deleteFromDesktop(it.path)
        }
        if (lstDelete.isNotEmpty()) {
            scope.launch {
                repository.deleteQuickAccess(lstDelete)
                scope.launch(Dispatchers.Main) {
                    Handler().postDelayed({
                        setStageFile(StageFile.None)
                    }, 300)
                }
            }
        } else {
            setStageFile(StageFile.None)
        }
    }

    fun deleteFile(path: String) {
        val file = File(path)
        DocumentFile.fromFile(file).delete()
        deleteFromDesktop(path)
    }

    private fun deleteFromDesktop(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val launcher = repository.launcherByPackage(path)
            launcher?.let {
                repository.delete(it)
            }
        }
    }

    fun pinOrUnpinAccess(isPin: Boolean) {
        val data = mutableListOf<QuickAccessModel>()
        lstFolderSelected.value?.forEach {
            if (!Config.FileManager.isPrimaryFolder(it.path, getApplication())) {
                data.add(QuickAccessModel(it.path))
            }
        }
        scope.launch {
            if (isPin) {
                repository.insertQuickAccess(data)
            } else {
                repository.deleteQuickAccess(data)
            }
            scope.launch(Dispatchers.Main) {
                Handler().postDelayed({
                    setStageFile(StageFile.None)
                }, 300)
            }
        }
    }

    fun renameFile(newName: String) {
        /*list only 1 item*/
        lstFolderSelected.value?.forEach {
            renameFile(it, newName)
        }
        Handler().postDelayed({
            setStageFile(StageFile.None)
        }, 300)
    }

    fun renameFile(file: File, newName: String) {
        DocumentFile.fromFile(file)
            .renameTo(file.name.replace(FilenameUtils.getBaseName(file.path), newName))
        repository.checkQuickAccessExist(file.path)?.let { model ->
            scope.launch {
                repository.deleteQuickAccess(mutableListOf(QuickAccessModel(model.path)))
                repository.insertQuickAccess(
                    mutableListOf(
                        QuickAccessModel(
                            model.path.replace(
                                FilenameUtils.getBaseName(model.path),
                                newName
                            )
                        )
                    )
                )
            }
        }

        renameFileFromDesktop(file, newName)
    }

    private fun renameFileFromDesktop(file: File, newName: String) {
        val pathOld = file.path
        val pathNew = file.path.replace(FilenameUtils.getBaseName(pathOld), newName)
        viewModelScope.launch(Dispatchers.IO) {
            val launcher = repository.launcherByPackage(pathOld)
            launcher?.let {
                repository.delete(it)
                it.appName = FilenameUtils.getBaseName(pathNew)
                it.packageName = pathNew
                repository.insert(it)
            }
        }
    }

    fun createFolder(context: Context, folderName: String, parent: String = getCurrentFolder()) {
        File(parent).apply {
            if (exists()) {
                val file = makeFolder(getApplication(), folderName)
                file?.let {
                    if (parent == Config.FileManager.desktopPath(getApplication())) {
                        createDesktopFolder(context, it.path)
                    }
                }
            }
        }
        setStageFile(StageFile.None)
    }

    private fun createDesktopFolder(context: Context, folderPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val launcher = LauncherModel(
                packageName = folderPath,
                logoResId = R.drawable.ic_folder_default,
                appName = FilenameUtils.getName(folderPath),
                prioritize = 4,
                launcherType = LauncherType.APP_FOLDER.value
            )
            insertToDesktop(launcher, context)
        }
    }

    private fun insertToDesktop(launcher: LauncherModel, context: Context) {
        var position = -1
        val apps = repository.getDesktopAppsList(PreferencesUtils.getInteger(PresKey.SORT_STATE))
        for (index in 0 until LauncherAppUtils.getSizeGrid(context).getSize()) {
            apps.find {
                it.position == index
            }?.let {

            } ?: kotlin.run {
                position = index
            }
            if (position != -1) {
                break
            }
        }
        launcher.position = position
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(launcher)
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}