/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.io.database.plugin;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.CaseDatabaseWriter;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

/**
 * This class is the manager of the case database formats. Detects the class anotated as CaseDatabaseFormat
 * annotations.
 *
 * @author ibermejo
 * @see org.openmarkov.core.io.format.annotation.FormatType
 */
public class CaseDatabaseManager {
    /**
     * The list of case database reader plugins detected in the project
     */
    private HashMap<String, Class<?>> readerPlugins;
    /**
     * The list of case database writer plugins detected in the project
     */
    private HashMap<String, Class<?>> writerPlugins;
    
    /**
     * Gets a FormatManager instance
     */
    public CaseDatabaseManager() {
        this.readerPlugins = new LinkedHashMap<>();
        this.writerPlugins = new LinkedHashMap<>();
        CaseDatabaseManager.findAllReaderPlugins().forEach(readerPlugin -> {
            CaseDatabaseFormat lAnnotation = readerPlugin.getAnnotation(CaseDatabaseFormat.class);
            readerPlugins.put(lAnnotation.extension(), readerPlugin);
        });
        CaseDatabaseManager.findAllWriterPlugins().forEach(writerPlugin -> {
            CaseDatabaseFormat lAnnotation = writerPlugin.getAnnotation(CaseDatabaseFormat.class);
            writerPlugins.put(lAnnotation.extension(), writerPlugin);
        });
    }
    
    private static @NotNull Stream<Class<CaseDatabaseReader>> findAllReaderPlugins() {
        return PluginSearch.init().annotatedWith(CaseDatabaseFormat.class)
                           .childrenOf(CaseDatabaseReader.class)
                           .stream();
    }
    
    private static @NotNull Stream<Class<CaseDatabaseWriter>> findAllWriterPlugins() {
        return PluginSearch.init().annotatedWith(CaseDatabaseFormat.class)
                .childrenOf(CaseDatabaseWriter.class)
                .stream();
    }
    
    /**
     * Gets the writer with the extension
     *
     * @param extension the extension required
     * @return a CaseDatabaseWriter object
     */
    public CaseDatabaseWriter getWriter(String extension) {
        try {
            return (CaseDatabaseWriter) writerPlugins.get(extension).getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException ignored) {
            return null;
        }
    }
    
    /**
     * @param extension the extension required
     * @return a CaseDatabaseReader object
     */
    public CaseDatabaseReader getReader(String extension) {
        try {
            return (CaseDatabaseReader) readerPlugins.get(extension).getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }
    
    /**
     * Returns a HashMap whose keys are extensions accepted by the readers and
     * whose values are descriptions of the file format read by the reader
     *
     * @return a HashMap whose keys are extensions accepted by the readers and
     * whose values are descriptions of the file format read by the reader
     */
    public HashMap<String, String> getAllReaders() {
        HashMap<String, String> readersInfo = new HashMap<>();
        for (String extension : readerPlugins.keySet()) {
            String description = readerPlugins.get(extension).getAnnotation(CaseDatabaseFormat.class).name();
            readersInfo.put(extension, description);
        }
        
        return readersInfo;
    }
    
    /**
     * Returns a HashMap whose keys are extensions accepted by the writers and
     * whose values are descriptions of the file format written by the writer
     *
     * @return a HashMap whose keys are extensions accepted by the writers and
     * whose values are descriptions of the file format written by the writer
     */
    public HashMap<String, String> getAllWriters() {
        HashMap<String, String> writersInfo = new HashMap<>();
        for (String extension : writerPlugins.keySet()) {
            String description = writerPlugins.get(extension).getAnnotation(CaseDatabaseFormat.class).name();
            writersInfo.put(extension, description);
        }
        
        return writersInfo;
        
    }
    
}
