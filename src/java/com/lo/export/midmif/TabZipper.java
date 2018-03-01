/*
 */
package com.lo.export.midmif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author jphoude
 */
public class TabZipper {
    private static final Logger LOGGER = Logger.getLogger(TabZipper.class.getName());
    
    private File inputTab;
    private String layerName;
    
    public TabZipper(File inputTab, String layerName) {
        this.inputTab = inputTab;
        this.layerName = layerName;
    }
    
    private String getBaseName(String fileName) {
        int index = fileName.indexOf('.');
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }
    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.'));
    }
    
    private File[] getTabDependencies() {
        String name = inputTab.getName();
        final String tabBase = getBaseName(name);
        File directory = inputTab.getParentFile();
        
        File[] depFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String baseName = getBaseName(name);

                return baseName != null && baseName.equalsIgnoreCase(tabBase);
            }
        });
            
        return depFiles;
    }
    
    private void addFileToZip(ZipOutputStream zos, File file) throws IOException {
        String fileName = layerName + getExtension(file.getName());
        ZipEntry entry = new ZipEntry(fileName);
        zos.putNextEntry(entry);
        
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            IOUtils.copy(fis, zos);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        
        
        zos.closeEntry();
    }
    
    public void writeZippedOutput(OutputStream os) throws FileNotFoundException, IOException {
        ZipOutputStream zos = null;
        File[] tabDependencies = getTabDependencies();
        try {
            zos = new ZipOutputStream(os);

            for (File file : tabDependencies) {
                addFileToZip(zos, file);
            }
        } finally {
            if (zos != null) {
                zos.finish();
                zos.close();
            }
        }
        
        for (File file : tabDependencies) {
            file.delete();
        }
    }
}
