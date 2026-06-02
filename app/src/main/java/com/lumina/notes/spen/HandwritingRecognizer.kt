package com.lumina.notes.spen

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import com.lumina.notes.data.ink.Stroke as InkStroke

/**
 * On-device handwriting → text using **ML Kit Digital Ink Recognition**
 * (a real Google library). Converts the note's S Pen strokes to an [Ink] and
 * asks ML Kit for the best text candidate. The language model is downloaded
 * once (needs network the first time); afterwards it works offline.
 */
class HandwritingRecognizer(languageTag: String = "it") {

    private val model: DigitalInkRecognitionModel?
    private val recognizer: DigitalInkRecognizer?
    private val remoteModelManager = RemoteModelManager.getInstance()

    init {
        val id = runCatching {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
                ?: DigitalInkRecognitionModelIdentifier.fromLanguageTag("en")
        }.getOrNull()
        model = id?.let { DigitalInkRecognitionModel.builder(it).build() }
        recognizer = model?.let {
            DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(it).build())
        }
    }

    /**
     * Recognizes [strokes], invoking [onResult] with the best text or null.
     * Downloads the model first if needed.
     */
    fun recognize(strokes: List<InkStroke>, onResult: (String?) -> Unit) {
        val rec = recognizer
        val mdl = model
        if (rec == null || mdl == null || strokes.isEmpty()) {
            onResult(null)
            return
        }
        ensureModel(mdl) { ready ->
            if (!ready) { onResult(null); return@ensureModel }
            rec.recognize(toInk(strokes))
                .addOnSuccessListener { result ->
                    onResult(result.candidates.firstOrNull()?.text)
                }
                .addOnFailureListener {
                    Log.w(TAG, "recognition failed: ${it.message}")
                    onResult(null)
                }
        }
    }

    private fun ensureModel(mdl: DigitalInkRecognitionModel, onReady: (Boolean) -> Unit) {
        remoteModelManager.isModelDownloaded(mdl)
            .addOnSuccessListener { downloaded ->
                if (downloaded == true) {
                    onReady(true)
                } else {
                    remoteModelManager.download(mdl, DownloadConditions.Builder().build())
                        .addOnSuccessListener { onReady(true) }
                        .addOnFailureListener {
                            Log.w(TAG, "model download failed: ${it.message}")
                            onReady(false)
                        }
                }
            }
            .addOnFailureListener { onReady(false) }
    }

    private fun toInk(strokes: List<InkStroke>): Ink {
        val inkBuilder = Ink.builder()
        var t = 0L
        for (s in strokes) {
            val sb = Ink.Stroke.builder()
            for (p in s.points) {
                sb.addPoint(Ink.Point.create(p.x, p.y, t))
                t += 16L
            }
            inkBuilder.addStroke(sb.build())
        }
        return inkBuilder.build()
    }

    fun close() {
        runCatching { recognizer?.close() }
    }

    private companion object {
        const val TAG = "HandwritingRecognizer"
    }
}
