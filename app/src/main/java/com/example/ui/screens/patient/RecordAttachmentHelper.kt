package com.example.ui.screens.patient

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class SavedAttachment(
    val localPath: String,
    val displayName: String,
    val fileType: String // "IMAGE" or "PDF"
)

object RecordAttachmentHelper {

    fun createCameraOutputUri(context: Context): Pair<Uri, File> {
        val dir = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val file = File(dir, "scan_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return uri to file
    }

    fun copyFileToAttachments(
        context: Context,
        sourceFile: File,
        preferredName: String = sourceFile.name
    ): SavedAttachment? {
        return try {
            val destDir = File(context.filesDir, "medical_attachments").apply { mkdirs() }
            val destFile = File(destDir, "rx_${System.currentTimeMillis()}_${sanitizeName(preferredName)}")
            sourceFile.copyTo(destFile, overwrite = true)
            SavedAttachment(
                localPath = destFile.absolutePath,
                displayName = preferredName,
                fileType = "IMAGE"
            )
        } catch (_: Exception) {
            null
        }
    }

    fun copyUriToAttachments(context: Context, uri: Uri): SavedAttachment? {
        return try {
            val mimeType = context.contentResolver.getType(uri).orEmpty()
            val rawName = queryDisplayName(context, uri)
            val isPdf = mimeType.contains("pdf", ignoreCase = true) ||
                rawName.endsWith(".pdf", ignoreCase = true)
            val fallbackExt = if (isPdf) "pdf" else "jpg"
            val displayName = rawName.ifBlank {
                "doc_${System.currentTimeMillis()}.$fallbackExt"
            }
            val destDir = File(context.filesDir, "medical_attachments").apply { mkdirs() }
            val destFile = File(destDir, "upload_${System.currentTimeMillis()}_${sanitizeName(displayName)}")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            SavedAttachment(
                localPath = destFile.absolutePath,
                displayName = displayName,
                fileType = if (isPdf) "PDF" else "IMAGE"
            )
        } catch (_: Exception) {
            null
        }
    }

    fun decodeSampledImageBitmap(path: String, maxDim: Int = 1200): ImageBitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sampleSize = 1
            while ((bounds.outWidth / sampleSize) > maxDim || (bounds.outHeight / sampleSize) > maxDim) {
                sampleSize *= 2
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    fun openAttachmentExternally(context: Context, path: String, fileType: String): Boolean {
        val file = File(path)
        if (!file.exists()) return false
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mime = if (fileType.equals("PDF", ignoreCase = true)) "application/pdf" else "image/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open Medical Document"))
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else ""
            } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun sanitizeName(name: String): String {
        return name.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(48)
    }
}
