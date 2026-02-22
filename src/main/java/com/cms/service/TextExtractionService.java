package com.cms.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TextExtractionService {

    /**
     * Extract text from a file based on its type.
     * Supports PDF files — returns extracted text.
     * For images, returns null (images are rendered inline instead).
     */
    public String extractText(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return extractFromPdf(filePath);
        }

        // Images (jpg, jpeg, png) are rendered inline — no text extraction
        return null;
    }

    /**
     * Check if a file is an image (for inline rendering).
     */
    public boolean isImage(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

    /**
     * Check if a file is a PDF.
     */
    public boolean isPdf(String fileName) {
        if (fileName == null) return false;
        return fileName.toLowerCase().endsWith(".pdf");
    }

    private String extractFromPdf(Path filePath) {
        try {
            PDDocument document = Loader.loadPDF(filePath.toFile());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text != null && !text.isBlank() ? text.trim() : "[No readable text found in this PDF]";
        } catch (IOException e) {
            return "[Error extracting text from PDF: " + e.getMessage() + "]";
        }
    }
}
