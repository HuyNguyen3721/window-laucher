package com.ezteam.windowslauncher.extension

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import java.io.File

fun File.isPdf(): Boolean {
    return path.endsWith(".pdf")
}

fun File.isVideo(): Boolean {
    return path.endsWith(".mp4") || path.endsWith(".3gp")
}

fun File.isImage(): Boolean {
    return path.endsWith(".jpg") ||
            path.endsWith(".png") ||
            path.endsWith(".gif") ||
            path.endsWith(".jpeg")
}

fun File.getThumbnail(): Bitmap? {
    return ThumbnailUtils.createVideoThumbnail(
        path,
        MediaStore.Images.Thumbnails.MINI_KIND
    )
}