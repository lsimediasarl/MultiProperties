/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Control to retreive a MultiProperties ResourceBundle
 *
 * @author sbodmer
 */
public class MultiPropertiesResourceBundleControl extends ResourceBundle.Control {
    static public MultiPropertiesResourceBundleControl Control = new MultiPropertiesResourceBundleControl();
    
    /**
     * Time to live in ms (default to 5 mn)
     */
    static final int TTL = 5*60*1000;
    
    public MultiPropertiesResourceBundleControl() {
        super();
    }
    
    /**
     * Only the passed locale is a candidate (as only one file is needed to
     * store all locales)<p>
     * 
     * @param baseName
     * @param locale
     * @return 
     */
    @Override
    public List<Locale> getCandidateLocales(String baseName, Locale locale) {
        if (baseName == null) throw new NullPointerException();
        return Arrays.asList(locale);

    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        if (baseName == null || locale == null) throw new NullPointerException();
        return TTL;
    }

    /**
     * Return the "multiproperties" element
     *
     * @param baseName
     * @return
     */
    @Override
    public List<String> getFormats(String baseName) {
        if (baseName == null) throw new NullPointerException();
        return Arrays.asList("multiproperties");
    }

    /**
     * Return the same as the baseName in any case
     * 
     * @param baseName
     * @param locale
     * @return 
     */
    @Override
    public String toBundleName(String baseName, Locale locale) {
        if (baseName == null) throw new NullPointerException();
        return baseName;
    }
            
    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        // System.out.println("NEWBUNDLE:" + baseName + ", " + locale + ", " + format);
        if (baseName == null || locale == null || format == null || loader == null) throw new NullPointerException();

        ResourceBundle bundle = null;
        if (format.equals("multiproperties")) {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, format);
            // System.out.println("RESOURCENAME:"+resourceName);
            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    if (reload) {
                        // disable caches if reloading
                        connection.setUseCaches(false);
                    }
                    try (InputStream stream = connection.getInputStream()) {
                        if (stream != null) {
                            BufferedInputStream bis = new BufferedInputStream(stream);
                            bundle = new MultiPropertiesResourceBundle(bis, locale);
                        }
                    }
                }
            }
            
        }
        return bundle;
    }

    /**
     * Example how to use it
     * 
     * @param args 
     */
    public static void main(String args[]) {
        //---
        ResourceBundle bundle = ResourceBundle.getBundle("lsimedia/multiproperties/utils/ml", new Locale("fr"), MultiPropertiesResourceBundleControl.Control);
        String key = bundle.getString("word_yes");
        
    }
}
