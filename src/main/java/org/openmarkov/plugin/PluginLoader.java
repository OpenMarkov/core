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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipFile;

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
 * @author jvelez
 * @version 1.0 - jvelez: Initial implementation.
 * <br>1.1 - jrico:
 * <br>- Plugin loading is now done just once per program execution.
 * <br>- Functions are no longer recursive, but iterative.
 * <br>- PluginLoaderIF#loadAllPlugins(FilterIF) no longer throws PluginException (It was never thrown)
 * <br>- Added nullability annotations.
 */

@SuppressWarnings("unchecked") class PluginLoader {
    
    private static final ClassLoader CLASS_LOADER = ClassLoader.getSystemClassLoader();
    private static final String CLASS_EXTENSION = ".class";
    private static final char PACKAGE_SEPARATOR = '.';
    private static final String OPEN_MARKOV_PATH_PREFIX = "org.openmarkov";
    
    private static final Map<PluginClassCategory, List<Class<Object>>> LOADED_CLASSES = new HashMap<>();
    
    /**
     * Gets a stream of {@code Class<Object>} for a certain Plugin category.
     */
    static Stream<Class<Object>> pluginsStream(PluginClassCategory category) {
        PluginLoader.initializeCategory(category);
        return PluginLoader.LOADED_CLASSES.get(category).stream();
    }
    
    /**
     * Initializes the List of Classes of a certain Plugin category.
     */
    private static void initializeCategory(PluginClassCategory category) {
        if (PluginLoader.LOADED_CLASSES.containsKey(category)) {
            return;
        }
        switch (category) {
            case OPENMARKOV, EXTERNAL_DEPENDENCY -> PluginLoader.loadJarDependencies(category);
            case JAVA -> PluginLoader.loadJavaClasses();
        }
    }
    
    /**
     * Initializes the List of Classes corresponding to classes belonging to Java.
     */
    private static void loadJavaClasses() {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        Path modules = fs.getPath("/modules");
        var javaClasses = new ArrayList<Class<Object>>();
        try (Stream<Path> pathStream = Files.walk(modules)) {
            for (var path : pathStream.toList()) {
                if (!path.toString().endsWith(PluginLoader.CLASS_EXTENSION)) {
                    continue;
                }
                var subpath = path.subpath(2, path.getNameCount());
                var className = subpath.toString().replace("/", ".");
                try {
                    Class<Object> loadedClass = (Class<Object>) PluginLoader.CLASS_LOADER.loadClass(className.substring(0, className.length() - PluginLoader.CLASS_EXTENSION.length()));
                    if (PluginLoader.verifyClass(loadedClass)){
                        javaClasses.add(loadedClass);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        PluginLoader.LOADED_CLASSES.put(PluginClassCategory.JAVA, javaClasses);
    }
    
    private static boolean verifyClass(Class<Object> loadedClass) {
        try{
            loadedClass.getName();
            loadedClass.getSimpleName();
            return true;
        }catch (NoClassDefFoundError | IncompatibleClassChangeError e){
            return false;
        }
    }
    
    /**
     * Initializes either the List of Classes corresponding to classes belonging to OpenMarkov or to the external
     * dependencies.
     * <p>
     * The {@code category} parameter must be either {@link PluginClassCategory#OPENMARKOV} or
     * {@link PluginClassCategory#EXTERNAL_DEPENDENCY}.
     */
    private static void loadJarDependencies(PluginClassCategory category) {
        if (category != PluginClassCategory.OPENMARKOV && category != PluginClassCategory.EXTERNAL_DEPENDENCY) {
            return;
        }
        ScanResult scan = new ClassGraph().scan();
        var classPaths = scan.getClasspathURLs().stream().map(URL::getFile).toList();
        scan.close();
        List<Class<Object>> loadedClasses = new ArrayList<>();
        for (var classPath : classPaths) {
            for (var classQualifiedName : PluginLoader.getClassesQualifiedNames(classPath)) {
                try {
                    boolean isValidClass = switch (category) {
                        case OPENMARKOV -> classQualifiedName.startsWith(PluginLoader.OPEN_MARKOV_PATH_PREFIX);
                        case EXTERNAL_DEPENDENCY -> !classQualifiedName.startsWith(PluginLoader.OPEN_MARKOV_PATH_PREFIX);
                        default -> false;
                    };
                    if (isValidClass) {
                        Class<Object> loadedClass = (Class<Object>) PluginLoader.CLASS_LOADER.loadClass(classQualifiedName);
                        if (PluginLoader.verifyClass(loadedClass)){
                            loadedClasses.add(loadedClass);
                        }
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            }
        }
        PluginLoader.LOADED_CLASSES.put(category, loadedClasses);
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
     * @param iterator        The iterator to turn into a {@link Stream}.
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
