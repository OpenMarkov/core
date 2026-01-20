/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.format.annotation;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnreacheableException;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.plugin.PluginSearch;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class is the manager of the format annotations. Detects the plugins with FormatType
 * annotations.
 *
 * @author mpalacios
 * @author carmenyago -adapted the manager to different versions of ProbModelXML
 * @see FormatType
 */
public class FormatManager {
    private static final FormatManager INSTANCE = new FormatManager();
    
    /**
     * The Reader role
     */
    private String roleReader = "Reader";
    
    /**
     * The writer role
     */
    private String roleWriter = "Writer";
    
    /**
     * Reader classes
     * It is a Map&lt;extension, &lt;version, readerClass&gt;&gt;
     */
    private Map<String, Map<String, Class<?>>> readerClasses;
    
    /**
     * Writer classes
     * It is a Map&lt;extension, &lt;version, writerClass&gt;&gt;
     */
    private Map<String, Map<String, Class<?>>> writerClasses;
    
    /**
     * Reader instances
     * It is a Map&lt;extension, &lt;version, readerInstance&gt;&gt;
     */
    private Map<String, Map<String, ProbNetReader>> readerInstances;
    
    /**
     * Writer instances
     * It is a Map&lt;extension, &lt;version, writerInstance&gt;&gt;
     */
    private Map<String, Map<String, ProbNetWriter>> writerInstances;
    
    /**
     * Gets a FormatManager instance
     */
    private FormatManager() {
        super();
        
        this.readerClasses = new LinkedHashMap<>();
        this.writerClasses = new LinkedHashMap<>();
        this.readerInstances = new LinkedHashMap<>();
        this.writerInstances = new LinkedHashMap<>();
        
        findAllFormatPlugins().forEach(plugin -> {
            FormatType lAnnotation = plugin.getAnnotation(FormatType.class);
            
            if (lAnnotation.role().equals(roleReader)) {
            	/*
            	readerClasses.put (lAnnotation.extension (), plugin);
            	*/
                String extension = lAnnotation.extension();
                String version = "";
                if (!extension.equals("elv")) {
                    version = lAnnotation.version();
                }
                Map<String, Class<?>> readerForExtension = readerClasses.get(extension);
                if (readerForExtension != null) {
                    readerForExtension.put(version, plugin);
                } else {
                    Map<String, Class<?>> versionsHash = new LinkedHashMap<>();
                    versionsHash.put(version, plugin);
                    readerClasses.put(extension, versionsHash);
                }
            }
            if (lAnnotation.role().equals(roleWriter)) {
            	/*
            	writerClasses.put (lAnnotation.extension (), plugin);
            	*/
                
                String extension = lAnnotation.extension();
                String version = "";
                if (!extension.equals("elv")) {
                    version = lAnnotation.version();
                }
                Map<String, Class<?>> writerForExtension = writerClasses.get(extension);
                if (writerForExtension != null) {
                    writerForExtension.put(version, plugin);
                } else {
                    Map<String, Class<?>> versionsHash = new LinkedHashMap<>();
                    versionsHash.put(version, plugin);
                    writerClasses.put(extension, versionsHash);
                }
                
            }
        });
    }
    
    /**
     * Gets a FormatManager instance
     *
     * @return FormatManager instance
     */
    public static FormatManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * This method gets all the plugins with FormatTypeProbModelXML annotations
     *
     * @return a list with the plugins detected with FormatTypeProbModelXML annotations.
     */
    private static Stream<Class<Object>> findAllFormatPlugins() {
        return PluginSearch.init().annotatedWith(FormatType.class).stream();
    }
    //	/**
    //	 * Gets the plugin with the "Writer" role and the extension
    //	 * @param extension the extension required
    //	 * @return a probNetWriter object
    //	 */
    //	public ProbNetWriter getProbNetWriter (String extension)
    //	{
    //	    ProbNetWriter instance = null;
    //	    if(writerInstances.containsKey (extension))
    //	    {
    //	        instance = writerInstances.get(extension);
    //	    }else
    //	    {
    //	        if(writerClasses.containsKey (extension))
    //	        {
    //        		try
    //        		{
    //        		    instance = (ProbNetWriter) writerClasses.get (extension).newInstance ();
    //        		}
    //        		catch (Exception e) {}
    //	        }
    //	    }
    //		return instance;
    //	}
    
    /**
     * Gets the plugin with the "Writer" role, the extension and the version of the network.
     * If the extension is "elv" corresponding to Elvira enconding, fileFormat is the empty string
     *
     * @param extension  - the extension corresponding to the enconding of the file (elv, pgmx)
     * @param fileFormat - format and version of the file
     *
     * @return the ProbNetWriter corresponding to the selected extension and format of the file
     *
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     */
    public ProbNetWriter getProbNetWriter(String extension, String fileFormat) {
        ProbNetWriter instance = null;
        String version = "";
        if (!(fileFormat.equals("Elvira"))) {
            version = fileFormat.substring(fileFormat.indexOf('.') + 1);
        }
        
        if ((writerInstances.containsKey(extension)) && (writerInstances.get(extension).containsKey(version))) {
            Map<String, ProbNetWriter> versionsHash = writerInstances.get(extension);
            instance = versionsHash.get(version);
        } else {
            if ((writerClasses.containsKey(extension)) && (writerClasses.get(extension).containsKey(version))) {
                Map<String, Class<?>> versionsHash = writerClasses.get(extension);
                try {
                    instance = (ProbNetWriter) versionsHash.get(version).getDeclaredConstructor().newInstance();
                } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new UnreacheableException(e);
                }
            }
        }
        return instance;
        
    }
    
    
    //	/**
    //	 * Gets the plugin with the "Reader" role and the extension
    //	 * @param extension the extension required
    //	 * @return a probNetReader object
    //	 */
    //	public ProbNetReader getProbNetReader (String extension)
    //	{
    //	    ProbNetReader instance = null;
    //        if(readerInstances.containsKey (extension))
    //        {
    //            instance = readerInstances.get(extension);
    //        }else
    //        {
    //            if(readerClasses.containsKey (extension))
    //            {
    //                try
    //                {
    //                    instance = (ProbNetReader) readerClasses.get (extension).newInstance ();
    //                }
    //                catch (Exception e) {}
    //            }
    //        }
    //        return instance;
    //	}
    
    /**
     * Gets the plugin corresponding to the "Reader" role, the extension and the version
     *
     * @param fileName File name
     *
     * @return a ProbNetReader object
     *
     * @throws Exception when an exception is raised is thrown to be caught by the gui
     */
    public ProbNetReader getProbNetReader(String fileName) throws SAXException, IOException, IllegalArgumentException, SecurityException, NoReaderForFileException, ParserException.BadlyStructuredFile {
        return getProbNetReader(new File(fileName).toURI().toURL());
        /*
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String fileVersion = "";
        
        if (!fileExtension.equals("elv")) {
            checkVersion(fileName);
            checkStructure(fileName);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(fileName));
            fileVersion = doc.getDocumentElement().getAttribute("formatVersion");
            //Removing the last index of the version
            fileVersion = fileVersion.substring(0, fileVersion.lastIndexOf('.'));
        }
        ProbNetReader reader = getProbNetReader(fileExtension, fileVersion);
        return reader;
         */
    }
    
    /**
     * Gets the plugin corresponding to the "Reader" role, given the URL of network
     *
     * @param url URL of the resource
     *
     * @return a ProbNetReader object
     *
     * @throws Exception when an exception is raised is thrown to be caught by the gui
     */
    public ProbNetReader getProbNetReader(URL url) throws SAXException, IOException, NoReaderForFileException, ParserException.BadlyStructuredFile {
        //checkVersion(url);
        checkStructure(url);
        String fileName = url.getFile();
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String fileVersion = "";
        if (!fileExtension.equals("elv")) {
            DocumentBuilder docBuilder;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new UnreacheableException(e);
            }
            Document doc = docBuilder.parse(url.openStream());
            fileVersion = doc.getDocumentElement().getAttribute("formatVersion");
            //Removing the last index of the version
            fileVersion = fileVersion.substring(0, fileVersion.lastIndexOf('.'));
        }
        ProbNetReader reader = getProbNetReader(fileExtension, fileVersion);
        if (reader == null) {
            throw new NoReaderForFileException(fileExtension, fileVersion, url);
        }
        return reader;
    }
    
    public void checkVersion(URL url) throws SAXException, IOException {
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("version.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsd);
        Schema schema = factory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnreacheableException(e);
        }
        Document document = db.parse(url.openStream());
        validator.validate(new DOMSource(document));
    }
    
    /**
     * Gets the plugin corresponding to the "Reader" role, the extension and the version
     *
     * @param extension - the extension required
     * @param version   - the version of the ProbModel required
     *
     * @return a probNetReader object
     *
     * @throws Exception when an exception is raised is thrown to be caught by the gui
     */
    public ProbNetReader getProbNetReader(String extension, String version) {
        ProbNetReader instance = null;
        if ((readerInstances.containsKey(extension)) && (readerInstances.get(extension).containsKey(version))) {
            Map<String, ProbNetReader> versionsHash = readerInstances.get(extension);
            instance = versionsHash.get(version);
        } else {
            if ((readerClasses.containsKey(extension)) && (readerClasses.get(extension).containsKey(version))) {
                Map<String, Class<?>> versionsHash = readerClasses.get(extension);
                try {
                    instance = (ProbNetReader) versionsHash.get(version).getDeclaredConstructor().newInstance();
                } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new UnreacheableException(e);
                }
            }
        }
        return instance;
    }
    
    //	/**
    //	 * Gets the extension of the file given by fileName
    //	 * @param fileName
    //	 * 			- the name of the file
    //	 * @return the extension of fileName
    //	 */
    //	private static String getFileExtension(String fileName) {
    //
    //		String fileExtension = null;
    //		int i = fileName.lastIndexOf('.');
    //		if ((i > 0) && (i < (fileName.length() - 1))) {
    //			fileExtension = fileName.substring(i + 1).toLowerCase();
    //		}
    //
    //		return fileExtension;
    //
    //	}
    
    
    /**
     * Gets the extension, description of all the writers
     *
     * @return a HashMap with a pair (extension, description) for each writer
     */
    public HashMap<String, String> getWriters() {
        HashMap<String, String> writers = new HashMap<>();
        for (String extension : writerClasses.keySet()) {
            for (String version : writerClasses.get(extension).keySet()) {
                FormatType lAnnotation = writerClasses.get(extension).get(version).getAnnotation(FormatType.class);
                writers.put(lAnnotation.description(), lAnnotation.extension());
            }
        }
        
        return writers;
    }
    //	/**
    //     * Gets the all the reader plugins
    //     * @return all the reader plugins found
    //     */
    //
    //    public HashMap<String, String> getReaders()
    //    {
    //        HashMap<String, String> writers = new HashMap<>();
    //        for (String extension : readerClasses.keySet ()) {
    //            FormatType lAnnotation = readerClasses.get (extension).getAnnotation (FormatType.class);
    //            writers.put (lAnnotation.description(), lAnnotation.extension());
    //        }
    //
    //        return writers;
    //    }
    //	/**
    //     * Gets the all the reader plugins
    //     * @return a HashMap with the <description, extension> of every extension found
    //     */
    
    /**
     * Gets the (extension, description) of the readers
     *
     * @return a Map with all the extensions found
     */
    
    public HashMap<String, String> getReaders() {
        HashMap<String, String> readers = new HashMap<>();
        for (String extension : readerClasses.keySet()) {
            for (String version : readerClasses.get(extension).keySet()) {
                FormatType lAnnotation = readerClasses.get(extension).get(version).getAnnotation(FormatType.class);
                String description = lAnnotation.description();
                int indexDot = description.indexOf('.');
                if (indexDot > -1) {
                    description = description.substring(0, indexDot);
                }
                readers.put(description, lAnnotation.extension());
                break;
            }
        }
        
        return readers;
    }
    
    public void checkVersion(String name) throws SAXException, IOException, ParserConfigurationException {
        
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("version.xsd");
        
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document document = parser.parse(new File(name));
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        Source schemaFile = new StreamSource(xsd);
        Schema schema = factory.newSchema(schemaFile);
        
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(document));
    }
    
    
    public void checkStructure(String name) throws SAXException, IOException, ParserException.BadlyStructuredFile {
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("val_v4.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsd);
        Schema schema = factory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        URL url = new File(name).toURI().toURL();
        try {
            validator.validate(new StreamSource(url.openStream()));
        } catch (SAXParseException e) {
            throw new ParserException.BadlyStructuredFile(url, e);
        }
    }
    
    public void checkStructure(URL url) throws SAXException, IOException, ParserException.BadlyStructuredFile {
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("val_v4.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsd));
        try {
            schema.newValidator().validate(new StreamSource(url.openStream()));
        } catch (SAXParseException e) {
            throw new ParserException.BadlyStructuredFile(url, e);
        }
    }
    
}























