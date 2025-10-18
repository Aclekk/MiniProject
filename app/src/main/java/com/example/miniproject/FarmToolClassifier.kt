package com.example.miniproject.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.IOException

/**
 * 🤖 FarmToolClassifier - ML Helper untuk Visual Search Alat Tani
 *
 * Karena kita belum punya trained model, ini menggunakan simple color & pattern detection
 * sebagai POC (Proof of Concept). Nanti bisa diganti dengan TensorFlow Lite model asli.
 */
class FarmToolClassifier(private val context: Context) {

    companion object {
        private const val TAG = "FarmToolClassifier"

        // 🎯 Label mapping untuk dummy products
        private val TOOL_KEYWORDS = mapOf(
            "cangkul" to listOf("cangkul", "hoe", "digging"),
            "pupuk" to listOf("pupuk", "fertilizer", "kompos", "organik"),
            "benih" to listOf("benih", "seed", "bibit", "padi", "jagung"),
            "traktor" to listOf("traktor", "tractor", "mesin", "kendaraan"),
            "rotavator" to listOf("rotavator", "rotary", "penggembu r"),
            "sekop" to listOf("sekop", "shovel", "spade"),
            "selang" to listOf("selang", "hose", "irigasi", "pipa"),
            "arit" to listOf("arit", "sickle", "sabit"),
            "sprayer" to listOf("sprayer", "semprot", "spray"),
            "pemotong" to listOf("potong", "mower", "mesin potong", "rumput"),
            "pestisida" to listOf("pestisida", "pesticide", "obat", "hama")
        )
    }

    /**
     * 🔍 Classify image dari URI
     * Returns: List of predicted product names (sorted by confidence)
     */
    fun classifyImage(imageUri: Uri): List<String> {
        return try {
            val bitmap = uriToBitmap(imageUri)
            if (bitmap != null) {
                classifyBitmap(bitmap)
            } else {
                Log.e(TAG, "Failed to convert URI to Bitmap")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error classifying image: ${e.message}")
            emptyList()
        }
    }

    /**
     * 🔍 Classify bitmap langsung (ULTRA RELAXED VERSION)
     */
    private fun classifyBitmap(bitmap: Bitmap): List<String> {
        Log.d(TAG, "🎨 Analyzing image: ${bitmap.width}x${bitmap.height}")

        // 🎨 Enhanced color-based detection
        val dominantColors = analyzeDominantColors(bitmap)
        val predictions = mutableListOf<Pair<String, Float>>()

        // Log color distribution untuk debugging
        Log.d(TAG, "🎨 Color distribution: $dominantColors")

        // 🚫 ONLY reject obvious skin tones (selfies)
        val skinTone = isSkinTone(dominantColors)
        Log.d(TAG, "🧑 Skin tone detected: $skinTone")

        if (skinTone) {
            Log.d(TAG, "❌ REJECTED - Detected skin tone (likely selfie/face)")
            return emptyList()
        }

        Log.d(TAG, "✅ PASSED validation - Proceeding with classification...")

        // 🟢 Green dominance → fertilizer/seeds
        val greenRatio = dominantColors["green"]!!
        if (greenRatio > 0.05f) {
            predictions.add("pupuk" to (0.7f + greenRatio * 2f))
            predictions.add("benih" to (0.6f + greenRatio * 1.5f))
            Log.d(TAG, "🟢 Green detected: $greenRatio → Added pupuk/benih")
        }

        // ⚫ Black dominance (metal tools)
        val blackRatio = dominantColors["black"]!!
        if (blackRatio > 0.05f) {
            predictions.add("sekop" to (0.9f + blackRatio * 2f))
            predictions.add("cangkul" to (0.85f + blackRatio * 1.5f))
            predictions.add("arit" to (0.8f + blackRatio))
            Log.d(TAG, "⚫ Black detected: $blackRatio → Added sekop/cangkul/arit")
        }

        // 🟤 Brown/gray → metal hand tools
        val brownRatio = dominantColors["brown"]!!
        val grayRatio = dominantColors["gray"]!!
        if (brownRatio > 0.03f || grayRatio > 0.05f) {
            predictions.add("cangkul" to (0.8f + brownRatio * 2f + grayRatio))
            predictions.add("sekop" to (0.75f + brownRatio * 1.5f + grayRatio))
            predictions.add("arit" to (0.7f + brownRatio + grayRatio * 0.8f))
            Log.d(TAG, "🟤 Brown/Gray detected: $brownRatio/$grayRatio → Added hand tools")
        }

        // 🔴 Red/orange → heavy machinery
        val redRatio = dominantColors["red"]!!
        if (redRatio > 0.05f && redRatio < 0.5f) {
            predictions.add("traktor" to (0.8f + redRatio))
            predictions.add("rotavator" to (0.7f + redRatio * 0.8f))
            predictions.add("pemotong" to (0.6f + redRatio * 0.6f))
            Log.d(TAG, "🔴 Red detected: $redRatio → Added machinery")
        }

        // 🔵 Blue → water-related equipment
        val blueRatio = dominantColors["blue"]!!
        if (blueRatio > 0.05f) {
            predictions.add("selang" to (0.7f + blueRatio * 1.5f))
            predictions.add("sprayer" to (0.65f + blueRatio * 1.2f))
            Log.d(TAG, "🔵 Blue detected: $blueRatio → Added water tools")
        }

        // 🎯 FALLBACK: If no predictions, suggest common hand tools ONLY
        if (predictions.isEmpty()) {
            Log.d(TAG, "⚠️ No color matches, using fallback suggestions")
            predictions.add("cangkul" to 0.4f)
            predictions.add("sekop" to 0.4f)
            // Removed pupuk from fallback - only suggest tools with no color info
        }

        // 📊 Sort by confidence and return top 3
        val topPredictions = predictions
            .sortedByDescending { it.second }
            .distinctBy { it.first }
            .take(3)
            .map { it.first }

        Log.d(TAG, "🎯 FINAL Top Predictions: $topPredictions")
        return topPredictions
    }

    /**
     * 🧑 Detect if image contains skin tones (reject selfies/people photos)
     */
    private fun isSkinTone(colors: Map<String, Float>): Boolean {
        val redRatio = colors["red"]!!
        val greenRatio = colors["green"]!!
        val blackRatio = colors["black"]!!
        val grayRatio = colors["gray"]!!

        // Very specific skin tone: high red, very low black/gray, low green
        val isSkin = (redRatio > 0.35f && blackRatio < 0.05f &&
                grayRatio < 0.10f && greenRatio < 0.12f)

        Log.d(TAG, "🧑 Skin check - red:$redRatio black:$blackRatio gray:$grayRatio green:$greenRatio → $isSkin")
        return isSkin
    }

    /**
     * 🎨 Analyze dominant colors in bitmap
     */
    private fun analyzeDominantColors(bitmap: Bitmap): Map<String, Float> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)

        var redCount = 0
        var greenCount = 0
        var blueCount = 0
        var brownCount = 0
        var grayCount = 0
        var blackCount = 0
        var totalPixels = 0

        for (x in 0 until scaledBitmap.width) {
            for (y in 0 until scaledBitmap.height) {
                val pixel = scaledBitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff

                totalPixels++

                // Color classification
                when {
                    g > r * 1.2 && g > b * 1.2 -> greenCount++ // Green
                    r > g * 1.2 && r > b * 1.2 -> redCount++   // Red
                    b > r * 1.2 && b > g * 1.2 -> blueCount++  // Blue
                    r in 80..140 && g in 60..100 && b in 40..80 -> brownCount++ // Brown
                    Math.abs(r - g) < 30 && Math.abs(g - b) < 30 && r < 100 -> grayCount++ // Gray
                    r < 50 && g < 50 && b < 50 -> blackCount++ // Black
                }
            }
        }

        scaledBitmap.recycle()

        return mapOf(
            "red" to (redCount.toFloat() / totalPixels),
            "green" to (greenCount.toFloat() / totalPixels),
            "blue" to (blueCount.toFloat() / totalPixels),
            "brown" to (brownCount.toFloat() / totalPixels),
            "gray" to (grayCount.toFloat() / totalPixels),
            "black" to (blackCount.toFloat() / totalPixels)
        )
    }

    /**
     * 🖼️ Convert URI to Bitmap
     */
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.e(TAG, "Error loading image: ${e.message}")
            null
        }
    }

    /**
     * 🔍 Get product keywords untuk filtering
     */
    fun getProductKeywords(toolName: String): List<String> {
        return TOOL_KEYWORDS[toolName.lowercase()] ?: emptyList()
    }
}