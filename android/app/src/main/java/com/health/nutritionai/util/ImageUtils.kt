package com.health.nutritionai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun compressImage(imageFile: File, maxSizeKB: Int = 1024): ByteArray {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        var quality = 90
        var streamLength: Int
        val bmpStream = ByteArrayOutputStream()

        do {
            bmpStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bmpStream)
            streamLength = bmpStream.toByteArray().size
            quality -= 5
        } while (streamLength > maxSizeKB * 1024 && quality > 0)

        return bmpStream.toByteArray()
    }

    fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

