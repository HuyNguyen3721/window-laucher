package com.ezteam.baseproject.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.*

object FileSaveManager {

    fun saveImageBitmap(
        context: Context,
        folderName: String,
        bitmap: Bitmap,
        fileName: String = ""
    ): String? {
        val filename = fileName.ifEmpty { "Img_${System.currentTimeMillis()}.png" }
        var fos: OutputStream?
        var imageUri: Uri?
        val newFile = File(initFolderParent(folderName), filename)
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (!newFile.exists()) {
                    newFile.createNewFile()
                }
                fos = try {
                    FileOutputStream(newFile)
                } catch (ex: Exception) {
                    FileOutputStream(newFile.path)
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos?.flush()
                fos?.close()
                MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    newFile.absolutePath,
                    newFile.name,
                    newFile.name
                )
                imageUri = Uri.fromFile(newFile)
                context.sendBroadcast(
                    Intent(
                        "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                        imageUri
                    )
                )
            } else {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + folderName
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                context.contentResolver.also { resolver ->
                    imageUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let {
                        resolver.openOutputStream(it)
                    }
                }
                fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                fos?.close()
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                imageUri?.let { context.contentResolver.update(it, contentValues, null, null) }
            }
            return newFile.path
        } catch (ex: Exception) {
        }
        return null
    }

    private fun initFolderParent(name: String): String? {
        val mediaDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            name
        )
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED && !mediaDirectory.isDirectory) {
            mediaDirectory.mkdirs()
        }
        return mediaDirectory.path
    }

}