package cl.duoc.electromechanics.device

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhotoTarget(val uri: Uri, val path: String)

object CameraFileHelper {
    fun createPhotoTarget(context: Context, prefix: String): PhotoTarget {
        val dir = File(context.filesDir, "evidencias").apply { if (!exists()) mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "${prefix}_$stamp.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return PhotoTarget(uri, file.absolutePath)
    }

    fun isValidPhoto(path: String): Boolean {
        val file = File(path)
        val ok = file.exists() && file.length() > 0
        if (!ok) file.delete()
        return ok
    }
}
