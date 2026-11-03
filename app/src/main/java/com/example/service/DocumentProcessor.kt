package com.example.service

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class DocumentProcessor {

    suspend fun extractText(context: Context, uriString: String, fileType: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open file")
        
        try {
            if (fileType.contains("pdf", ignoreCase = true)) {
                extractFromPdf(inputStream)
            } else if (fileType.contains("text", ignoreCase = true)) {
                inputStream.bufferedReader().use { it.readText() }
            } else {
                throw Exception("Unsupported file format for AI processing. Supported: PDF, TXT")
            }
        } finally {
            inputStream.close()
        }
    }

    private fun extractFromPdf(inputStream: InputStream): String {
        var document: PDDocument? = null
        return try {
            document = PDDocument.load(inputStream)
            val pdfStripper = PDFTextStripper()
            val text = pdfStripper.getText(document)
            if (text.isNullOrBlank()) {
                throw Exception("The PDF contains no readable text or is image-based.")
            }
            text
        } finally {
            document?.close()
        }
    }
}
