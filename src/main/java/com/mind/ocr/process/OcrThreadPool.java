package com.mind.ocr.process;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mind.ocr.configs.OcrConfig;
import com.mind.ocr.configs.OcrConfig.ApplicationConfig;

@Component

public class OcrThreadPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(OcrThreadPool.class);

  
    private OcrConfig config;
    
    @Autowired
    public OcrThreadPool(OcrConfig ocrConfig) {
        this.config = ocrConfig;
    }


    public boolean startOCR() {
        try {
            // Read base folder and applications list from configuration
            String baseFolder = config.getBaseFolder();
            List<ApplicationConfig> applications = config.getApplications();

            moveFilesToMainFolder(baseFolder);

            int THREAD_SIZE = Integer.parseInt(System.getProperty("ocr.thread.size", "10"));
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_SIZE);

            for (ApplicationConfig appConfig : applications) {
                String incomingFolderPath = baseFolder + "/" + appConfig.getName() + "/" + appConfig.getIncoming();
                processFiles(incomingFolderPath, THREAD_SIZE, appConfig, executor);
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to finish
            }
        } catch (Exception ex) {
            LOGGER.error("Exception in OCRThreadPool: ", ex);
        }
		return true;
    }

    private void processFiles(String incomingFolderPath, int THREAD_SIZE, ApplicationConfig appConfig, ExecutorService executor) {
        File incomingFolder = new File(incomingFolderPath);
        File[] files = incomingFolder.listFiles();

        if (files == null || files.length == 0) {
           // LOGGER.info("No files found for processing in folder: " + incomingFolderPath);
            return;
        }

        LOGGER.info("Total files received for OCR: " + files.length);

        for (int i = 0; i < Math.min(files.length, THREAD_SIZE); i++) {
            File file = files[i];
            String fileType = getFileExtension(file);

            if ("pdf".equalsIgnoreCase(fileType) || "tif".equalsIgnoreCase(fileType) || "tiff".equalsIgnoreCase(fileType)) {
                String newLocation = incomingFolderPath + "_" + i;
                moveFileToDirectory(file, newLocation);

                String movedFilePath = newLocation + "/" + file.getName();
                Runnable worker = new WorkerThread(movedFilePath,appConfig,config);
                executor.execute(worker);
            }
        }
    }

    private void moveFilesToMainFolder(String baseFolder) {
        File rootFolder = new File(baseFolder);
        String[] directories = rootFolder.list((current, name) -> name.contains("Incoming_Files_"));

        if (directories != null) {
            for (String dirPath : directories) {
                File subDir = new File(rootFolder, dirPath);
                String[] pdfOrTiffFiles = subDir.list((current, name) -> name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".tif") || name.toLowerCase().endsWith(".tiff"));

                if (pdfOrTiffFiles != null) {
                    for (String fileName : pdfOrTiffFiles) {
                        File sourceFile = new File(subDir, fileName);
                        File destFile = new File(baseFolder + "/BPO/Incoming_Files/" + fileName);

                        long fileAgeMinutes = getDateDiff(new Date(sourceFile.lastModified()), new Date(), TimeUnit.MINUTES);
                        if (fileAgeMinutes > 30) {
                            try {
                                FileUtils.moveFile(sourceFile, destFile);
                                LOGGER.info("Moved file: " + fileName);
                            } catch (IOException e) {
                                LOGGER.error("Error moving file: " + fileName, e);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex > 0) ? name.substring(lastIndex + 1) : "";
    }

    private void moveFileToDirectory(File file, String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        file.renameTo(new File(directory, file.getName()));
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
