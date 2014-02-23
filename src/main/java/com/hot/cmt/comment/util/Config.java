package com.hot.cmt.comment.util;

/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Responsible for holding the Cache configuration properties. If the default
 * constructor is used, this class will load the properties from the
 * <code>cache.configuration</code>.
 * 
 * @author <a href="mailto:fabian.crabus@gurulogic.de">Fabian Crabus</a>
 * @version $Revision: 1.1 $
 */
public class Config implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final transient Logger logger = LoggerFactory.getLogger(Config.class);

    private static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    private static final String PROTOCOL_JAR = "jar";

    private ClassLoader classloader;
    /**
     * Properties map to hold the cache configuration.
     */
    private Properties properties = null;

    private String[] locations;

    public Config() {
        this.classloader = getClass().getClassLoader();
    }

    /**
     * @param locations
     */
    public Config(String[] locations) {
        this();
        this.locations = locations;
        loadProps();
    }

    public Config(String locations) {
        this(locations.split(","));
    }

    /**
     * Retrieve the value of the named configuration property. If the property
     * cannot be found this method will return <code>null</code>.
     * 
     * @param key
     *            The name of the property.
     * @return The property value, or <code>null</code> if the value could not
     *         be found.
     * 
     * @throws IllegalArgumentException
     *             if the supplied key is null.
     */
    public String get(String key) {
        return get(key, null);
    }

    /**
     * get string value with default
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public String get(String key, String defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (properties == null) {
            return null;
        }

        String value = properties.getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * get int value
     * 
     * @param key
     * @return
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * get int value with default
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(String key, int defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (properties == null) {
            return defaultValue;
        }
        String value = properties.getProperty(key);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * get boolean value
     * 
     * @param key
     * @return
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * get boolean value with default
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (properties == null) {
            return defaultValue;
        }
        String value = properties.getProperty(key);
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves all of the configuration properties. This property set should
     * be treated as immutable.
     * 
     * @return The configuration properties.
     */
    public Properties getProperties() {
        return properties;
    }

    private void loadProps() {
        if (locations == null) {
            return;
        }
        logger.debug("Getting Config");

        properties = new Properties();

        for (String location : locations) {
            location = location.trim();
            try {
                List<Resource> resources = getResources(location);
                for (Resource resource : resources) {
                    try {
                        InputStream in = resource.getInputStream();
                        properties.load(in);
                        logger.info("load {}", resource);
                        in.close();
                    } catch (Exception e) {
                        logger.error("Error load resource {}", resource);
                    }
                }
            } catch (Exception e) {
                logger.error("Error reading " + location + ", " + e);
                logger.error("Ensure the " + location + " file is readable and in your classpath.");
            } finally {
                
            }
        }
        logger.info("Properties " + properties);
    }

    private List<Resource> getResources(String locationPattern) throws IOException {
        Set<Resource> resources = new HashSet<Resource>();
        String rootDirPath = determineRootDir(locationPattern);
        String subPattern = locationPattern.substring(rootDirPath.length());

        if (rootDirPath.startsWith(CLASSPATH_ALL_URL_PREFIX)) { // 包含jar中的文件
            String findDirPath = rootDirPath.substring(CLASSPATH_ALL_URL_PREFIX.length());
            findDirPath = processPath(findDirPath);
            Enumeration<URL> urls = classloader.getResources(findDirPath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if (PROTOCOL_JAR.equals(protocol)) {
                    resources.addAll(findJarResource(url, findDirPath, subPattern));
                } else {
                    resources.addAll(findClasspathResource(url, findDirPath, subPattern));
                }
            }
        } else if (rootDirPath.startsWith(CLASSPATH_URL_PREFIX)) { // 不包含jar中的文件
            String findDirPath = rootDirPath.substring(CLASSPATH_URL_PREFIX.length());
            URL url = classloader.getResource(findDirPath);
            resources.addAll(findClasspathResource(url, findDirPath, subPattern));
        } else { // 单一文件
            resources.add(new Resource(locationPattern));
        }

        return new ArrayList<Resource>(resources);
    }

    private String determineRootDir(String location) {
        int prefixEnd = location.indexOf(":") + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd && location.substring(prefixEnd, rootDirEnd).contains("*")) {
            rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

    private Set<Resource> findClasspathResource(URL url, String findDirPath, String subPattern) {
        Set<Resource> resources = new HashSet<Resource>();
        if (subPattern.contains("*")) { // 匹配的所有文件
            File[] files = new File(url.getFile()).listFiles();
            if (files == null) {
                return resources;
            }
            for (File file : files) {
                if (isMatch(file.getName(), subPattern)) {
                    resources.add(new Resource(file));
                }
            }
        } else { // 单一文件
            findDirPath = processPath(findDirPath);
            resources.add(new Resource(findDirPath));
        }
        return resources;
    }

    private Set<Resource> findJarResource(URL url, String findDirPath, String subPattern) throws IOException {
        Set<Resource> resources = new HashSet<Resource>();
        URLConnection con = url.openConnection();
        if (con instanceof JarURLConnection) {
            JarURLConnection jarCon = (JarURLConnection) con;
            JarFile jarFile = jarCon.getJarFile();
            logger.info("load from jar file {}", jarCon.getJarFileURL().toExternalForm());
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(findDirPath)) {
                    if (isMatch(entryPath, subPattern)) {
                        resources.add(new Resource(2, entryPath));
                    }
                }
            }
            jarFile.close();
        }
        return resources;
    }

    private String processPath(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    private boolean isMatch(String path, String pattern) {
        int index = 0;
        String[] matches = pattern.split("\\*");
        for (String match : matches) {
            if ("".equals(match)) {
                continue;
            }
            int nextIndex = path.lastIndexOf(match);
            if (nextIndex != -1 && nextIndex >= index) {
                index = nextIndex;
            } else {
                return false;
            }
        }
        return true;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
        loadProps();
    }

    class Resource {
        int type; // 1=file 2=jarfile 3=url
        String path;
        File file;
        JarFile jarFile;
        URL url;

        public Resource(String path) {
            this.type = 0;
            this.path = path;
        }

        public Resource(File file) {
            this.type = 1;
            this.file = file;
            this.path = file.getPath();
        }

        public Resource(int type, String path) throws IOException {
            this.type = type;
            this.path = path;
            if (type == 1) {
                file = new File(path);
            } else if (type == 2) {
                // jarFile = new JarFile(path);
            } else if (type == 3) {
                url = new URL(path);
            }
        }

        public InputStream getInputStream() throws IOException {
            if (type == 1) {
                return new FileInputStream(file);
            } else if (type == 2) {
                return classloader.getResourceAsStream(path);
            } else if (type == 3) {
                return url.openStream();
            } else {
                return classloader.getResourceAsStream(path);
            }
        }

        @Override
        public String toString() {
            return "Resource[type=" + type + ", path=" + path + "]";
        }
    }
    
    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public long getLong(String key, long defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (properties == null) {
            return defaultValue;
        }
        String value = properties.getProperty(key);
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
   
    
    /**
     * 
     * @param key
     * @return
     */
    public double getDouble(String key) {
    	return getDouble(key, 0.0D);
    }
    
    /**
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    
    public double getDouble(String key, double defaultValue) {
    	if (key == null) {
    		throw new IllegalArgumentException("key is null");
    	}
    	
    	if (properties == null) {
    		return defaultValue;
    	}
    	String value = properties.getProperty(key);
    	try {
    		return Double.parseDouble(value);
    	} catch (Exception e) {
    		return defaultValue;
    	}
    }
    
}
