package ch.vilki.secured;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static ch.vilki.secured.SecStorage.ENC_VERSION;

public class SecureProperties {

    static Logger logger = LoggerFactory.getLogger(SecStorage.class);

    Multimap<LinkedHashSet<String>, SecureProperty> _map = null;
    private Path _filePath = null;

    public SecureProperties() {
        _map = ArrayListMultimap.create();
    }

    public String getStringProperty(String key) {
        return getStringProperty(SecureProperty.parseKey(key));
    }

    public String getStringProperty(String[] key) {
        SecureProperty secureProperty = getProperty(key);
        if (secureProperty == null) return null;
        return secureProperty.get_value();
    }

    public boolean getBooleanValue(String key) {
        return getBooleanValue(SecureProperty.parseKey(key));
    }
    public boolean getBooleanValue(String[] key) {
        String val = getStringProperty(key);
        if (val == null) return false;
        if (val.equalsIgnoreCase("true")) return true;
        return false;
    }

    public void addStringProperty(String key[], String value,boolean encrypted) {
        addProperty(SecureProperty.createNewSecureProperty(key, value,encrypted));
    }

    public void addBooleanProperty(String key[], boolean value) {
        String booleanValue = "false";
        if(value) booleanValue = "true";
        SecureProperty secureProperty = SecureProperty.createNewSecureProperty(key,booleanValue,false);
        if (value) secureProperty.set_value("true");
        else secureProperty.set_value("false");
        addProperty(secureProperty);
    }

    public SecureProperty getProperty(String key){
        return getProperty(SecureProperty.parseKey(key));
    }

    public List<SecureProperty> getAllProperties(String key)
    {
        List<SecureProperty> retValue = new ArrayList<>();
        LinkedHashSet<String> compareWith = SecureProperty.createKey(key);
        for(LinkedHashSet<String> k: _map.keySet())
        {
            if(SecureProperty.createKeyWithSeparator(k).contains("STORAGE@@")) continue;
            Collection<SecureProperty> keyProperties = _map.get(k);
            for(SecureProperty secureProperty: keyProperties)
            {
                if(secureProperty.isSubKeyOf(compareWith)) retValue.add(secureProperty);
            }
        }
        return  retValue;
    }

    public Set<String> getAllChildLabels(String key)
    {
        Set<String> retValue = new HashSet<>();
        LinkedHashSet<String> compareWith = SecureProperty.createKey(key);
        for(LinkedHashSet<String> k: _map.keySet())
        {
            if(SecureProperty.createKeyWithSeparator(k).contains("STORAGE@@")) continue;
            Collection<SecureProperty> keyProperties = _map.get(k);
            for(SecureProperty secureProperty: keyProperties)
            {
                if(secureProperty.isChildOf(compareWith))
                {
                    LinkedHashSet<String> property_key = (LinkedHashSet<String>) secureProperty.get_key().clone();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(SecureProperty.createKeyWithSeparator(compareWith));
                    if(stringBuilder.length() > 0) stringBuilder.append("@@");
                    property_key.removeAll(compareWith);
                    Iterator<String> it=property_key.iterator();
                    stringBuilder.append(it.next());
                    retValue.add(stringBuilder.toString());
                }
            }
        }
        return retValue;
    }
    public Set<String> getAllLabels()
    {
        Set<String> retValue = new HashSet<>();
        for(LinkedHashSet<String> k: _map.keySet())
        {
            if(SecureProperty.createKeyWithSeparator(k).contains("STORAGE@@")) continue;
            StringBuilder stringBuilder = new StringBuilder();
           String[] array = k.toArray(new String[k.size()]);
           for(int i=0; i< array.length-1; i++)
           {
               stringBuilder.append(array[i]);
               stringBuilder.append("@@");
           }
           if(stringBuilder.length() > 2)
           {
               stringBuilder.deleteCharAt(stringBuilder.length()-1);
               stringBuilder.deleteCharAt(stringBuilder.length()-1);
           }
           if(stringBuilder.length() > 0) retValue.add(stringBuilder.toString());
           else retValue.add("");
        }
        return retValue;

    }

    public List<String> getAllKeys()
    {
        List<String> retValue = new ArrayList<>();
        for(LinkedHashSet<String> k: _map.keySet())
        {
            if(SecureProperty.createKeyWithSeparator(k).contains("STORAGE@@")) continue;
            retValue.add(SecureProperty.createKeyWithSeparator(k));

        }
        return retValue;
    }

    public SecureProperty getProperty(String[] key) {
        for (LinkedHashSet<String> s : _map.keySet()) {
            Collection<SecureProperty> properties = _map.get(s);
            for (SecureProperty secureProperty : properties) {
                if (secureProperty.isKeyEqual(key)) return secureProperty;
            }
        }
        return null;
    }

    public void  addProperty(SecureProperty secureProperty) {
        if (!hasUnorderedProperty(secureProperty)) {
            _map.put(secureProperty.get_key(), secureProperty);
            return;
        }
        Collection<SecureProperty> found_properties = _map.get(secureProperty.get_key());
        Iterator<SecureProperty> it = found_properties.iterator();
        while (it.hasNext()) {
            SecureProperty s = it.next();
            if (s.isKeyEqual(secureProperty.get_key())) it.remove();
        }
        found_properties.add(secureProperty);
     }

    public void  deleteProperty(SecureProperty secureProperty) {
        if (!hasUnorderedProperty(secureProperty)) {
           return;
        }
        Collection<SecureProperty> found_properties = _map.get(secureProperty.get_key());
        Iterator<SecureProperty> it = found_properties.iterator();
        while (it.hasNext()) {
            SecureProperty s = it.next();
            if (s.isKeyEqual(secureProperty.get_key())) it.remove();
        }
    }

    private boolean hasUnorderedProperty(SecureProperty secureProperty) {
        if (_map.isEmpty()) return false;
        if (_map.get(secureProperty.get_key()).isEmpty()) return false;
        return true;
    }

    public void saveProperties() {
        String fileName = _filePath.toString().replace(".json",".properties");
        if(!fileName.endsWith("properties")) fileName = fileName+".properties";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            ArrayList<SecureProperty> data = new ArrayList<>();
            ArrayList<SecureProperty> header = new ArrayList<>();
            data = addData(data);
            data.sort(SecureProperty::compareTo);
            header = addHeader(header);
            writer.write("-------------------------------@@HEADER_START@@------------------------------------------------------------- ");
            writer.write(System.lineSeparator());
            for(SecureProperty secureProperty: header) createFileEntry(secureProperty,writer);
            writer.write("-------------------------------@@HEADER_END@@-------------------------------------------------------------");
            writer.write(System.lineSeparator());
            for(SecureProperty secureProperty: data)  createFileEntry(secureProperty,writer);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFileEntry(SecureProperty secureProperty, BufferedWriter writer) throws IOException {
        String key = SecureProperty.createKeyWithSeparator(secureProperty.get_key());
        writer.write(key);
        writer.write("=");
        if(secureProperty.is_encrypted())
        {
            writer.write("{ENC}");
            if(secureProperty.get_value()!= null) writer.write(secureProperty.get_value());
            writer.write("{ENC}");
        }
        else
        {
            if(secureProperty.get_value()!= null) writer.write(secureProperty.get_value());
        }
        writer.write(System.lineSeparator());
    }

    private  ArrayList<SecureProperty>  addData( ArrayList<SecureProperty> export)
    {
        LinkedHashSet<String> hash = SecureProperty.createKey(SecStorage.MASTER_PASSWORD_HASH);
        LinkedHashSet<String> win = SecureProperty.createKey(SecStorage.MASTER_PASSWORD_WIN_SECURED);
        LinkedHashSet<String> test = SecureProperty.createKey(SecStorage.TEST_STRING);
        LinkedHashSet<String> vers = SecureProperty.createKey(ENC_VERSION);

        for (LinkedHashSet key : _map.keySet()) {
            if(key.equals(hash)) continue;
            if(key.equals(win)) continue;
            if(key.equals(test)) continue;
            if(key.equals(vers)) continue;
            Collection secureProperty = _map.get(key);
            Iterator<SecureProperty> iterator = secureProperty.iterator();
            while (iterator.hasNext()) export.add(iterator.next());
        }
        return export;
    }

    private  ArrayList<SecureProperty>  addHeader( ArrayList<SecureProperty> export)
    {
        LinkedHashSet<String> hash = SecureProperty.createKey(SecStorage.MASTER_PASSWORD_HASH);
        LinkedHashSet<String> win = SecureProperty.createKey(SecStorage.MASTER_PASSWORD_WIN_SECURED);
        LinkedHashSet<String> test = SecureProperty.createKey(SecStorage.TEST_STRING);
        LinkedHashSet<String> vers = SecureProperty.createKey(ENC_VERSION);

        Collection secureProperty = null;
        secureProperty = _map.get(hash);
        if(secureProperty != null)
        {
            Iterator<SecureProperty> iterator = secureProperty.iterator();
            while (iterator.hasNext()) export.add(iterator.next());
        }
        secureProperty = _map.get(win);
        if(secureProperty != null)
        {
            Iterator<SecureProperty> iterator = secureProperty.iterator();
            while (iterator.hasNext()) export.add(iterator.next());
        }
        secureProperty = _map.get(test);
        if(secureProperty != null)
        {
            Iterator<SecureProperty> iterator = secureProperty.iterator();
            while (iterator.hasNext()) export.add(iterator.next());
        }
        secureProperty = _map.get(vers);
        if(secureProperty != null)
        {
            Iterator<SecureProperty> iterator = secureProperty.iterator();
            while (iterator.hasNext()) export.add(iterator.next());
        }

        return export;
    }
    public static SecureProperties openSecuredProperties(String fileName, boolean checkVersion) throws IOException, SecureStorageException {
        SecureProperties secureProperties = new SecureProperties();
        secureProperties._filePath = Paths.get(fileName);
        if(!Files.exists(secureProperties._filePath)) throw new SecureStorageException(SecureStorageException.FILE_NOT_EXISTS);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(secureProperties._filePath.toString()), StandardCharsets.UTF_8))) {
            String l = null;
            while ((l=br.readLine()) != null) {
                if (l.contains("@@HEADER_START@@") || l.contains("@@HEADER_END@@")) continue;
                SecureProperty secureProperty = secureProperties.readFileEntry(l);
                if(secureProperty != null) secureProperties.addProperty(secureProperty);
            }
        }
        if(checkVersion){
            if(secureProperties.getProperty(ENC_VERSION)!=null && secureProperties.getProperty(ENC_VERSION).get_value().equalsIgnoreCase("2")){
                logger.info("encrypted with version 2 check passed");
            }
            else{
                throw new SecureStorageException(SecureStorageException.OLD_VERSION);
            }
        }
        return secureProperties;
    }


    private SecureProperty readFileEntry(String line)
    {
        if(line == null) return null;
        int posEqual = line.indexOf("=");
        if(posEqual==-1) return null;
        String keyString = line.substring(0,posEqual);
        SecureProperty secureProperty = new SecureProperty();
        secureProperty.set_key(SecureProperty.createKey(keyString));
        String valueString = line.substring(posEqual+1);
        if(valueString == null || valueString.equalsIgnoreCase(""))
        {
            secureProperty.set_encrypted(false);
        }
        else {
            if(valueString.startsWith("{ENC}") && valueString.endsWith("{ENC}"))
            {
                String val = valueString.replace("{ENC}","");
                secureProperty.set_value(val);
                secureProperty.set_encrypted(true);
            }
            else
            {
                secureProperty.set_value(valueString);
                secureProperty.set_encrypted(false);
            }
        }
        return secureProperty;
    }

    public static SecureProperties createSecuredProperties(String fileName) throws SecureStorageException, IOException {
        SecureProperties secureProperties = new SecureProperties();
        secureProperties._filePath = Paths.get(fileName);
        if(Files.exists(secureProperties._filePath)) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(secureProperties._filePath.toString()), StandardCharsets.UTF_8))) {
                String l = null;
                while ((l = br.readLine()) != null) {
                    if (l.contains("@@HEADER_START@@") || l.contains("@@HEADER_END@@")) {
                        throw new SecureStorageException(SecureStorageException.FILE_EXISTS_ALREADY);
                    }
                    SecureProperty secureProperty = secureProperties.readFileEntry(l);
                    if (secureProperty != null) secureProperties.addProperty(secureProperty);
                }
            }
        }
        secureProperties.saveProperties();
        return secureProperties;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();


        return "SecureProperties{" +
                "_map=" + _map +
                ", _filePath=" + _filePath +
                '}';
    }
}







