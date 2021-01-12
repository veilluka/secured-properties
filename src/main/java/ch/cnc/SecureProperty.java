package ch.cnc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sun.util.resources.cldr.om.CalendarData_om_ET;

import java.security.KeyStore;
import java.util.*;

public class SecureProperty implements Comparable<SecureProperty> {

    private LinkedHashSet<String> _key = new LinkedHashSet<>();
    private String _value;
    private boolean _encrypted;


    public static LinkedHashSet<String> createKey(String[] key)
    {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        for(int i=0; i< key.length; i++) linkedHashSet.add(key[i]);
        return linkedHashSet;
    }

    public static LinkedHashSet<String> createKey(String key)
    {
        return createKey(parseKey(key));
    }

    public static String[] parseKey(String key)
    {
        if(key == null) return new String[0];
        return  key.split("@@");
    }

    public static String createKeyWithSeparator(LinkedHashSet<String> key)
    {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = key.iterator();
        while (iterator.hasNext())
        {
            stringBuilder.append(iterator.next());
            stringBuilder.append("@@");
        }
        if(stringBuilder.length() > 2)
        {
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        return stringBuilder.toString();
    }

    public static SecureProperty createNewSecureProperty(String key,String value,boolean encrypted)
    {
        return createNewSecureProperty(SecureProperty.parseKey(key),value,encrypted);
    }
    public static SecureProperty createNewSecureProperty(String[] key,String value,boolean encrypted)
    {
        SecureProperty secureProperty = new SecureProperty();

        if(key!=null && key.length > 0)  secureProperty.set_key(key);
        secureProperty.set_value(value);
        secureProperty.set_encrypted(encrypted);
        return secureProperty;
    }

    public static String getLabel(LinkedHashSet<String> labelWithKey)
    {
        String lblWKey[] = labelWithKey.toArray(new String[labelWithKey.size()]);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i< labelWithKey.size() -1; i++)
        {
            stringBuilder.append(lblWKey[i]);
            stringBuilder.append("@@");
        }
        if(stringBuilder.length() == 0) return "";
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();

    }

    public SecureProperty addKey(String key)
    {
        _key.add(key);
        return this;
    }
    public void set_key(String[] key)
    {
        for(int i=0; i< key.length; i++) _key.add(key[i]);
    }
    public void set_key(LinkedHashSet<String> key)
    {
       _key = key;
    }
    public LinkedHashSet<String> get_key(){return _key;}

    public String get_valueKey()
    {
        return  _key.toArray(new String[_key.size()])[_key.size()-1];
    }

    public String get_value() {
        return _value;
    }
    public void set_value(String _value) {
        this._value = _value;
    }
    public void set_encrypted(boolean encrypted) {this._encrypted = encrypted;}
    public boolean is_encrypted() {return _encrypted;}



    public static SecureProperty getSecureProperty(com.google.gson.internal.LinkedTreeMap map)
    {
        List<String> keys = (List) map.get("_key");
        String[] key = new String[keys.size()];
        for(int i=0; i<key.length;i++) key[i] = keys.get(i);
        String value = (String) map.get("_value");
        boolean encrypted = (boolean) map.get("_encrypted");
        SecureProperty secureProperty = createNewSecureProperty(key,value,encrypted);
        secureProperty.set_value(value);
        return secureProperty;
    }

    public boolean isKeyEqual(LinkedHashSet<String> otherKey)
    {
        if(_key == null && otherKey == null) return true;
        if(_key == null) return false;
        if(otherKey==null) return false;
        if(!Sets.symmetricDifference(_key,otherKey).isEmpty()) return false;
        String[] a_array = _key.toArray(new String[_key.size()]);
        String[] b_array = otherKey.toArray(new String[otherKey.size()]);
        for(int i=0; i< a_array.length; i++)
        {
            if(!a_array[i].equalsIgnoreCase(b_array[i])) return false;
        }
        return true;
    }

    public boolean isKeyEqual(String[] otherKey)
    {
        LinkedHashSet<String> key = new LinkedHashSet<>();
       for(int i=0; i< otherKey.length; i++) key.add(otherKey[i]);
       return  isKeyEqual(key);

    }

    public boolean isSubKeyOf(LinkedHashSet<String> otherKey)
    {
        if(_key.isEmpty()) return false;
        if(otherKey.isEmpty()) return true;
        if(_key.size() == 1 && otherKey.size()==1 && otherKey.contains("")) return true;
        if(otherKey.size() >= _key.size()) return false;
        String[] key_array = _key.toArray(new String[_key.size()]);
        String[] other_key_array = otherKey.toArray(new String[otherKey.size()]);

        for(int i=0; i< other_key_array.length; i++)
        {
            if(!key_array[i].equalsIgnoreCase(other_key_array[i])) return false;
        }
        return true;
    }

    public boolean isChildOf(LinkedHashSet<String> otherKey)
    {
        if(_key.isEmpty()) return false;
        if(otherKey.isEmpty()) return true;
        if(otherKey.size() +2 != _key.size()) return false;
        String[] key_array = _key.toArray(new String[_key.size()]);
        String[] other_key_array = otherKey.toArray(new String[otherKey.size()]);
        for(int i=0; i< other_key_array.length; i++)
        {
            if(!key_array[i].equalsIgnoreCase(other_key_array[i])) return false;
        }
        return true;
    }

    public SecureProperty copy()
    {
        SecureProperty secureProperty = new SecureProperty();
        secureProperty.set_value(get_value());
        secureProperty.set_key(get_key());
        secureProperty.set_encrypted(is_encrypted());
        return secureProperty;
    }

    @Override
    public String toString() {
        return "SecureProperty{" +
                ", _key='" + _key + '\'' +
                ", _value='" + _value + '\'' +
                ", _encrypted=" + _encrypted +
                '}';
    }

    @Override
    public int compareTo(SecureProperty o)
    {
       return SecureProperty.createKeyWithSeparator(get_key()).compareTo(SecureProperty.createKeyWithSeparator(o.get_key()));

    }
}
