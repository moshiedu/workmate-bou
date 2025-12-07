package com.moshitech.workmate.feature.imagestudio.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

class EditRepository(private val context: Context) {

    suspend fun loadBitmap(uri: Uri, maxWidth: Int = 2048, maxHeight: Int = 2048): Bitmap? = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            // Decode bitmap with inSampleSize set
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    suspend fun applyTransformations(
        original: Bitmap,
        brightness: Float, // -1.0 to 1.0 (0 is default)
        contrast: Float,   // 0.0 to 2.0 (1 is default)
        saturation: Float, // 0.0 to 2.0 (1 is default)
        filterMatrix: FloatArray? = null,
        rotationAngle: Float = 0f,
        hue: Float = 0f,           // -180 to 180
        temperature: Float = 0f,   // -1 to 1
        tint: Float = 0f           // -1 to 1
    ): Bitmap = withContext(Dispatchers.Default) {
        // Apply rotation first if needed
        val rotatedBitmap = if (rotationAngle != 0f) {
            val matrix = android.graphics.Matrix().apply {
                postRotate(rotationAngle)
            }
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        } else {
            original
        }
        
        val bitmap = Bitmap.createBitmap(rotatedBitmap.width, rotatedBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val matrix = ColorMatrix()

        // 1. Adjustment Matrix
        val adjustmentMatrix = ColorMatrix()
        
        // Brightness & Contrast
        val contrastScale = contrast
        val brightnessOffset = brightness * 255f
        
        val cm = floatArrayOf(
            contrastScale, 0f, 0f, 0f, brightnessOffset,
            0f, contrastScale, 0f, 0f, brightnessOffset,
            0f, 0f, contrastScale, 0f, brightnessOffset,
            0f, 0f, 0f, 1f, 0f
        )
        adjustmentMatrix.set(cm)
        
        // Saturation
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation)
        adjustmentMatrix.postConcat(saturationMatrix)

        matrix.postConcat(adjustmentMatrix)
        
        // 2. Hue adjustment
        if (hue != 0f) {
            val hueMatrix = createHueMatrix(hue)
            matrix.postConcat(hueMatrix)
        }
        
        // 3. Temperature adjustment (warm/cool)
        if (temperature != 0f) {
            val tempMatrix = createTemperatureMatrix(temperature)
            matrix.postConcat(tempMatrix)
        }
        
        // 4. Tint adjustment (green/magenta)
        if (tint != 0f) {
            val tintMatrix = createTintMatrix(tint)
            matrix.postConcat(tintMatrix)
        }

        // 5. Filter Matrix (if applied)
        if (filterMatrix != null) {
            val fm = ColorMatrix(filterMatrix)
            matrix.postConcat(fm)
        }

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(rotatedBitmap, 0f, 0f, paint)

        bitmap
    }
    
    private fun createHueMatrix(hue: Float): ColorMatrix {
        val angle = hue * Math.PI / 180.0
        val cosA = kotlin.math.cos(angle).toFloat()
        val sinA = kotlin.math.sin(angle).toFloat()
        
        val lumR = 0.213f
        val lumG = 0.715f
        val lumB = 0.072f
        
        return ColorMatrix(floatArrayOf(
            lumR + cosA * (1 - lumR) + sinA * (-lumR), lumG + cosA * (-lumG) + sinA * (-lumG), lumB + cosA * (-lumB) + sinA * (1 - lumB), 0f, 0f,
            lumR + cosA * (-lumR) + sinA * (0.143f), lumG + cosA * (1 - lumG) + sinA * (0.140f), lumB + cosA * (-lumB) + sinA * (-0.283f), 0f, 0f,
            lumR + cosA * (-lumR) + sinA * (-(1 - lumR)), lumG + cosA * (-lumG) + sinA * (lumG), lumB + cosA * (1 - lumB) + sinA * (lumB), 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
    }
    
    private fun createTemperatureMatrix(temperature: Float): ColorMatrix {
        val tempValue = temperature * 50f
        return ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, tempValue,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, -tempValue,
            0f, 0f, 0f, 1f, 0f
        ))
    }
    
    private fun createTintMatrix(tint: Float): ColorMatrix {
        val tintValue = tint * 50f
        return ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, tintValue,
            0f, 0f, 1f, 0f, -tintValue * 0.5f,
            0f, 0f, 0f, 1f, 0f
        ))
    }
    
    suspend fun saveBitmap(bitmap: Bitmap, filename: String, format: Bitmap.CompressFormat, quality: Int): Uri? = withContext(Dispatchers.IO) {
        try {
            val extension = if (format == Bitmap.CompressFormat.PNG) "png" else "jpg"
            val fullFilename = if (filename.endsWith(".jpg") || filename.endsWith(".png")) filename else "$filename.$extension"
            val mimeType = if (format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
            
            // Use MediaStore to save to gallery
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fullFilename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Workmate")
            }
            
            val resolver = context.contentResolver
            val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            imageUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(format, quality, outputStream)
                }
                uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
