    package com.example.miniproject.ml

    import android.content.Context
    import android.graphics.Bitmap
    import android.graphics.ImageDecoder
    import android.net.Uri
    import android.os.Build
    import android.provider.MediaStore
    import android.util.Log
    import com.example.miniproject.data.api.ApiClient
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import okhttp3.MediaType.Companion.toMediaTypeOrNull
    import okhttp3.MultipartBody
    import okhttp3.RequestBody.Companion.asRequestBody
    import java.io.File
    import java.io.FileOutputStream

    class WaterQualityClassifier(private val context: Context) {

        companion object {
            private const val TAG = "WaterQuality"
        }

        fun close() {
            // API-based classifier, nothing to close
            Log.d(TAG, "close() called (no-op, API based)")
        }

        data class WaterQualityResult(
            val quality: String,
            val confidence: Float,
            val recommendation: String,
            val probabilities: Map<String, Double>? = null,
            val rawPrediction: String? = null
        )

        suspend fun analyzeWater(imageUri: Uri): WaterQualityResult? = withContext(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            try {
                Log.d(TAG, "==================================================")
                Log.d(TAG, "🌊 analyzeWater START")
                Log.d(TAG, "uri=$imageUri")
                Log.d(TAG, "scheme=${imageUri.scheme} host=${imageUri.host} path=${imageUri.path}")
                Log.d(TAG, "==================================================")

                // 1) URI -> Bitmap
                Log.d(TAG, "📥 Converting URI -> Bitmap...")
                val bitmap = uriToBitmap(imageUri)
                Log.d(TAG, "✅ Bitmap created: ${bitmap.width}x${bitmap.height}")

                // 2) Bitmap -> Temp file
                Log.d(TAG, "💾 Saving bitmap -> temp file...")
                val file = bitmapToTempFile(bitmap)
                Log.d(TAG, "✅ Temp file: ${file.absolutePath}")
                Log.d(TAG, "   exists=${file.exists()} size=${file.length()} bytes")

                // 3) Build multipart
                Log.d(TAG, "📦 Building multipart request...")
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, requestBody)
                Log.d(TAG, "✅ Multipart created: name=image filename=${file.name}")

                // 4) Call API
                Log.d(TAG, "📤 Calling Flask API /predict ...")
                val resp = ApiClient.waterQualityService.predictWaterQuality(part)

                // cleanup file
                val deleted = try { file.delete() } catch (_: Exception) { false }
                Log.d(TAG, "🧹 Temp file deleted=$deleted")

                // 5) Handle response
                Log.d(TAG, "📨 Response received in ${System.currentTimeMillis() - start} ms")
                Log.d(TAG, "HTTP code=${resp.code()} message=${resp.message()} success=${resp.isSuccessful}")

                if (!resp.isSuccessful) {
                    val err = try { resp.errorBody()?.string() } catch (e: Exception) { "errorBody read failed: ${e.message}" }
                    Log.e(TAG, "❌ HTTP ERROR: code=${resp.code()} msg=${resp.message()}")
                    Log.e(TAG, "❌ errorBody=$err")
                    return@withContext null
                }

                val body = resp.body()
                if (body == null) {
                    Log.e(TAG, "❌ Response body is null")
                    return@withContext null
                }

                Log.d(TAG, "✅ API body.success=${body.success}")
                Log.d(TAG, "   quality=${body.quality} confidence=${body.confidence}")
                Log.d(TAG, "   rawPrediction=${body.rawPrediction}")
                Log.d(TAG, "   probabilities=${body.probabilities}")
                Log.d(TAG, "   error=${body.error}")

                if (body.success != true) {
                    Log.e(TAG, "❌ API returned success=false, error=${body.error}")
                    return@withContext null
                }

                val quality = body.quality ?: "Keruh"
                val confidence = (body.confidence ?: 0.0).toFloat()

                val result = WaterQualityResult(
                    quality = quality,
                    confidence = confidence,
                    recommendation = recommendationFor(quality),
                    probabilities = body.probabilities,
                    rawPrediction = body.rawPrediction
                )

                Log.d(TAG, "🎯 Result: quality=${result.quality} conf=${result.confidence}")
                Log.d(TAG, "✅ analyzeWater END (${System.currentTimeMillis() - start} ms)")
                Log.d(TAG, "==================================================")

                result

            } catch (e: Exception) {
                Log.e(TAG, "❌ analyzeWater exception: ${e.message}")
                Log.e(TAG, Log.getStackTraceString(e))
                null
            }
        }

        private fun uriToBitmap(uri: Uri): Bitmap {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Log.d(TAG, "uriToBitmap: using ImageDecoder (API 28+)")
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                Log.d(TAG, "uriToBitmap: using MediaStore.getBitmap (<API 28)")
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }

        private fun bitmapToTempFile(bitmap: Bitmap): File {
            val file = File(context.cacheDir, "temp_water_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                Log.d(TAG, "bitmap.compress ok=$ok")
            }
            return file
        }

        private fun recommendationFor(quality: String): String {
            return when (quality) {
                "Jernih" -> "Air berkualitas baik. Cocok untuk budidaya dan irigasi."
                "Keruh" -> "Air keruh. Disarankan filter atau pengendapan."
                "Tercemar" -> "Air tercemar. Butuh treatment sebelum digunakan."
                else -> "Perlu pengecekan lanjutan."
            }
        }

        // Ini yang dipakai HomeFragment kamu
        fun getRecommendedKeywords(quality: String): List<String> {
            val keywords = when (quality) {
                "Jernih" -> listOf(
                    "pupuk", "benih", "bibit", "pakan", "nutrisi",
                    "vitamin", "probiotik", "kolam", "sawah"
                )
                "Keruh" -> listOf(
                    "filter", "penyaring", "penjernih", "pompa", "aerator",
                    "sedimentasi", "kapas filter", "zeolit", "pasir silika",
                    "tawas", "koagulan"
                )
                "Tercemar" -> listOf(
                    "karbon aktif", "zeolit", "disinfektan", "klorin", "uv",
                    "filter", "biofilter", "bakteri pengurai", "em4", "aerator"
                )
                else -> listOf("pupuk", "benih", "bibit", "pakan")
            }

            Log.d(TAG, "🔎 getRecommendedKeywords quality=$quality -> $keywords")
            return keywords
        }
    }
