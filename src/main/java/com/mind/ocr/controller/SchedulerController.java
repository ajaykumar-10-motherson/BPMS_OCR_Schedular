package com.mind.ocr.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.mind.ocr.Service.SchedulerService;



@RestController
public class SchedulerController {

    @Resource
    SchedulerService schedulerService;    

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerController.class);

    @Scheduled(cron = "${scheduler.cron.every20sec1}")
    public void printCurrentTime() {
    	//LOGGER.info("Start OCRSchuler main method printCurrentTime");
    	
        schedulerService.ocrScheduler();
    }
}
