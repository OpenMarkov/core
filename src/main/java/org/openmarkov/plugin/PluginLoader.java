/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.plugin;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginException;
import org.openmarkov.plugin.service.PluginLoaderIF;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipFile;

import static org.openmarkov.plugin.Filter.filter;

/*
 * Development Environment        :  Eclipse
 * Name of the File               :  PluginLoader.java
 * Creation/Modification History  :
 * <p>
 * jvelez     15/09/2011 19:08:10      Created.
 * Gigaesfera CO.
 * (#)PluginLoader.java 1.0    15/09/2011 19:08:10
 */

/**
 * This class is an implementation of {@link PluginLoaderIF} interface.
 *
 * @author jvelez
 * @version 1.0 - jvelez: Initial implementation.
 * <br>1.1 - jrico:
 * <br>- Plugin loading is now done just once per program execution.
 * <br>- Functions are no longer recursive, but iterative.
 * <br>- {@link PluginLoader#loadAllPlugins(FilterIF)} no longer throws {@link PluginException} (It was never thrown)
 * <br>- Added nullability annotations.
 */
public class PluginLoader implements PluginLoaderIF {
    
    private static final ClassLoader CLASS_LOADER = ClassLoader.getSystemClassLoader();
    private static final String CLASS_EXTENSION = ".class";
    private static final char PACKAGE_SEPARATOR = '.';
    
    private static boolean CLASSES_ARE_LOADED = false;
    private static final ArrayList<Class<?>> LOADED_CLASSES = new ArrayList<>(2000);
    
    /**
     * Returns a plugin from the system environment.
     *
     * @param name The qualified name of the plugin class.
     * @return a plugin from the system environment.
     */
    @Override
    public final @NotNull Class<?> loadPlugin(String name) throws PluginException {
        try {
            return PluginLoader.CLASS_LOADER.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new PluginException("Unable to load plugin [" + name + ']', e);
        }
    }
    
    
    //TODO: In the previous version, this method used to have a clause such as
    // 'throws PluginException', although it never threw said exception, in this
    // version this has been removed to match the real behaviour, but every place
    // where 'loadAllPlugins' were called are still using a 'try catch' to caught
    // the old PluginException, this could and should be removed in every call to
    // said method.
    
    /**
     * Returns all plugins from the system environment.
     *
     * @param filter the plugins filter to select plugins.
     * @return all plugins from the system environment.
     */
    @Override
    public final @NotNull List<Class<?>> loadAllPlugins(@Nullable FilterIF filter) {
        PluginLoader.ensureClassesAreLoaded();
        if (filter == null) {
            filter = filter().end();
        }
        return PluginLoader.LOADED_CLASSES
                .stream()
                .filter(filter::checkPlugin)
                .toList();
    }
    
    /**
     * Loads all the classes of the classpath in {@link PluginLoader#LOADED_CLASSES}, only if this hasn't been done
     * before.
     * <p>
     * This method is synchronized to avoid multiple threads trying to load the classes at the same time, which in turn
     * would create duplicates in {@link PluginLoader#LOADED_CLASSES}.
     * Although by default, the load is done at the
     * beginning of the program in a single-threaded enviroment, so this acts as prevention.
     */
    private synchronized static void ensureClassesAreLoaded() {
        if (PluginLoader.CLASSES_ARE_LOADED)
            return;
        ScanResult scan = new ClassGraph().scan();
        var classPaths = scan.getClasspathURLs().stream().map(URL::getFile).toList();
        scan.close();
        for (var classpath : classPaths) {
            for (var classQualifiedName : PluginLoader.getClassesQualifiedNames(classpath)) {
                if (!classQualifiedName.startsWith("org.openmarkov"))
                    continue;
                try {
                    Class<?> loadedClass = PluginLoader.CLASS_LOADER.loadClass(classQualifiedName);
                    PluginLoader.LOADED_CLASSES.add(loadedClass);
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            }
        }
        PluginLoader.CLASSES_ARE_LOADED = true;
    }
    
    /**
     * Returns all plugins from the system environment.
     *
     * @return all plugins from the system environment.
     */
    @Override public final @NotNull List<Class<?>> loadAllPlugins() {
        FilterIF filter = filter().end();
        return this.loadAllPlugins(filter);
    }
    
    /**
     * Returns all classes names.
     *
     * @param classpath the path where searches starts.
     * @return a list of classes names.
     */
    private static @NotNull List<String> getClassesQualifiedNames(@NotNull String classpath) {
        File classpathFile = new File(classpath);
        if (classpathFile.isDirectory()) {
            return PluginLoader.getClassesNamesFromDirectory(classpathFile);
        }
        return PluginLoader.getClassesNamesOfJar(classpathFile);
    }
    
    /**
     * Returns all classes names of a jar file.
     *
     * @param file the jar file.
     * @return a list of classes names.
     */
    @SuppressWarnings("OverlyBroadCatchBlock")
    private static @NotNull List<String> getClassesNamesOfJar(@NotNull File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            return PluginLoader.iteratorToStream(zipFile.entries().asIterator())
                               .filter(entry -> entry.getName().endsWith(PluginLoader.CLASS_EXTENSION))
                               .map(classFileEntry -> {
                                   String classFileName = classFileEntry.getName();
                                   return classFileName
                                           .substring(0, classFileName.length() - PluginLoader.CLASS_EXTENSION.length())
                                           .replace(File.separatorChar, PluginLoader.PACKAGE_SEPARATOR)
                                           .replace('/', PluginLoader.PACKAGE_SEPARATOR);
                               })
                               .toList();
        } catch (IOException ignored) {
            return List.of();
        }
    }
    
    /**
     * Returns all resources matching with a pattern type from a directory.
     *
     * @param classpath the classpath.
     * @return a list of resource names.
     */
    private static @NotNull List<String> getClassesNamesFromDirectory(@NotNull File classpath) {
        String pathName;
        try {
            pathName = classpath.getCanonicalPath();
        } catch (IOException e) {
            return List.of();
        }
        ArrayDeque<File> directoriesQueue = new ArrayDeque<>(List.of(classpath));
        ArrayList<String> classResources = new ArrayList<>();
        while (!directoriesQueue.isEmpty()) {
            File currentDir = directoriesQueue.removeLast();
            File[] files = currentDir.listFiles();
            if (files == null) continue;
            Arrays
                    .stream(files)
                    .filter(file -> {
                        if (file.isDirectory())
                            directoriesQueue.addLast(file);
                        return file.isFile();
                    })
                    .map(file -> {
                        try {
                            return file.getCanonicalPath();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(fileName -> fileName.endsWith(PluginLoader.CLASS_EXTENSION))
                    .map(fileName -> {
                        // Takes the qualified name of the class, for example, if the classpath is "C:/openmarkov/core"
                        // and the class is "C:/openmarkov/core/apackage/aclass.class", it will return "apackage.aclass"
                        String className = fileName.substring(pathName.length() + 1,
                                                              fileName.length() - PluginLoader.CLASS_EXTENSION.length());
                        return className.replace(File.separatorChar, PluginLoader.PACKAGE_SEPARATOR);
                    })
                    .forEach(classResources::add);
        }
        return classResources;
    }
    
    /**
     * Creates an {@link Stream} out of a value that extends {@link Iterator}.
     *
     * @param iterator The iterator to turn into a {@link Stream}.
     * @param <IteratorValue> Value type of the {@link Iterator}.
     * @return A {@link Stream} with the elements of the {@link Iterator}.
     */
    private static <IteratorValue> @NotNull Stream<IteratorValue>
    iteratorToStream(@Nullable Iterator<IteratorValue> iterator) {
        if (iterator == null) {
            return Stream.empty();
        }
        Iterable<IteratorValue> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
    
}
