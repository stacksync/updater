package com.stacksync.updater.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import com.stacksync.updater.client.UpdaterController.VersionStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.Logger;

public class Updater {
    
    private static final Logger logger = Logger.getLogger(Updater.class.getName());
    
    // TODO Change SERVER_URL!!!
    private static final String VERSION_URL = "http://SERVER_URL/api/version";
    private static final String FILE_URL = "http://SERVER_URL/api/files";
    private static final String DOWNLOAD_FILE = "stacksync.zip";
    
    public VersionStatus checkNewVersionAvailable(){
       
        Float currentVersion = getCurrentVersion();
        logger.debug("Current version: "+currentVersion);
        Float newVersion = getNewVersion();
        logger.debug("Remote version: "+newVersion);
        
        if (newVersion == null){
            return VersionStatus.PROBLEM;
        }
        
        return newVersion > currentVersion ? VersionStatus.OUTDATED : VersionStatus.UPDATED;
    }
    
    private Float getCurrentVersion() {
        Float version = null;
        try {
            JarFile jar = new JarFile("Stacksync.jar");
            Manifest mf = jar.getManifest();
            String versionStr = mf.getMainAttributes().getValue("Version");
            version = Float.parseFloat(versionStr);
        } catch (IOException ex) {
            logger.error("Error obtaining current version: ", ex);
        }
        return version;
    }
    
    public Float getNewVersion() {
        
        Float newVersion = null;
        
        try {
            Client client = Client.create();
            WebResource resource = client.resource(VERSION_URL);
            logger.debug("Getting new version from URL: "+VERSION_URL);
            ClientResponse response = resource.accept("application/json").get(ClientResponse.class);

            if (response.getStatus() == 200) {
                String output = response.getEntity(String.class);
                newVersion = getVersionFromJSON(output);
            }
        } catch (Exception e) {
            logger.error("Error getting new version: "+e);
        }
        
        return newVersion;
    }
    
    private Float getVersionFromJSON(String jsonVersion) {
        
        JsonElement element = new JsonParser().parse(jsonVersion);
        
        JsonObject object = element.getAsJsonObject();
        Float version = object.get("version").getAsFloat();
        
        return version;
    }

    public boolean downloadNewVersion(){
        boolean correctDownload = false;
        
        try {
            Client client = Client.create();
            WebResource resource = client.resource(FILE_URL);
            logger.debug("Downloading new version from: "+FILE_URL);
            ClientResponse response = resource.get(ClientResponse.class);

            if (response.getStatus() == 200) {
                logger.debug("Client downloaded correctly.");
                File downloadedFile = new File(DOWNLOAD_FILE);
                writeFile(response.getEntityInputStream(), downloadedFile);
                
                String md5 = response.getHeaders().get("md5").get(0);
                logger.debug("MD5 from the request: "+md5);

                correctDownload = checkMD5(md5);
            }
        } catch (Exception ex) {
            logger.error("Error downloading new version: "+ex);
        }
        
        return correctDownload;
    }

    private boolean checkMD5(String md5){
        boolean correct = false;
        
        try {
            String calculatedMD5 = createMD5();
            if(calculatedMD5.equals(md5)) {
                logger.debug("Version downloaded correctly.");
                correct = true;
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.error(ex);
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        return correct;
    }

    private String createMD5() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream is = new FileInputStream(DOWNLOAD_FILE);
        try {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                md.update(buf, 0, n);
            }
        } finally {
            is.close();
        }
        
        byte[] digest = md.digest();
        
        String result = "";

        for (int i=0; i < digest.length; i++) {
            result += Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        
        logger.debug("MD5 calculated from the downloaded file: "+result);
        return result;
    }

    public boolean extractDownloadedVersion() {
        
        boolean success = true;
        
        try {
            String zipFile = DOWNLOAD_FILE;
            int BUFFER = 2048;
            File file = new File(zipFile);

            ZipFile zip = new ZipFile(file);
            String newPath = zipFile.substring(0, zipFile.length() - 4);

            new File(newPath).mkdir();
            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(newPath, currentEntry);
                //destFile = new File(newPath, destFile.getName());
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (!entry.isDirectory())
                {
                    BufferedInputStream is = new BufferedInputStream(zip
                    .getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                    BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            
        } catch (ZipException ex) {
            success = false;
            logger.error("Error extracting zip: "+ex);
        } catch (IOException ex) {
            success = false;
            logger.error("Error extracting zip: "+ex);
        }
        
        return success;
    }
    
    private void writeFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);

        int read;
        byte[] bytes = new byte[4096];

        while ((read = is.read(bytes)) != -1) {
            fos.write(bytes, 0, read);
        }

        is.close();
        fos.close();
    }
}
