package com.mind.ocr.Service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mind.ocr.configs.OcrConfig;
import com.mind.ocr.process.OcrThreadPool;


@Service
public class SchedulerService {
	
	@Value("${spring.enable.ocr}")
    private  String enable;
	
	private final OcrConfig ocrConfig;
	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);
	 @Autowired
	    public SchedulerService(OcrConfig ocrConfig) {
	        this.ocrConfig = ocrConfig;
	    }

	private static int runningStatus = 0;
    public static Properties properties = null;
    File errorLogFile = null;
    
    public void ocrScheduler() {
    //	LOGGER.info("Started SchedulerService.ocrScheduler() from service .");
        if (enable.equalsIgnoreCase("1") && runningStatus == 0) {

           runningStatus = 1;
           
          // FreeOCR ocr = new FreeOCR();
           boolean isSucces = false;
           OcrThreadPool ocrThreadPool = new OcrThreadPool(ocrConfig);
           isSucces = ocrThreadPool.startOCR();

           String baseFolder = ocrConfig.getBaseFolder();
           List<OcrConfig.ApplicationConfig> applications = ocrConfig.getApplications();
           for (OcrConfig.ApplicationConfig applicationData : applications) {
               String strSourceFolder = baseFolder + "\\" + applicationData.getName() + "\\" + applicationData.getIncoming();
               File sourceFolder = new File(strSourceFolder);
               File[] listOfFiles = sourceFolder.listFiles();
               for (File file : listOfFiles) {
                  try {
                     Date date2 = new Date(file.lastModified());
                     long diff = getDateDiff(date2, new Date(), TimeUnit.MINUTES);
                     if (diff > 25) {
                        if (diff % 60 == 0) {
                           LOGGER.error("File " + file.getName() + " has some error please check ASAP.");
                        }
                        break;
                     }
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                  }
               }
            }
           if (isSucces) {
              runningStatus = 0;
           }
        }
    }
//    
// // This method runs after the bean initialization
//    @PostConstruct
//    public void initializeFolders() {
//        String baseFolder = ocrConfig.getBaseFolder();
//        List<OcrConfig.ApplicationConfig> applications = ocrConfig.getApplications();
//
//        for (OcrConfig.ApplicationConfig app : applications) {
//            // Create necessary folders if they don't exist
//            createFolder(new File(baseFolder, app.getIncoming()));
//            createFolder(new File(baseFolder, app.getOutgoing()));
//            createFolder(new File(baseFolder, app.getErrorneous()));
//            createFolder(new File(baseFolder, app.getLog()));
//        }
//        System.out.println("All required folders have been checked and created if necessary.");
//    }
//    
// // Utility method to create a folder if it doesn't exist
//    private void createFolder(File folder) {
//        if (!folder.exists()) {
//            boolean isCreated = folder.mkdirs();
//            if (isCreated) {
//                System.out.println("Created folder: " + folder.getAbsolutePath());
//            } else {
//                System.err.println("Failed to create folder: " + folder.getAbsolutePath());
//            }
//        } else {
//            System.out.println("Folder already exists: " + folder.getAbsolutePath());
//        }
//    }
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit)
    {
       long diffInMillies = date2.getTime() - date1.getTime();
       return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
    
    
}
