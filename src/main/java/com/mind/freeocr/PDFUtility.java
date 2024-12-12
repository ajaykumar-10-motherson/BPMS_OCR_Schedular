package com.mind.freeocr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PDFUtility {
	private static final Logger LOGGER = LoggerFactory.getLogger(PDFUtility.class);

	public static boolean splitPDF(String input, String output, int fromPage, int toPage) {
		boolean result = false;
		try {
			String inFile = input.toLowerCase();
			PdfReader reader = new PdfReader(inFile);
			Document document = new Document(reader.getPageSizeWithRotation(1));
			PdfCopy writer = new PdfCopy(document, new FileOutputStream(output));
			document.open();
			PdfImportedPage page;

			while (fromPage <= toPage) {
				page = writer.getImportedPage(reader, fromPage);
				writer.addPage(page);
				fromPage++;
			}
			document.close();
			writer.close();
			reader.close();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public static boolean splitPDFFILE(String input, String output, int fromPage, int toPage) {
		boolean result = false;
		FileOutputStream fileOutputStream = null;
		Document document = null;
		PdfCopy pdfCopy = null;
		PdfReader reader = null;
		try {
			String inFile = input.toLowerCase();
			reader = new PdfReader(inFile);
			fileOutputStream = new FileOutputStream(output);
			document = new Document(reader.getPageSizeWithRotation(1));
			pdfCopy = new PdfCopy(document, fileOutputStream);
			document.open();
			while (fromPage <= toPage) {
				PdfImportedPage page = pdfCopy.getImportedPage(reader, fromPage);
				pdfCopy.addPage(page);
				++fromPage;
			}
			result = true;
		} catch (Exception e) {
			result = false;

			if (document != null) document.close();
			try {
				if (fileOutputStream != null) fileOutputStream.close();
				fileOutputStream = null;
				document = null;
				if (pdfCopy != null) {
					pdfCopy.close();
				}
			} catch (IOException ex) {
				LOGGER.error(ex.getMessage());
			}

		} finally {
			if (document != null) document.close();
			try {
				reader.close();
				if (fileOutputStream != null) fileOutputStream.close();
				fileOutputStream = null;
				document = null;
				pdfCopy = null;
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
			if (pdfCopy != null) pdfCopy.close();
		}

		return result;
	}

	public static boolean oldSplitPDF(InputStream inputStream, OutputStream outputStream, int fromPage, int toPage) {
		Document document = new Document();
		boolean result = false;
		try {
			PdfReader inputPDF = new PdfReader(inputStream);
			int totalPages = inputPDF.getNumberOfPages();
			// make fromPage equals to toPage if it is greater
			if (fromPage > toPage) {
				fromPage = toPage;
			}
			if (toPage > totalPages) {
				toPage = totalPages;
			}
			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			// Holds the PDF data
			PdfImportedPage page;
			while (fromPage <= toPage) {
				document.newPage();
				page = writer.getImportedPage(inputPDF, fromPage);
				cb.addTemplate(page, 0, 0);
				fromPage++;
			}
			outputStream.flush();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen()) document.close();
			try {
				if (outputStream != null) outputStream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return result;
	}

	public static int pageCountPDF(String inputStream) {
		Document document = new Document();
		FileInputStream fileInputStream = null;
		int totalPages = 0;
		PdfReader inputPDF = null;
		try {
			fileInputStream = new FileInputStream(inputStream);
			inputPDF = new PdfReader(fileInputStream);
			totalPages = inputPDF.getNumberOfPages();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen()) {
				document.close();
			}

			if (inputPDF != null) {
				inputPDF.close();
			}

			try {
				if (fileInputStream != null) fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return totalPages;
	}

	public static boolean makeDestinationSplitPath(String folderPath) {
		boolean mkDir = false;
		try {
			File isDir = new File(folderPath);
			if (!isDir.exists()) {
				if (!isDir.isDirectory()) mkDir = isDir.mkdirs();
			}
			else
				mkDir = isDir.exists();

		} catch (Exception ex) {
			System.out.println("makeDestinationSplitPath Error:" + ex.getMessage());
		}
		return mkDir;
	}

	public static ArrayList<Integer> getBlankPageNoList(String input, String output, int fromPage, int toPage, int pagePerFile, int totalFiles,
	    int splitFileSize) {
		boolean result = false;
		int count = 0;
		int pageSplitFrom = 0;
		int pageSplitTo = 0;
		String SPLIT_FILE_NAME = "";
		String SPLITFILE = "";
		ArrayList<Integer> blankPageList = new ArrayList<Integer>();
		String tempDirectory = output + File.separator + "TEMPSPLIT";
		try {
			boolean mkDir = PDFUtility.makeDestinationSplitPath(tempDirectory);
			if (mkDir) {
				for (int i = 0; i < totalFiles; i++) {
					count = i + 1;
					SPLIT_FILE_NAME = "Split_" + count + ".pdf";
					SPLITFILE = tempDirectory + File.separator + SPLIT_FILE_NAME;
					if (i == 0) {
						pageSplitFrom = fromPage;
						pageSplitTo = (fromPage + pagePerFile) - 1;
					}
					else {
						pageSplitFrom = pageSplitTo + 1;
						pageSplitTo = (pageSplitFrom + pagePerFile) - 1;
					}

					result = PDFUtility.splitPDF(input, SPLITFILE, pageSplitFrom, pageSplitTo);
					if (result) {
						File file = new File(SPLITFILE);
						long fileSize = file.length() / 1024;
						// System.out.println(SPLITFILE+" : -->"+fileSize);
						if (fileSize <= splitFileSize) {
							blankPageList.add(0);
						}
						else {
							blankPageList.add(count);
						}
					}
				}
				File delDir = new File(tempDirectory);
				if (delDir.exists()) deleteDir(delDir);
			}
			// System.out.println("blankPageList-->" + blankPageList+" : blankPageList.size()-->" +
			// blankPageList.size()+ " : totalFiles-->" + totalFiles);

		} catch (Exception e) {
			System.out.println("PDFUtility : getBlankPageNoList -->");
			e.printStackTrace();
			result = false;
		}


		return blankPageList;
	}

	public static boolean splitPDFWithBlankPage(String input, String output, int fromPage, int toPage, int pagePerFile, int totalFiles,
	    int splitFileSize, int splitFileCount) {
		boolean result = false;
		String SPLIT_FILE_NAME = "";
		String SPLITFILE = "";
		String splitFileName = "";
		ArrayList<Integer> blankPageList = new ArrayList<Integer>();

		try {
			blankPageList = getBlankPageNoList(input, output, fromPage, toPage, pagePerFile, totalFiles, splitFileSize);

			if (blankPageList.size() > 0) {
				int getPageFrom = 0;
				int getPageTo = 0;
				int tempValue = 0;
				splitFileName = input.substring(input.lastIndexOf("\\"), input.lastIndexOf("."));

				for (int i = 0; i < blankPageList.size(); i++) {
					SPLIT_FILE_NAME = splitFileName + "_" + splitFileCount + "_split.pdf";
					SPLITFILE = output + File.separator + SPLIT_FILE_NAME;
					tempValue = blankPageList.get(i);
					if (tempValue == 0) {
						if (getPageFrom > 0) {
							// System.out.println(getPageFrom + " : " + getPageTo);
							result = PDFUtility.splitPDF(input, SPLITFILE, getPageFrom, getPageTo);
							if (result) splitFileCount++;
						}
						getPageFrom = 0;
						getPageTo = 0;
						continue;
					}
					else {
						if (getPageFrom == 0) {
							getPageFrom = tempValue;
							getPageTo = tempValue;
						}
						else {
							getPageTo = tempValue;
						}
					}
					if (tempValue == totalFiles) {
						// System.out.println(getPageFrom + " : " + getPageTo);
						result = PDFUtility.splitPDF(input, SPLITFILE, getPageFrom, getPageTo);
						if (result) splitFileCount++;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}

//	public static void main(String args[]) {
//		String input = "D:\\Desktop\\APO\\Scan Docs\\thumbnail\\Case_10.pdf";
//		String output = "D:\\Desktop\\APO\\Scan Docs\\thumbnail";
//		int fromPage = 1;
//		int toPage = 1;
//		int pagePerFile = 1;
//		int totalFiles = pageCountPDF(input);
//		int splitFileSize = 15;
//		int splitFileCount = 1;
//
//		splitPDFWithBlankPage(input, output, fromPage, toPage, pagePerFile, totalFiles, splitFileSize, splitFileCount);
//	}

}
