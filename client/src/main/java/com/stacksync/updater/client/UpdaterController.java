package com.stacksync.updater.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class UpdaterController {
    
    private static final Logger logger = Logger.getLogger(UpdaterController.class.getName());
    private static final String EXTRACTION_FOLDER = "stacksync";
    private static final String DOWNLOAD_FILE = "stacksync.zip";
    private static final String UPDATER_NAME = "UpdaterClient.jar";

    public enum VersionStatus {
        UPDATED ,
        OUTDATED,
        PROBLEM
    }
    
    private Updater updater;

    public UpdaterController(){
        updater = new Updater();
    }
    
    public void start(){
        
        VersionStatus status = updater.checkNewVersionAvailable();
        
        if (status == VersionStatus.UPDATED) {
            logger.debug("StackSync is already in the last version.");
            launchStackSync();
            return;
        } else if (status == VersionStatus.PROBLEM){
            // REPORT problem!!
            logger.error("There is a PROBLEM checking if there is a new version.");
            launchStackSync();
            return;
        }
        
        boolean ok = updater.downloadNewVersion();
        
        if (!ok) {
            return;            
        }
        
        ok = updater.extractDownloadedVersion();
        
        if (!ok) {
            return;            
        }
        
        logger.debug("Killing stacksync to replace files.");
        //stacksync.destroy();
        replaceFiles();
        launchStackSync();
        cleanFiles();
        
    }
    
    private void launchStackSync() {
        //Launch StackSync
        try {
            logger.debug("Launching StackSync.");
            Runtime.getRuntime().exec("java -jar Stacksync.jar");  
        } catch (IOException e) {
            logger.error("Error launching StackSync: ", e);
        }
    }
    
    private void replaceFiles() {
                
        removeLocalFiles();
        
        File dir = new File("./stacksync/");
        File listDir[] = dir.listFiles();
        
        for (int i = 0; i < listDir.length; i++) {
            try {
                
                File dest = new File(listDir[i].getName());
                File source = new File("./stacksync/"+listDir[i].getName());
                
                if (source.isDirectory()) {
                    FileUtils.copyDirectory(source, dest);
                    logger.debug("Copying folder "+source.getAbsolutePath()+" to "+dest.getAbsolutePath());
                }
                else {
                    FileUtils.copyFile(source, dest);
                    logger.debug("Copying file "+source.getAbsolutePath()+" to "+dest.getAbsolutePath());
                }
            } catch (IOException ex) {
                logger.error("Error replacing files: ", ex);
            }
        }
    }
    
    private void removeLocalFiles() {
        File dir = new File("./");
        File listDir[] = dir.listFiles();
        
        for (int i = 0; i < listDir.length; i++) {
            try {
                if(!listDir[i].getName().equals(DOWNLOAD_FILE) 
                        && !listDir[i].getName().equals(EXTRACTION_FOLDER)
                        && !listDir[i].getName().equals(UPDATER_NAME)
                        && !listDir[i].getName().equals("uninstall.exe")){
                    File f = new File(listDir[i].getName());
                    if (f.isDirectory()){
                        FileUtils.deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
                
            } catch (IOException ex) {
                //Logger.getLogger(UpdaterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void cleanFiles() {
        File dir = new File("./");
        File listDir[] = dir.listFiles();
        
        for (int i = 0; i < listDir.length; i++) {
            try {
                if(listDir[i].getName().equals(DOWNLOAD_FILE)
                        || listDir[i].getName().equals(EXTRACTION_FOLDER)) {
                    File f = new File(listDir[i].getName());
                    if (f.isDirectory()){
                        FileUtils.deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                    logger.debug("Deleting "+f.getName());
                }                
            } catch (IOException ex) {
                logger.error("Error cleaning files: ", ex);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        URL log4jResource = UpdaterController.class.getResource("/log4j.xml");
        DOMConfigurator.configure(log4jResource);
        
        logger.debug("Starting updater.");
        UpdaterController updater = new UpdaterController();
        updater.start();
        logger.debug("Everything updated!!!");
    }
 
}
