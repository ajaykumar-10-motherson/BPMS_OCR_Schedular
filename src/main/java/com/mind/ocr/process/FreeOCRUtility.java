package com.mind.ocr.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mind.freeocr.FileInfo;
import com.mind.freeocr.PDFUtility;
import com.mind.freeocr.TIFFUtils;
import com.mind.freeocr.TiffUtility;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import net.sourceforge.tess4j.TesseractException;

public class FreeOCRUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(FreeOCR.class);
	final static String PDF_FILE_EXT = "pdf";
	final static String TIFF_FILE_EXT = "tiff";
	final static String FILE_SEPERATOR = "//";

	public static void deleteFiles(Set<String> largeFilesList) {
		if (largeFilesList != null && largeFilesList.size() > 0) {
			for (String str : largeFilesList) {
				try {
					File delFile = new File(str);
					if (delFile.exists()) {
						Path fp1 = delFile.toPath();
						Files.delete(fp1);
						// FileDeleteStrategy.FORCE.delete(delFile);
						if (!(delFile.isFile()))
							delFile.deleteOnExit();
					}
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
				}
			}
			largeFilesList.clear();
		}
	}

	public static int getLastPage(int pageCnt) {
		int pageLimit;
		if (pageCnt < 5) {
			pageLimit = pageCnt;
		} else if (pageCnt < 7) {
			pageLimit = 2;
		} else if (pageCnt < 10) {
			pageLimit = 3;
		} else
			pageLimit = 2;
		return pageLimit;
	}

	public static void copyAndDeleteFile(String dstFld, String org_file_before_split, File sourceFile,
			String fileName) {
		try {
			FileUtils.copyFile(sourceFile, new File(dstFld + FILE_SEPERATOR + fileName));
			Path fp = sourceFile.toPath();
			Files.delete(fp);
			deleteFileFromIncomminLocation(org_file_before_split);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
		}
	}

	public static void deleteFileFromIncomminLocation(String fileName) throws IOException {
		if (fileName != null && !fileName.trim().isEmpty()) {
			File fle = new File(fileName);
			Path fp_old = fle.toPath();
			Files.delete(fp_old);
		}
	}

	public static void deleteFile(String fileName) {
		try {
			if (fileName != null && !fileName.trim().isEmpty()) {
				File fle = new File(fileName);
				Path fp_old = fle.toPath();
				Files.delete(fp_old);
			}
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
		}
	}

	public static File doSplitTiffFile(String errFld, File sourceFile, String fileName, int pageCnt)
			throws TesseractException {

		File outFile = null;
		int pageLimit = 0;
		pageLimit = FreeOCRUtility.getLastPage(pageCnt);
		outFile = new File(errFld + FILE_SEPERATOR + fileName);
		try {
			TIFFUtils.splitTiff(sourceFile.getAbsolutePath(), outFile.getAbsolutePath(), pageLimit);
		} catch (IOException ex) {
			throw new TesseractException();
		}
		sourceFile = new File(errFld + FILE_SEPERATOR + outFile.getName());
		return sourceFile;
	}

	public static File doSplitPDFFile(String errFld, File sourceFile, String fileName, int pageCnt) {

		File outFile = null;
		int pageLimit = 0;
		pageLimit = FreeOCRUtility.getLastPage(pageCnt);
		outFile = new File(errFld + FILE_SEPERATOR + fileName);
		PDFUtility.splitPDFFILE(sourceFile.getAbsolutePath(), outFile.getAbsolutePath(), 1, pageLimit);
		sourceFile = new File(errFld + FILE_SEPERATOR + outFile.getName());
		return sourceFile;
	}

	public static PrintWriter getPrintWriter(File file, FileOutputStream os) {
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(os);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return printWriter;
	}

	public static void writeIntoLog(String content, PrintWriter printWriter) {
		printWriter.write(content);
	}

	public static Set<FileInfo> sortFilesBySize(File[] files) {
		Set<FileInfo> filesSet = new HashSet<FileInfo>(300);
		// Added all files from in-comming folder to list collection
		for (int fileNo = 0; fileNo < files.length; fileNo++) {
			File file = files[fileNo];
			FileInfo fileInfo = new FileInfo(file, file.getName(), file.length());
			filesSet.add(fileInfo);
		}
		return filesSet;
	}

	/**
	 * Do split tiff file into pdf.
	 *
	 * @param sourceFile               the source file
	 * @param ocrSplitFolder           the ocr split folder
	 * @param fileNameWithoutExtension the file name without extension
	 * @param fromPage                 the from page
	 * @param toPage                   the to page
	 * @return the file
	 */
	public static File doSplitTiffFileIntoPdf(File sourceFile, String ocrSplitFolder, String fileNameWithoutExtension,
			int fromPage, int toPage) {
		File outFile = null;
		outFile = new File(ocrSplitFolder + FILE_SEPERATOR + fileNameWithoutExtension + ".pdf");
		TiffUtility.splitNTiffToPdf(sourceFile.getAbsolutePath(), outFile.getAbsolutePath(), fromPage, toPage);
		sourceFile = new File(ocrSplitFolder + FILE_SEPERATOR + outFile.getName());
		return sourceFile;
	}

	public static String ocrPDF(String searchableFile) {
		String fileText = "";
		try {
			// Create PdfReader instance.
			PdfReader pdfReader = new PdfReader(searchableFile);

			int pages = pdfReader.getNumberOfPages();

			// Iterate the pdf through pages.
			for (int i = 1; i <= pages; i++) {
				// Extract the page content using PdfTextExtractor.
				fileText = fileText + PdfTextExtractor.getTextFromPage(pdfReader, i)+ " ";
				fileText = StringEscapeUtils.escapeXml10(fileText);

				//System.out.println(fileText);

			}

			// Close the PdfReader.
			pdfReader.close();
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage());
		}
		return fileText;
	}

}
