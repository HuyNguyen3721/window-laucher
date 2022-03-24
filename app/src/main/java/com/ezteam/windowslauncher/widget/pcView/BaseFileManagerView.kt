package com.ezteam.windowslauncher.widget.pcView

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.documentfile.provider.DocumentFile
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.callback.FolderCallback
import com.anggrayudi.storage.callback.MultipleFileCallback
import com.anggrayudi.storage.file.FileSize
import com.ezteam.baseproject.extensions.fragmentActivity
import com.ezteam.baseproject.extensions.infoDetail
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.dialog.InputNameDialog
import com.ezteam.windowslauncher.viewmodel.FileManagerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.apache.commons.io.FilenameUtils
import org.koin.java.KoinJavaComponent
import java.io.File

open class BaseFileManagerView(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    val viewModel by KoinJavaComponent.inject(FileManagerViewModel::class.java)


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel.stackFolderUndo.removeObserver { }
        viewModel.stackFolderRedo.removeObserver { }
        viewModel.lstFolderSelected.removeObserver { }
        viewModel.totalFileInFolder.removeObserver { }
        viewModel.lstFolderSave.removeObserver { }
    }

    fun toast(content: String?) {
        if (!TextUtils.isEmpty(content)) Toast.makeText(
            context,
            content,
            Toast.LENGTH_SHORT
        ).show()
    }


    fun openFolderByPath(path: String?) {
        path?.let {
            File(it).apply {
                if (exists() && isDirectory) {
                    viewModel.pushDataUndo(path)
                }
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    fun createMultipleFileCallback() =
        object : MultipleFileCallback(CoroutineScope(Dispatchers.Main)) {

            var dialog: MaterialDialog? = null
            var tvStatus: TextView? = null
            var progressBar: ProgressBar? = null

            override fun onStart(
                files: List<DocumentFile>,
                totalFilesToCopy: Int,
                workerThread: Thread
            ): Long {
                var totalSize = 0L
                files.forEach {
                    totalSize += it.length()
                }
                if (totalSize > 10 * FileSize.MB) {
                    dialog = MaterialDialog(context)
                        .cancelable(false)
                        .positiveButton(android.R.string.cancel) { workerThread.interrupt() }
                        .customView(R.layout.dialog_copy_progress).apply {
                            tvStatus = getCustomView().findViewById<TextView>(R.id.tvProgressStatus)
                                .apply {
                                    text = context.getString(R.string.copy_file, 0, "%")
                                }

                            progressBar =
                                getCustomView().findViewById<ProgressBar>(R.id.progressCopy).apply {
                                    isIndeterminate = true
                                }
                            show()
                        }
                }
                return 500
            }

            override fun onParentConflict(
                destinationParentFolder: DocumentFile,
                conflictedFolders: MutableList<ParentConflict>,
                conflictedFiles: MutableList<ParentConflict>,
                action: ParentFolderConflictAction
            ) {
                handleParentFolderConflict(conflictedFolders, conflictedFiles, action)
            }

            override fun onContentConflict(
                destinationParentFolder: DocumentFile,
                conflictedFiles: MutableList<FolderCallback.FileConflict>,
                action: FolderCallback.FolderContentConflictAction
            ) {
                handleFolderContentConflict(action, conflictedFiles)
            }

            override fun onReport(report: Report) {
                tvStatus?.text =
                    context.getString(R.string.copy_file, report.progress.toInt(), "%")
                progressBar?.isIndeterminate = false
                progressBar?.progress = report.progress.toInt()
            }

            override fun onCompleted(result: Result) {
                dialog?.dismiss()
                viewModel.setStageFile(FileManagerViewModel.StageFile.None)
            }

            override fun onFailed(errorCode: ErrorCode) {
                dialog?.dismiss()
                toast(context.getString(R.string.an_error_has) + errorCode)
            }
        }

    private fun handleParentFolderConflict(
        conflictedFolders: MutableList<MultipleFileCallback.ParentConflict>,
        conflictedFiles: MutableList<MultipleFileCallback.ParentConflict>,
        action: MultipleFileCallback.ParentFolderConflictAction
    ) {
        val newSolution = ArrayList<MultipleFileCallback.ParentConflict>(conflictedFiles.size)
        askFolderSolution(action, conflictedFolders, conflictedFiles, newSolution)
    }

    private fun handleFolderContentConflict(
        action: FolderCallback.FolderContentConflictAction,
        conflictedFiles: MutableList<FolderCallback.FileConflict>
    ) {
        val newSolution = ArrayList<FolderCallback.FileConflict>(conflictedFiles.size)
        askSolution(action, conflictedFiles, newSolution)
    }

    private fun askSolution(
        action: FolderCallback.FolderContentConflictAction,
        conflictedFiles: MutableList<FolderCallback.FileConflict>,
        newSolution: MutableList<FolderCallback.FileConflict>
    ) {
        val currentSolution = conflictedFiles.removeFirstOrNull()
        if (currentSolution == null) {
            action.confirmResolution(newSolution)
            return
        }
        var doForAll = false
        MaterialDialog(context)
            .cancelable(false)
            .title(text = context.getString(R.string.conflict_found))
            .message(text = context.getString(R.string.folder_already, currentSolution.target.name))
            .checkBoxPrompt(text = context.getString(R.string.apply_to_all)) { doForAll = it }
            .listItems(
                items = listOf(
                    context.getString(R.string.replace),
                    context.getString(R.string.create_new),
                    context.getString(R.string.skip_duplicate)
                )
            ) { _, index, _ ->
                currentSolution.solution = FileCallback.ConflictResolution.values()[index]
                newSolution.add(currentSolution)
                if (doForAll) {
                    conflictedFiles.forEach { it.solution = currentSolution.solution }
                    newSolution.addAll(conflictedFiles)
                    action.confirmResolution(newSolution)
                } else {
                    askSolution(action, conflictedFiles, newSolution)
                }
            }
            .show()
    }

    private fun askFolderSolution(
        action: MultipleFileCallback.ParentFolderConflictAction,
        conflictedFolders: MutableList<MultipleFileCallback.ParentConflict>,
        conflictedFiles: MutableList<MultipleFileCallback.ParentConflict>,
        newSolution: MutableList<MultipleFileCallback.ParentConflict>
    ) {
        val currentSolution = conflictedFolders.removeFirstOrNull()
        if (currentSolution == null) {
            askFileSolution(action, conflictedFolders, conflictedFiles, newSolution)
            return
        }
        var doForAll = false
        val canMerge = currentSolution.canMerge
        MaterialDialog(context)
            .cancelable(false)
            .title(text = context.getString(R.string.conflict_found))
            .message(text = context.getString(R.string.folder_already, currentSolution.target.name))
            .checkBoxPrompt(text = context.getString(R.string.apply_to_all)) { doForAll = it }
            .listItems(
                items = mutableListOf(
                    context.getString(R.string.replace),
                    context.getString(R.string.merge),
                    context.getString(R.string.create_new),
                    context.getString(R.string.skip_duplicate)
                ).apply { if (!canMerge) remove(context.getString(R.string.merge)) }) { _, index, _ ->
                currentSolution.solution =
                    FolderCallback.ConflictResolution.values()[if (!canMerge && index > 0) index + 1 else index]
                newSolution.add(currentSolution)
                if (doForAll) {
                    conflictedFolders.forEach { it.solution = currentSolution.solution }
                    newSolution.addAll(conflictedFolders)
                    askFileSolution(action, conflictedFolders, conflictedFiles, newSolution)
                } else {
                    askFolderSolution(action, conflictedFolders, conflictedFiles, newSolution)
                }
            }
            .show()
    }

    private fun askFileSolution(
        action: MultipleFileCallback.ParentFolderConflictAction,
        conflictedFolders: MutableList<MultipleFileCallback.ParentConflict>,
        conflictedFiles: MutableList<MultipleFileCallback.ParentConflict>,
        newSolution: MutableList<MultipleFileCallback.ParentConflict>
    ) {
        val currentSolution = conflictedFiles.removeFirstOrNull()
        if (currentSolution == null) {
            action.confirmResolution(newSolution.plus(conflictedFolders))
            return
        }
        var doForAll = false
        MaterialDialog(context)
            .cancelable(false)
            .title(text = context.getString(R.string.conflict_found))
            .message(text = context.getString(R.string.folder_already, currentSolution.target.name))
            .checkBoxPrompt(text = context.getString(R.string.apply_to_all)) { doForAll = it }
            .listItems(
                items = mutableListOf(
                    context.getString(R.string.replace),
                    context.getString(R.string.create_new),
                    context.getString(R.string.skip_duplicate)
                )
            ) { _, index, _ ->
                currentSolution.solution =
                    FolderCallback.ConflictResolution.values()[if (index > 0) index + 1 else index]
                newSolution.add(currentSolution)
                if (doForAll) {
                    conflictedFiles.forEach { it.solution = currentSolution.solution }
                    newSolution.addAll(conflictedFiles)
                    action.confirmResolution(newSolution.plus(conflictedFolders))
                } else {
                    askFolderSolution(action, conflictedFolders, conflictedFiles, newSolution)
                }
            }
            .show()
    }

    fun showPropertiesFile(file: File) {
        MaterialDialog(context)
            .cancelable(false)
            .title(text = context.getString(R.string.properties))
            .message(text = Html.fromHtml(file.infoDetail))
            .positiveButton(0, context.getString(R.string.ok)) {
                it.dismiss()
            }
            .show()
    }

    fun showDilogRenameFile() {
        viewModel.lstFolderSelected.value?.let {
            if (it.isEmpty())
                return
            val dialog = InputNameDialog.ExtendBuilder()
                .setFileName(FilenameUtils.getBaseName(it[0].path))
                .setTitle(resources.getString(R.string.rename))
                .onSetPositiveButton(resources.getString(R.string.save)) { _, data ->
                    data[InputNameDialog.INPUT_NAME]?.let { name ->
                        viewModel.renameFile(name as String)
                    }
                }
                .onSetNegativeButton(resources.getString(R.string.cancel)) {}
                .build()
            context.fragmentActivity()?.supportFragmentManager?.let { manager ->
                dialog.show(
                    manager, InputNameDialog::javaClass.name
                )
            }
        }

    }


    fun showDialogDelete() {
        MaterialDialog(context)
            .cancelable(false)
            .title(text = context.getString(R.string.delete))
            .message(
                text = context.getString(
                    R.string.request_delete,
                    viewModel.lstFolderSelected.value?.size
                )
            )
            .positiveButton(0, context.getString(R.string.yes)) {
                viewModel.deleteFile()
                it.dismiss()
            }
            .negativeButton(0, context.getString(R.string.no)) {
                it.dismiss()
            }
            .show()
    }

    fun showCreateFolder() {
        val dialog = InputNameDialog.ExtendBuilder()
            .setTitle(resources.getString(R.string.newfolder))
            .onSetPositiveButton(resources.getString(R.string.save)) { _, data ->
                data[InputNameDialog.INPUT_NAME]?.let { name ->
                    viewModel.createFolder(context, name as String)
                }
            }
            .onSetNegativeButton(resources.getString(R.string.cancel)) {}
            .build()
        context.fragmentActivity()?.supportFragmentManager?.let {
            dialog.show(
                it, InputNameDialog::javaClass.name
            )
        }

    }
}