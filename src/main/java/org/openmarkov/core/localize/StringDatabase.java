/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.openmarkov.plugin.Filter;
import org.openmarkov.plugin.PluginLoader;

import javax.swing.event.EventListenerList;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class creates new string resources with the recorded language.
 *
 * @author jmendoza
 * @version 1.3. ibermejo challenge everything
 */
public class StringDatabase {
    
    /**
     * Default language.
     */
    private static final String DEFAULT_LANGUAGE = "en";

	/*
	private static final String DEFAULT_LANGUAGE = OpenMarkovPreferences
			.get(OpenMarkovPreferences.PREFERENCE_LANGUAGE, OpenMarkovPreferences.OPENMARKOV_LANGUAGES,
					System.getProperty("user.language"));
					
	OpenMarkovPreferences.set(OpenMarkovPreferences.PREFERENCE_LANGUAGE, newLanguage,
		OpenMarkovPreferences.OPENMARKOV_LANGUAGES);

	 */
    /**
     * Unique instance of this class.
     */
    private static StringDatabase USER_INSTANCE = null;
    /**
     * English instance of this class.
     */
    private static StringDatabase DEVELOPER_INSTANCE = null;
    /**
     * Language to use.
     */
    private String language = DEFAULT_LANGUAGE;
    /**
     * Locale to use
     */
    private Locale locale = null;
    /**
     * Map containing all the bundles
     */
    private Map<String, StringBundle> bundles = null;
    // Create the listener list
    private EventListenerList listenerList = null;
    
    /**
     * This constructor initializes the object with the language of the class.
     * Then creates all the resource bundles to check if the language is
     * available for all of them. If this language is not available for all, the
     * default one is used.
     */
    private StringDatabase() {
        setLocale(new Locale(language));
        /* Set format locale to english (to format decimal point)*/
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
        bundles = calculateAllBundles();
        listenerList = new EventListenerList();
        if (bundles.isEmpty()) {
            setLanguage("en");
        }
    }
    
    /**
     * Returns the unique instance of this class. If the instance doesn't exist,
     * then a new instance is initialized.
     *
     * @return the unique instance.
     */
    public static StringDatabase getUniqueInstance() {
        if (USER_INSTANCE == null) {
            USER_INSTANCE = new StringDatabase();
        }
        return USER_INSTANCE;
    }
    
    public static String surrondAsUnknown(String string) {
        return ">>> " + string + " <<<";
    }
    
    private Locale getLocaleByLanguage(String language) {
        if (language.equals("es")) {
            return new Locale("es");
        }
        // System.out.println("LocaleChangeEvent failure for locale "
        // + locale.toString() + ": not defined");
        // System.out.println("Setting english as default locale...");
        return Locale.ENGLISH;
    }
    
    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Sets the language to a new one.
     *
     * @param newLanguage new language.
     */
    public void setLanguage(String newLanguage) {
        if (!newLanguage.equals(language)) {
            language = (newLanguage.equals("es")) ? "es" : "en";
            language="en";
            setLocale(getLocaleByLanguage(language));
            /* Set format locale to english (to format decimal point)*/
            Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
            resetBundles();
            fireLocaleChangeEvent(new LocaleChangeEvent(this, newLanguage));
			/*
			OpenMarkovPreferences.set(OpenMarkovPreferences.PREFERENCE_LANGUAGE, newLanguage,
					OpenMarkovPreferences.OPENMARKOV_LANGUAGES);
			*/
        }
    }
    
    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }
    
    /*
     * Returns a string resource linked to the file given as parameter. The
     * value of the 'language' variable is used. If it is null or empty, the
     * language of the system is taken into account. If the system's language
     * isn't available, the default language is English.
     *
     * @param resourceFile file that contains the resource strings.
     * @return a resource bundle linked to the file.
     */
	/*
	public StringBundle getBundle(String resourceFile) {
		StringBundle stringBundle = null;
		XMLResourceBundle bundle = null;
		// TODO: Manolo
		//String file = "localize/" + resourceFile;
		String file = "/localize/" + resourceFile;
		try {
			bundle = (XMLResourceBundle) createXMLResourceBundle(file, locale);
		} catch (MissingResourceException e) {
			System.out.println("WARNING: Resource bundle " + resourceFile + " could not be found for locale '" + locale
					+ "'. English will be used instead");
			setLanguage("en");
			try {
				bundle = (XMLResourceBundle) createXMLResourceBundle(file, locale);
			} catch (MissingResourceException e1) {
				throw new MissingResourceException(
						"Any of the " + resourceFile.toLowerCase() + " resource string files is missing",
						StringDatabase.class.getName(), getLocale().getLanguage());
			}
		}
		stringBundle = new StringBundle(bundle);
		return stringBundle;
	}
	*/
/*	
	public StringBundle getStringBundle(ResourceBundle resourceBundle) {
		StringBundle stringBundle = null;
		XMLResourceBundle bundle = null;
		// TODO: Manolo
		//String file = "localize/" + resourceFile;
		String file = "/localize/" + resourceFile;
		try {
			bundle = (XMLResourceBundle) createXMLResourceBundle(file, locale);
		} catch (MissingResourceException e) {
			System.out.println("WARNING: Resource bundle " + resourceFile + " could not be found for locale '" + locale
					+ "'. English will be used instead");
			setLanguage("en");
			try {
				bundle = (XMLResourceBundle) createXMLResourceBundle(file, locale);
			} catch (MissingResourceException e1) {
				throw new MissingResourceException(
						"Any of the " + resourceFile.toLowerCase() + " resource string files is missing",
						StringDatabase.class.getName(), getLocale().getLanguage());
			}
		}
		stringBundle = new StringBundle(bundle);
		return stringBundle;
	}
	*/
    
    /**
     * @param newLocale the locale to set
     */
    public void setLocale(Locale newLocale) {
        locale = newLocale;
    }

	/*
	public Map<String, StringBundle> oldGetAllBundles() {
		
		Iterable<LocalizeResourcesProvider> providers = ServiceLoader.load(LocalizeResourcesProvider.class);
		
		Map<String, StringBundle> bundleMap = new LinkedHashMap<>();
		String localeSuffix = "_" + locale.getLanguage();
		String classPath = System.getProperty("java.class.path", ".");
		String[] classPathElements = classPath.split(File.pathSeparator);
		for (String element : classPathElements) {
			File classpathElement = new File(element);

			if (classpathElement.isDirectory()) {
				File localizeFolder = new File(classpathElement.getAbsolutePath() + File.separator + "localize");
				if (localizeFolder.listFiles() != null) {
					for (final File fileEntry : localizeFolder.listFiles()) {
						if (fileEntry.isFile()) {
							if (fileEntry.getName().endsWith(".xml")) {
								String baseName = FilenameUtils.getBaseName(fileEntry.getName());
								if (baseName.endsWith(localeSuffix)) {
									baseName = baseName.substring(0, baseName.length() - localeSuffix.length());
									bundleMap.put(baseName, getBundle(baseName));
								}
							}
						}
					}
				}
			} else { // it is a jar file
				ZipFile zipFile;
				try {
					zipFile = new ZipFile(classpathElement.getAbsolutePath());
					Enumeration<? extends ZipEntry> zipEntryEn = (Enumeration<? extends ZipEntry>) zipFile.entries();
					while (zipEntryEn.hasMoreElements()) {
						ZipEntry aZipEntry = (ZipEntry) zipEntryEn.nextElement();
						if (aZipEntry.getName().startsWith("localize/") && aZipEntry.getName().endsWith(".xml")) {
							String baseName = FilenameUtils.getBaseName(aZipEntry.getName());
							if (baseName.endsWith(localeSuffix)) {
								int endPosition = baseName.length() - localeSuffix.length();
								baseName = baseName.substring(0, endPosition);
								bundleMap.put(baseName, getBundle(baseName));
							}
						}
					}
					zipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bundleMap;
	}
	*/
    
    private Map<String, StringBundle> calculateAllBundles() {
        //Iterable<LocalizeResourcesProvider> providers = ServiceLoader.load(LocalizeResourcesProvider.class);
        Iterable<LocalizeResourcesProvider> providers = getBundleProviders()
                .toList();
        Map<String, StringBundle> bundlesMap = new LinkedHashMap<>();
        for (LocalizeResourcesProvider provider : providers) {
            bundlesMap.putAll(provider.getBundlesMap(this.locale));
        }
        return bundlesMap;
    }
    
    public static @NotNull Stream<LocalizeResourcesProvider> getBundleProviders() {
        return new PluginLoader()
                .loadAllPlugins(Filter.filter().toImplement(LocalizeResourcesProvider.class))
                .stream()
                .map(c -> {
                    try {
                        return (LocalizeResourcesProvider) c.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }
    
    public Map<String, StringBundle> getAllBundles(){
        return this.bundles;
    }
    
    /**
     * @param file
     * @param locale
     * @return An instance of ResourceBundle considering that properties files
     * are in XML format.
     */
	/*
	public ResourceBundle oldCreateXMLResourceBundle(String file, Locale locale) {
		ResourceBundle bundle;
		bundle = ResourceBundle.getBundle(file, locale, new ResourceBundle.Control() {
			public java.util.List<String> getFormats(String baseName) {
			if (baseName == null)
					throw new NullPointerException();
				return Arrays.asList("xml");
			}

			public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
					boolean reload) throws IllegalAccessException, InstantiationException, IOException {
				if (baseName == null || locale == null || format == null || loader == null)
					throw new NullPointerException();
				ResourceBundle bundle = null;
				if (format.equals("xml")) {
					String bundleName = toBundleName(baseName, locale);
					String resourceName = toResourceName(bundleName, format);
					InputStream stream = null;
					if (reload) {
						URL url = loader.getResource(resourceName);
						if (url != null) {
							URLConnection connection = url.openConnection();
							if (connection != null) {
								// Disable caches to get fresh data for
								// reloading.
								connection.setUseCaches(false);
								stream = connection.getInputStream();
							}
						}
					} else {
						stream = loader.getResourceAsStream(resourceName);
					}
					if (stream != null) {
						BufferedInputStream bis = new BufferedInputStream(stream);
						bundle = new XMLResourceBundle(bis);
						bis.close();
					}
				}
				return bundle;
			}
		});
		return bundle;
	}
	*/
    public ResourceBundle createXMLResourceBundle(String file, Locale locale) {
        ResourceBundle bundle;
        bundle = ResourceBundle.getBundle(file, locale);
        return bundle;
        
    }
    
    // This methods allows classes to register for LocaleChangeEvent
    public void addLocaleChangeListener(LocaleChangeListener listener) {
        listenerList.add(LocaleChangeListener.class, listener);
    }
    
    // This methods allows classes to unregister for LocaleChangeEvent
    public void removeLocaleChangeListener(LocaleChangeListener listener) {
        listenerList.remove(LocaleChangeListener.class, listener);
    }
    
    /**
     * This private class is used to fire LocaleChangeEvent
     *
     * @param evt - event to manage for locale change
     */
    protected void fireLocaleChangeEvent(LocaleChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == LocaleChangeListener.class) {
                ((LocaleChangeListener) listeners[i + 1]).processLocaleChange(evt);
            }
        }
    }
    
    /**
     * reset the StringResource to null
     */
    private void resetBundles() {
        bundles.clear();
        bundles = calculateAllBundles();
    }
    
    public String getString(String key) {
        String value = this.getNullableString(key);
        return value != null ? value : StringDatabase.surrondAsUnknown(key);
    }
    
    public @Nullable String getNullableString(String key) {
        for (StringBundle bundle : this.bundles.values()) {
            String value = bundle.getString(key);
            if (value != null)
                return value;
        }
        return null;
    }
    
    
    public String getString(String bundle, String key) {
        String value = this.getNullableString(bundle, key);
        return value != null ? value : StringDatabase.surrondAsUnknown(key);
    }
    
    public @Nullable String getNullableString(@Nullable String bundle, String key) {
        StringBundle stringBundle = this.bundles.get(bundle);
        if (stringBundle == null) {
            return null;
        }
        return stringBundle.getString(key);
    }
    
    /**
     * This method returns the requested string resource, replacing each '~' by
     * an element of the array. The number of '~' replaced depends on the number
     * of elements of the array.
     *
     * @param key     the key of the desired string.
     * @param strings strings that will replace the '~'.
     * @return the string associated with the key. if the resource doesn't
     * exist, then a special string is returned.
     */
    public String getFormattedString(String key, String... strings) {
        String result = "";
        String parameter = "";
        boolean flag = true;
        int i = 0;
        int l = 0;
        int index = 0;
        final String diacritic = "~";
        try {
            result = getString(key);
            if (strings != null) {
                l = strings.length;
                while (flag && (i < l)) {
                    if ((index = result.indexOf(diacritic, index)) >= 0) {
                        parameter = strings[i++];
                        if (parameter == null) {
                            parameter = "";
                        }
                        result = result.substring(0, index) + result.substring(index)
                                                                    .replaceFirst(diacritic, parameter);
                        index += parameter.length();
                    } else {
                        flag = false;
                    }
                }
            }
        } catch (MissingResourceException e1) {
            result = StringDatabase.surrondAsUnknown(key);
        }
        return result;
    }
}
