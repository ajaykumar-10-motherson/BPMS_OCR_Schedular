package com.mind.freeocr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.FileChannelRandomAccessSource;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

public class TiffUtility {
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(TiffUtility.class);

	public static int pageCountTiff(String filePhysicalPath) {
		int numPages = 0;
		RandomAccessFile aFile = null;
		try {
			aFile = new RandomAccessFile(filePhysicalPath, "r");
			FileChannel inChannel = aFile.getChannel();
			FileChannelRandomAccessSource fcra = new FileChannelRandomAccessSource(inChannel);
			RandomAccessFileOrArray rafa = new RandomAccessFileOrArray(fcra);
			numPages = TiffImage.getNumberOfPages(rafa);
		} catch (Exception e) {
			logger.error("Exception pageCountTiff()->>", e);
		} finally {
			try {
				if (aFile != null) {
					aFile.close();
				}
			} catch (Exception e) {
				logger.error("Exception pageCountTiff()->>", e);
			}
		}
		return numPages;

	}


	public static int splitNTiffToPdf(String inputFilePhysicalPath, String outPutFilePhysicalPath, int fromPage,
			int toPage) {
		int numPages = 0;
		RandomAccessFile aFile = null;
		Document document = null;
		try {
			aFile = new RandomAccessFile(inputFilePhysicalPath, "r");
			FileChannel inChannel = aFile.getChannel();
			FileChannelRandomAccessSource fcra = new FileChannelRandomAccessSource(inChannel);
			document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(outPutFilePhysicalPath));
			document.open();
			RandomAccessFileOrArray rafa = new RandomAccessFileOrArray(fcra);
			int pages = TiffImage.getNumberOfPages(rafa);
			Image image;
			int start = 1;
			if (toPage > pages) {
				toPage = pages;
			}
			for (start = fromPage; start <= toPage; start++) {
				image = TiffImage.getTiffImage(rafa, start);
				Rectangle pageSize = new Rectangle(image.getWidth(), image.getHeight());
				document.setPageSize(pageSize);
				document.newPage();
				document.add(image);
			}
		} catch (Exception e) {
			logger.error("Exception splitNTiffToPdf()->>", e);
		} finally {
			try {
				if (document != null) {
					document.close();
				}
				if (aFile != null) {
					aFile.close();
				}
			} catch (IOException e) {
				logger.error("Exception splitNTiffToPdf()->>", e);
			}
		}
		return numPages;

	}
}
