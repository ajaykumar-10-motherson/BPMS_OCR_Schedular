package com.mind.freeocr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.apache.commons.io.FileDeleteStrategy;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class TIFFUtils {

	public static int pageCountTiff(String inputStream) {
		int numPages = 0;
		SeekableStream seekableStream = null;
		ImageDecoder decoder = null;
		try {
			seekableStream = new FileSeekableStream(inputStream);
			decoder = ImageCodec.createImageDecoder("tiff", seekableStream, null);
			numPages = decoder.getNumPages();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				seekableStream.close();
				decoder = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return numPages;

	}

	public static boolean isBlank(BufferedImage bi) throws Exception {
		long count = 0;
		long total = 0;
		double totalVariance = 0;
		double stdDev = 0;
		int height = bi.getHeight();
		int width = bi.getWidth();
		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(bi, 0, 0, width, height, pixels, 0, width);
		pg.grabPixels();
		for (int j = 0; j < height; j++) {

			for (int i = 0; i < width; i++) {
				count++;
				int pixel = pixels[j * width + i];
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;
				int pixelValue = new Color(red, green, blue, 0).getRGB();
				total += pixelValue;
				double avg = total / count;
				totalVariance += Math.pow(pixelValue - avg, 2);
				stdDev = Math.sqrt(totalVariance / count);
			}
		}

		return (stdDev < 60000);
	}

	public static List<Integer> blankPageList(int numPages, String inputFileLoc) throws Exception {

		SeekableStream seekableStream = new FileSeekableStream(inputFileLoc);
		List<Integer> imageList = new ArrayList<Integer>();
		ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", seekableStream, null);
		for (int j = 1; j <= numPages; j++) {
			PlanarImage op = new NullOpImage(decoder.decodeAsRenderedImage(j - 1), null, null, OpImage.OP_IO_BOUND);

			boolean isTrue = isBlank(op.getAsBufferedImage());
			if (!isTrue) {
				imageList.add(j);

			}
			else {
				imageList.add(0);
			}
		}
		return imageList;
	}

	public static Map<Integer, Integer> getSplitfromToList(String inputFileLoc) throws Exception {
		SeekableStream seekableStream = new FileSeekableStream(inputFileLoc);
		ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", seekableStream, null);
		int numPages = decoder.getNumPages();
		List<Integer> imageList = blankPageList(numPages, inputFileLoc);
		Map<Integer, Integer> balnksplitList = new HashMap<Integer, Integer>();
		try {
			int startPos = 1;
			int i = 0;
			for (Integer val : imageList) {
				i++;
				if (val != 0) {
					balnksplitList.put(startPos, i);
				}
				else {
					startPos = i + 1;
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return balnksplitList;
	}

//	public static void main(String[] args) {
//		// TiffUtils.SplitTiffFile(5,
//		// "D:/017_00004_000037_20140123_153118907.tiff", "D:/");
//		try {
//			// splitBlankFile("D:/153118907.tiff", "D:/153118907_");
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//	}

	public static boolean splitTIFF(String INPUT, String OUTPUT, int fromPage, int toPage) {
		try {
			Document TifftoPDF = new Document();
			RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(INPUT);
			PdfWriter.getInstance(TifftoPDF, new FileOutputStream(OUTPUT));
			TifftoPDF.open();
			int start = 1;
			for (start = fromPage; start <= toPage; start++) {
				Image tempImage = TiffImage.getTiffImage(tiffFile, start);
				tempImage.scalePercent(22f);
				TifftoPDF.add(tempImage);
			}
			TifftoPDF.close();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (DocumentException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// Extract image to count wise
	public static void splitTiff(String tiffFilePath, String outputFilePath, int count) throws IOException {
		SeekableStream seekableStream = null;
		try {
			RenderedImage renderedImage[], page;
			File file = new File(tiffFilePath);
			seekableStream = new FileSeekableStream(file);
			ImageDecoder imageDecoder = ImageCodec.createImageDecoder("tiff", seekableStream, null);
			renderedImage = new RenderedImage[imageDecoder.getNumPages()];
			/* count no. of pages available inside input tiff file */
			for (int i = 0; i < count; i++) {
				renderedImage[i] = imageDecoder.decodeAsRenderedImage(i);
			}
			/* set output folder path */
			String outputFolderName = outputFilePath + "toDelete";
			/*
			 * create file object of output folder and make a directory
			 */
			File fileObjForOPFolder = new File(outputFolderName);
			fileObjForOPFolder.mkdirs();
			/*
			 * extract no. of image available inside the input tiff file
			 */
			BufferedImage image[] = new BufferedImage[count];
			for (int i = 0; i < count; i++) {
				page = imageDecoder.decodeAsRenderedImage(i);
				File fileObj = new File(outputFolderName + "/" + (i + 1) + ".tiff");
				ParameterBlock parameterBlock = new ParameterBlock();
				/* add source of page */
				parameterBlock.addSource(page);
				/* add o/p file path */
				parameterBlock.add(fileObj.toString());
				/* add o/p file type */
				parameterBlock.add("tiff");
				/* create output image using JAI filestore */
				RenderedOp renderedOp = JAI.create("filestore", parameterBlock);
				renderedOp.dispose();
				SeekableStream ss = new FileSeekableStream(outputFolderName + "/" + (i + 1) + ".tiff");
				ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
				PlanarImage pi = new NullOpImage(decoder.decodeAsRenderedImage(0), null, null, OpImage.OP_IO_BOUND);
				image[i] = pi.getAsBufferedImage();
				ss.close();
			}
			TIFFEncodeParam params = new TIFFEncodeParam();
			params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
			OutputStream out = new FileOutputStream(outputFilePath);
			ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
			List<BufferedImage> list = new ArrayList<BufferedImage>(image.length);
			for (int i = 1; i < image.length; i++) {
				list.add(image[i]);
			}
			params.setExtraImages(list.iterator());
			encoder.encode(image[0]);
			list.clear();
			list = null;
			encoder = null;
			out.close();
			out = null;
			renderedImage = null;
			seekableStream.close();
			seekableStream = null;
			imageDecoder = null;
			image = null;
			FileDeleteStrategy.FORCE.delete(new File(outputFolderName));
			outputFolderName = null;
		} catch (Exception ex) {
			System.err.println("Date:" + new Date() + ex);
		} finally {
			System.gc();
		}
	}
}
