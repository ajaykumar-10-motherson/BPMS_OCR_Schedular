package com.mind.ocr.process;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mind.freeocr.FileInfo;
import com.mind.ocr.configs.OcrConfig;
import com.mind.ocr.configs.OcrConfig.ApplicationConfig;

public class WorkerThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerThread.class);

    private String inputFilePath;
    private ApplicationConfig appConfig;
    private OcrConfig ocrConfig;
    

    public WorkerThread(String inputFilePath, ApplicationConfig appConfig,OcrConfig ocrConfig ) {
        this.inputFilePath = inputFilePath;
        this.appConfig = appConfig;
        this.ocrConfig = ocrConfig;
    }

    @Override
    public void run() {
        processCommand(inputFilePath, appConfig);
    }

    private void processCommand(String inputFilePath, ApplicationConfig appConfig) {
        LOGGER.debug("WorkerThread --> Processing file: " + inputFilePath);

        // Get base folder and other paths from the configuration
        String baseFolder = ocrConfig.getBaseFolder();
        final String FILE_SEPARATOR = File.separator;

        
        // Define folder paths based on the application configuration
        String strSourceFolder = new File(inputFilePath).getParent();
        String strDestinationOCR = baseFolder + FILE_SEPARATOR + appConfig.getName() + FILE_SEPARATOR + appConfig.getOutgoing();
        String strErrorFolder = baseFolder + FILE_SEPARATOR + appConfig.getName() + FILE_SEPARATOR + appConfig.getErrorneous();
        String errorLogFile = baseFolder + FILE_SEPARATOR + appConfig.getName() + FILE_SEPARATOR + appConfig.getLog();
        String strSplitFolder = baseFolder + FILE_SEPARATOR + appConfig.getName() + FILE_SEPARATOR + appConfig.getSplit();
        String strSearchableFileFolder = baseFolder + FILE_SEPARATOR + appConfig.getName() + FILE_SEPARATOR + appConfig.getSearchable();

        // Date formats
        final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        final String DAILY_DATE_FORMAT = "ddMMyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat simpleDateFormatDaily = new SimpleDateFormat(DAILY_DATE_FORMAT);
        Calendar cal = Calendar.getInstance();

        String processDate = "1900-01-01";
        String xmlOrTxt = appConfig.getOutputFormat(); // Use output format from the configuration

        try {
            File file = new File(inputFilePath);
            FileInfo fileInfo = new FileInfo(file, file.getName(), file.length());

            FreeOCR freeOCR = new FreeOCR();

            // Start OCR processing
            freeOCR.startOCROfFiles(
            		strSourceFolder, 
            		strDestinationOCR, 
            		strErrorFolder, 
            		errorLogFile, 
            		xmlOrTxt, 
            		processDate, 
            		simpleDateFormat, 
            		simpleDateFormatDaily, 
            		cal, 
            		fileInfo, 
            		strSplitFolder, 
            		strSearchableFileFolder);

            LOGGER.info("Successfully processed file: " + inputFilePath);
            Thread.sleep(10000); // Simulate processing delay

        } catch (Exception e) {
            LOGGER.error("Error processing file: " + inputFilePath, e);
        }
    }
}
