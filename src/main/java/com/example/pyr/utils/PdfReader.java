package com.example.pyr.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PdfReader {
	
	@Autowired
	private Tokenizer tokenizer;

	public List<String> readPdf(String filePath, long tokensPerPage) {
		try (InputStream resourceInputStream = new ClassPathResource(filePath).getInputStream();
			 PDDocument pdDocument = PDDocument.load(resourceInputStream)) {

			PDFTextStripper pdfTextStripper = new PDFTextStripper();
			String pdfText = pdfTextStripper.getText(pdDocument);
			return tokenizer.divide(pdfText, tokensPerPage);

		} catch (Exception e) {
			throwException(e);
		}
        return null;
    }

	public List<String> readPdfPerPage(String filePath) {
		List<String> pages = new ArrayList<>();

		try (InputStream resourceInputStream = new ClassPathResource(filePath).getInputStream();
			 PDDocument pdDocument = PDDocument.load(resourceInputStream)) {
			
			PDFTextStripper pdfStripper = new PDFTextStripper();
			
			int numberOfPages = pdDocument.getNumberOfPages();
			for (int page = 1; page <= numberOfPages; page++) {
				pdfStripper.setStartPage(page);
				pdfStripper.setEndPage(page);
				String parsedText = pdfStripper.getText(pdDocument);
				pages.add(parsedText);
			}
		} catch (IOException e) {
			throwException(e);
		}
		return pages;
	}

	private static void throwException(Exception e) {
		throw new RuntimeException("Error al leer el archivo PDF", e);
	}
	
}
