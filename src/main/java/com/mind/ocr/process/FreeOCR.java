package com.mind.ocr.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mind.freeocr.FileInfo;
import com.mind.freeocr.PDFUtility;
import com.mind.freeocr.TIFFUtils;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;

import net.sourceforge.tess4j.TesseractException;

public class FreeOCR {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FreeOCR.class);
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = null;
	String destinationOCR = "";
	String sourceFolder = "";
	final String FILE_SEPERATOR = "//";
	final String XML_HEADER = "<form:Documents xmlns:form=\"http://www.abbyy.com/FlexiCapture/Schemas/Export/FormData.xsd\" xmlns:addData=\"http://www.abbyy.com/FlexiCapture/Schemas/Export/AdditionalFormData.xsd\"><_FullText:_FullText xmlns:_FullText=\"http://www.abbyy.com/FlexiCapture/Schemas/Export/FullText.xsd\"><_Document_Section_1><_FullText addData:SuspiciousSymbols=\"000\">";
	final String XML_FOOTER = "</_FullText></_Document_Section_1></_FullText:_FullText></form:Documents>";
	final String PDF_FILE_EXT = "pdf";
	final String TIFF_FILE_EXT = "tiff";
	final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	final String DAILY_DATE_FORMAT = "ddMMyy";
	static final int MAXIMUM_OCR_PAGE_COUNT = 2;

	
	public static Properties properties = null;
    static File propFile=null;
    static String replaceCharacters = "";
    static String[] replaceCharactersArray = null; 
    static List<String> replaceCharactersList = new ArrayList<>();
	
	public String startOCROfFiles(String srcFld, String dstFld, String errFld, String errLogFile, String xmlOrTxt,
			String processDate, SimpleDateFormat simpleDateFormat, SimpleDateFormat simpleDateFormatDaily, Calendar cal,
			FileInfo fileInfo, String strSplitFolder, String strSearchableFileFolder) {
			LOGGER.info("Started :: FreeOCR.startOCROfFiles()");
		
			File logFile = null;
		    File errFileLog = null;
		    File logForEachFile = null;
		    File xmlFileLog = null;
		    String org_file_before_split = null;
		    File sourceFile = null;
		    String fileName = null;
		    String actualFileName = null;
		    String ocrData = null;
		    PrintWriter logForEachFilePW = null;
		    PrintWriter logFilePrintWriter = null;
		    PrintWriter xmlFileLogPW = null;
		    PrintWriter errFileLogPW = null;
		    FileOutputStream logForEachFileOS = null;
		    FileOutputStream logFileOS = null;
		    FileOutputStream xmlFileLoOS = null;
		    FileOutputStream errFileLogOS = null;
		    String fileNameWithoutExtension = null;
		    File splitFile = null;
		    String searchableFile = null;
		    String fileText = "";
		    try {
		      if (!isValidFile(fileInfo))
		        return processDate; 
		      sourceFile = fileInfo.getFile();
		      fileName = sourceFile.getName();
		      int pos = fileName.lastIndexOf(".");
		      processDate = simpleDateFormat.format(cal.getTime());
		      String dlyFolderName = simpleDateFormatDaily.format(cal.getTime());
		      String logFolderPath = String.valueOf(errLogFile) + "//" + dlyFolderName;
		      File logFolder = new File(logFolderPath);
		      if (!logFolder.exists())
		        logFolder.mkdir(); 
		      actualFileName = fileName.substring(0, pos);
		      logForEachFile = new File(String.valueOf(logFolderPath) + "//" + actualFileName + ".log");
		      logForEachFileOS = new FileOutputStream(logForEachFile);
		      logForEachFilePW = FreeOCRUtility.getPrintWriter(logForEachFile, logForEachFileOS);
		      FreeOCRUtility.writeIntoLog("Start Time : " + processDate, logForEachFilePW);
		      int pageCnt = 0;
		      if (fileInfo.getFile().getName().substring(fileInfo.getFile().getName().lastIndexOf('.') + 1).toLowerCase()
		        .equalsIgnoreCase("pdf")) {
		        int splitPageSize = 0;
		        this.date = new Date();
		        System.out.println("Started---" + fileInfo.getFile().getName() + ":: at::" + this.dateFormat.format(this.date));
		        pageCnt = PDFUtility.pageCountPDF(sourceFile.getAbsolutePath());
		        if (pageCnt >= 2) {
		          splitPageSize = 2;
		        } else {
		          splitPageSize = 1;
		        } 
		        if (fileInfo.getFileSize() > 300000L || pageCnt > 2) {
		          splitFile = FreeOCRUtility.doSplitPDFFile(strSplitFolder, sourceFile, fileName, splitPageSize);
		          fileText = FreeOCRUtility.ocrPDF(splitFile.getAbsolutePath());
		          fileText = fileText.replaceAll("[^a-zA-Z0-9\\s\\t ,.-:_()/@%!|]", " ");
		          if (fileText.length() < 4)
		            searchableFile = createSearchablePdf(strSplitFolder, strSearchableFileFolder, 
		                splitFile.getName()); 
		        } else {
		          fileText = FreeOCRUtility.ocrPDF(sourceFile.getAbsolutePath());
		          fileText = fileText.replaceAll("[^a-zA-Z0-9\\s\\t ,.-:_()/@%!|]", " ");
		          if (fileText.length() < 4)
		            searchableFile = createSearchablePdf(srcFld, strSearchableFileFolder, sourceFile.getName()); 
		        } 
		      } else if (fileInfo.getFile().getName().substring(fileInfo.getFile().getName().lastIndexOf('.') + 1)
		        .toLowerCase().equalsIgnoreCase("tiff") || 
		        fileInfo.getFile().getName().substring(fileInfo.getFile().getName().lastIndexOf('.') + 1)
		        .toLowerCase().equalsIgnoreCase("tif")) {
		        pageCnt = TIFFUtils.pageCountTiff(sourceFile.getAbsolutePath());
		        if (fileInfo.getFileSize() > 300000L || pageCnt > 2) {
		          int splitPageSize = 0;
		          if (pageCnt >= 2) {
		            splitPageSize = 2;
		          } else {
		            splitPageSize = 1;
		          } 
		          fileNameWithoutExtension = sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf('.'));
		          splitFile = FreeOCRUtility.doSplitTiffFileIntoPdf(sourceFile, strSplitFolder, 
		              fileNameWithoutExtension, 1, splitPageSize);
		        } else {
		          fileNameWithoutExtension = sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf('.'));
		          splitFile = FreeOCRUtility.doSplitTiffFileIntoPdf(sourceFile, strSplitFolder, 
		              fileNameWithoutExtension, 1, 1);
		        } 
		        searchableFile = createSearchablePdf(strSplitFolder, strSearchableFileFolder, splitFile.getName());
		        fileText = FreeOCRUtility.ocrPDF(splitFile.getAbsolutePath());
		        fileText = fileText.replaceAll("[^a-zA-Z0-9\\s\\t ,.-:_()/@%!|]", " ");
		        if (fileText.length() < 4)
		          searchableFile = createSearchablePdf(strSplitFolder, strSearchableFileFolder, splitFile.getName()); 
		      } 
		      if (pageCnt != 0) {
		        if (fileText.length() < 4) {
		          File sourceSearchableFile = new File(searchableFile);
		          fileText = FreeOCRUtility.ocrPDF(sourceSearchableFile.getAbsolutePath());
		          fileText = fileText.replaceAll("[^a-zA-Z0-9\\s\\t ,.-:_()/@%!|]", " ");
		        } 
		      } else {
		        FileUtils.copyFile(sourceFile, new File(String.valueOf(dstFld) + "//" + fileName));
		        FileDeleteStrategy.FORCE.delete(sourceFile);
		        throw new TesseractException();
		      } 
		      ocrData = fileText;
		      if (ocrData.length() < 1) {
		        fileText = "";
		        throw new TesseractException();
		      } 
		      xmlFileLog = new File(String.valueOf(dstFld) + "//" + actualFileName + "." + xmlOrTxt);
		      xmlFileLoOS = new FileOutputStream(xmlFileLog);
		      xmlFileLogPW = FreeOCRUtility.getPrintWriter(xmlFileLog, xmlFileLoOS);
		      logFile = new File(String.valueOf(dstFld) + "//" + actualFileName + ".log");
		      logFileOS = new FileOutputStream(logFile);
		      logFilePrintWriter = FreeOCRUtility.getPrintWriter(logFile, logFileOS);
		      System.out.println("logFile::::" + logFile.getAbsolutePath());
		      FreeOCRUtility.writeIntoLog("Start Time : " + processDate, logFilePrintWriter);
		      FreeOCRUtility.copyAndDeleteFile(dstFld, sourceFile.getAbsolutePath(), sourceFile, fileName);
		      if (splitFile != null && splitFile.getName() != null)
		        FreeOCRUtility.deleteFile(splitFile.getAbsolutePath()); 
		      FreeOCRUtility.deleteFile(searchableFile);
		      FreeOCRUtility.writeIntoLog("<form:Documents xmlns:form=\"http://www.abbyy.com/FlexiCapture/Schemas/Export/FormData.xsd\" xmlns:addData=\"http://www.abbyy.com/FlexiCapture/Schemas/Export/AdditionalFormData.xsd\"><_FullText:_FullText xmlns:_FullText=\"http://www.abbyy.com/FlexiCapture/Schemas/Export/FullText.xsd\"><_Document_Section_1><_FullText addData:SuspiciousSymbols=\"000\">", xmlFileLogPW);
		      FreeOCRUtility.writeIntoLog(StringEscapeUtils.escapeXml10(ocrData), xmlFileLogPW);
		      FreeOCRUtility.writeIntoLog("</_FullText></_Document_Section_1></_FullText:_FullText></form:Documents>", xmlFileLogPW);
		      FreeOCRUtility.writeIntoLog(String.valueOf(System.getProperty("line.separator")) + "Message : File exported Successfully.", 
		          logFilePrintWriter);
		      FreeOCRUtility.writeIntoLog(String.valueOf(System.getProperty("line.separator")) + "End Time : " + processDate, 
		          logFilePrintWriter);
		      FreeOCRUtility.writeIntoLog(String.valueOf(System.getProperty("line.separator")) + "End Time : " + processDate, 
		          logForEachFilePW);
		      closedStream(logForEachFilePW, logFilePrintWriter, xmlFileLogPW, errFileLogPW, logForEachFileOS, logFileOS, 
		          xmlFileLoOS, errFileLogOS);
		    } catch (Exception ex) {
		      ex.printStackTrace();
		      LOGGER.error("FreeOCR-->startOCROfFiles Exception::", ex);
		      FreeOCRUtility.writeIntoLog(String.valueOf(System.getProperty("line.separator")) + "Error In file due to:" + ex, 
		          logForEachFilePW);
		      FreeOCRUtility.writeIntoLog(String.valueOf(System.getProperty("line.separator")) + "End Time : " + processDate, 
		          logForEachFilePW);
		      errFileLog = new File(String.valueOf(dstFld) + "//" + actualFileName + ".log");
		      try {
		        errFileLogOS = new FileOutputStream(errFileLog);
		      } catch (FileNotFoundException exp) {
		        LOGGER.error("FreeOCR-->startOCROfFiles Exception::", exp);
		      } 
		      errFileLogPW = FreeOCRUtility.getPrintWriter(errFileLog, errFileLogOS);
		      FreeOCRUtility.writeIntoLog("Start Time : " + processDate, errFileLogPW);
		      FreeOCRUtility.writeIntoLog(String.valueOf(System.getProperty("line.separator")) + "End Time : " + processDate, 
		          errFileLogPW);
		      FreeOCRUtility.writeIntoLog(String.valueOf(System.getProperty("line.separator")) + "Message : Error in OCR.", errFileLogPW);
		      FreeOCRUtility.copyAndDeleteFile(dstFld, org_file_before_split, sourceFile, fileName);
		      if (splitFile != null && splitFile.getName() != null)
		        FreeOCRUtility.deleteFile(splitFile.getAbsolutePath()); 
		      FreeOCRUtility.deleteFile(searchableFile);
		      closedStream(logForEachFilePW, logFilePrintWriter, xmlFileLogPW, errFileLogPW, logForEachFileOS, logFileOS, 
		          xmlFileLoOS, errFileLogOS);
		    } 
		    return processDate;
		    
	}
	
	private void closedStream(PrintWriter logForEachFilePW, PrintWriter logFilePrintWriter, PrintWriter xmlFileLogPW,
			PrintWriter errFileLogPW, FileOutputStream logForEachFileOS, FileOutputStream logFileOS,
			FileOutputStream xmlFileLoOS, FileOutputStream errFileLogOS) {
		if (logForEachFilePW != null)
			logForEachFilePW.close();
		if (logFilePrintWriter != null)
			logFilePrintWriter.close();
		if (xmlFileLogPW != null)
			xmlFileLogPW.close();
		if (errFileLogPW != null)
			errFileLogPW.close();
		try {
			if (logForEachFileOS != null)
				logForEachFileOS.close();
			if (logFileOS != null)
				logFileOS.close();
			if (xmlFileLoOS != null)
				xmlFileLoOS.close();
			if (errFileLogOS != null)
				errFileLogOS.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
	public static String createSearchablePdf(String sourceFolder, String destinationFolder, String fileName) {
		LOGGER.error("*New  createSearchablePdf(): Starts ");
		
		 	PowerShellResponse response = null;
		    String searchableFile = "";
		    PowerShell session = null;
		    try {
		      searchableFile = String.valueOf(destinationFolder) + "//" + fileName;
		      String path = "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";
		      String command = "ocrmypdf --tesseract-pagesegmode 1  --optimize 0 --output-type pdf -r --rotate-pages-threshold 50 --fast-web-view 0  " + sourceFolder + "//" + fileName + " " + searchableFile;
		      LOGGER.error("*New  command : "+command);
		      Runtime runtime = Runtime.getRuntime();
		      try {
		        Process proc = runtime.exec(String.valueOf(path) + " " + command);
		        proc.waitFor();
		      } catch (Exception exception) {}
		    } catch (Exception e) {
		      LOGGER.info("Error:" + e);
		    } 
		    return searchableFile;
		
	}
	private boolean isValidFile(FileInfo fileInfo) {
		return fileInfo.getFile().getName().substring(fileInfo.getFile().getName().lastIndexOf('.') + 1).toLowerCase()
				.equalsIgnoreCase(PDF_FILE_EXT)
				|| fileInfo.getFile().getName().substring(fileInfo.getFile().getName().lastIndexOf('.') + 1)
						.toLowerCase().equalsIgnoreCase("tif")
				|| fileInfo.getFile().getName().substring(fileInfo.getFile().getName().lastIndexOf('.') + 1)
						.toLowerCase().equalsIgnoreCase(TIFF_FILE_EXT);
	}
}
