package ch.cnc;

import com.github.windpapi4j.InitializationFailedException;
import com.github.windpapi4j.WinAPICallFailedException;
import com.github.windpapi4j.WinDPAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


public class SecStorage {

    static Logger logger = LoggerFactory.getLogger(SecStorage.class);
    public  static String TEST_STRING = "STORAGE@@WINDOWS_SECURED";
    public  static String MASTER_PASSWORD_WIN_SECURED = "STORAGE@@MASTER_PASSWORD_WINDOWS_SECURED";
    public  static String MASTER_PASSWORD_HASH= "STORAGE@@MASTER_PASSWORD_HASH";
    WinDPAPI _winDPAPI = null;
    private static SecStorage _storage = null;
    SecureProperties _secureProperties = null;
    private SecureString _masterPassword = null;
    private static SecStorage get_storage() {return _storage;}
    public boolean is_secureMode() {
        return _secureMode;
    }
    private boolean _secureMode = false;


    public static void destroy()
    {
        if(_storage._masterPassword != null)  _storage._masterPassword.destroyValue();
        _storage._winDPAPI = null;

        _storage._secureMode=false;
        _storage = null;
    }

    public static void  createNewSecureStorage(String fileName,SecureString masterPassword, boolean createSecured)
            throws SecureStorageException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        SecureString mPassword = null;
        if(masterPassword==null || masterPassword.get_value() == null)
        {
            mPassword = Password.getRandomPassword(12);
        }
        else mPassword = masterPassword;
        if(createSecured && (mPassword == null || mPassword.get_value() == null || mPassword.get_value().length < 8))
        {
            throw new SecureStorageException(SecureStorageException.PASSWORD_TO_SHORT);
        }

        boolean windPapiSupported = WinDPAPI.isPlatformSupported();
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.createSecuredProperties(fileName);
        if(!createSecured) return;
        if (windPapiSupported) {
           logger.info("Using windows encryption to secure storage");
            try {
                _storage._winDPAPI = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
                _storage.addWindowsCheck(mPassword);
                String saltedHash = Password.getSaltedHash(mPassword.get_value());
                _storage.addUnsecuredProperty(MASTER_PASSWORD_HASH , saltedHash);
            } catch (Exception e) {
                logger.error("Exception during creation of secure storage", e);
            }
        }
        else
        {
             String saltedHash = Password.getSaltedHash(mPassword.get_value());
            _storage.addUnsecuredProperty(MASTER_PASSWORD_HASH , saltedHash);
        }
        secureEntries();
    }


    public static void createNewSecureStorageFromCSVFile(String csvFile,String labelAttribute, String labelKey, String[] attributesEncrypted, SecureString masterPassword, String storageFile) throws SecureStorageException, NoSuchAlgorithmException, IOException, InitializationFailedException, InvalidKeySpecException {

        CsvReader csvReader = new CsvReader();
        csvReader.readCSVFile(csvFile,"utf-8");
        ArrayList<HashMap<String, ArrayList<String>>> parsedEntries = csvReader.getParsedEntries();
        HashSet<String> keys = new HashSet<>();
        for(HashMap<String, ArrayList<String>> entry: parsedEntries)
        {
            StringBuilder uniqueKey = new StringBuilder();
            if(labelAttribute != null)
            {
                if(entry.get(labelAttribute) != null) uniqueKey.append(entry.get(labelAttribute).get(0));
            }
            if(labelKey != null)
            {
                if(entry.get(labelKey) == null )   throw new SecureStorageException(SecureStorageException.CSV_LABEL_KEY_MISSING);
                uniqueKey.append("@@");
                uniqueKey.append(entry.get(labelKey).get(0));
            }
            if(keys.contains(uniqueKey.toString())) throw new SecureStorageException(SecureStorageException.CSV_KEY_NOT_UNIQUE);
            keys.add(uniqueKey.toString());

        }
        boolean createSecured = true;
        if(masterPassword == null || masterPassword.get_value() == null)
        {
            createSecured = false;
            SecStorage.createNewSecureStorage(storageFile,masterPassword,createSecured);
        }
        else
        {
            SecStorage.createNewSecureStorage(storageFile,masterPassword,createSecured);
        }
        SecStorage secStorage = null;
        if(createSecured)
        {
            secStorage = SecStorage.open_SecuredStorage(storageFile,masterPassword);
        }
        else
        {
            SecStorage.open_SecuredStorage(storageFile,createSecured);
        }

        Set<String> encrypted = new HashSet<>();
        if(attributesEncrypted != null && attributesEncrypted.length > 0) for(String s: attributesEncrypted) encrypted.add(s.toLowerCase());
        for(HashMap<String, ArrayList<String>> entry: parsedEntries)
        {
            StringBuilder uniqueKey = new StringBuilder();
            if(labelAttribute != null)
            {
                if(entry.get(labelAttribute) != null) uniqueKey.append(entry.get(labelAttribute).get(0));
            }
            if(labelKey != null)
            {
                if(entry.get(labelKey) == null )   throw new SecureStorageException(SecureStorageException.CSV_LABEL_VALUE_MISSING);
                if(labelAttribute != null) uniqueKey.append("@@");
                uniqueKey.append(entry.get(labelKey).get(0));
            }
            for(String attributeName: entry.keySet())
            {
                if(attributeName.equalsIgnoreCase(labelAttribute)) continue;
                ArrayList<String> values = entry.get(attributeName);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(uniqueKey.toString());
                stringBuilder.append("@@");
                stringBuilder.append(attributeName);
                if(values != null && values.size() > 0)
                {
                    if(encrypted.contains(attributeName))
                    {
                        secStorage.addSecuredProperty(stringBuilder.toString(),new SecureString(values.get(0)));
                    }
                    else
                    {
                        secStorage.addUnsecuredProperty(stringBuilder.toString(),values.get(0));
                    }
                }
            }
        }
    }

    public static boolean isWindowsSupported()
    {
        if(WinDPAPI.isPlatformSupported()) return true;
        return false;
    }

    public static boolean isSecuredWithCurrentUser(String fileName) throws IOException {
        if(!WinDPAPI.isPlatformSupported()) return false;
        _storage = new SecStorage();

        try {
            _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
            _storage._winDPAPI = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
        } catch (InitializationFailedException | SecureStorageException e) {
            logger.error("Exception in isSecuredWithCurrentUser",e);
            return false;
        }
        try
        {
            _storage.checkWindowsSecurity();
        }
        catch (Exception e)
        {
            return false;
        }
       return true;
    }

    public static boolean isSecured(String fileName) throws IOException, SecureStorageException {
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
        return _storage.hasProperty(MASTER_PASSWORD_HASH);
    }

    public static boolean isWindowsSecured(String fileName) throws IOException, SecureStorageException {
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
        return _storage.hasProperty(MASTER_PASSWORD_WIN_SECURED);
    }

    public static SecStorage open_SecuredStorage(String fileName, boolean openSecured) throws SecureStorageException, IOException, InitializationFailedException {
        if(!WinDPAPI.isPlatformSupported() && openSecured)
        {
            throw new SecureStorageException(SecureStorageException.NOT_WINDOWS_SUPPORTED);
        }
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
        if(!openSecured) return _storage;
        SecureString hash = _storage.getPropValue("STORAGE@@MASTER_PASSWORD_HASH");
        if(hash == null || hash.get_value() == null)
        {
            throw new SecureStorageException(SecureStorageException.MASTER_KEY_NOT_SET);
        }
        if(openSecured)
        {
            _storage._winDPAPI = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
            _storage.checkWindowsSecurity();
            _storage._secureMode = true;
            secureEntries();
        }
        return _storage;
    }

    private static void secureEntries()
    {
        List<SecureProperty> secureEntries = new ArrayList<>();
        for(LinkedHashSet<String> entryKey: _storage._secureProperties._map.keySet())
        {
            for(SecureProperty entry: _storage._secureProperties._map.get(entryKey))
            {
                if(entry.get_value() != null && entry.get_value().startsWith("{ENC}"))
                {
                    secureEntries.add(entry);
                }
            }
        }
        for(SecureProperty secureProperty: secureEntries)
        {
            secureProperty.set_value(secureProperty.get_value().replace("{ENC}",""));
            _storage.secure(secureProperty);
        }

    }

    public static SecStorage open_SecuredStorage(String fileName, SecureString masterPassword) throws SecureStorageException, IOException, InitializationFailedException {
        if(masterPassword == null)
        {
            throw new SecureStorageException(SecureStorageException.MASTER_KEY_NOT_SET);
        }
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
        if(!_storage.checkMasterKey(masterPassword)) throw new SecureStorageException(SecureStorageException.PASSWORD_NOT_CORRECT);
        if(WinDPAPI.isPlatformSupported())
        {
            _storage._winDPAPI =  WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
            try {
                _storage.addWindowsCheck(masterPassword);
            } catch (Exception e) {
                logger.warn("Open storage with master password, windows is supported but can not encrypt with windows",e);
                _storage._masterPassword = masterPassword;
                _storage._secureMode = true;
                return _storage;
            }
            _storage._secureMode = true;

        }
        else
        {
            _storage._secureMode = true;
            _storage._masterPassword = masterPassword;
        }
        return _storage;
    }

    public static boolean isPasswordCorrect(String fileName, SecureString masterPassword) throws IOException, SecureStorageException {
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
        if(!_storage.checkMasterKey(masterPassword))
        {
           SecStorage.destroy();
           return false;
        }
        SecStorage.destroy();
        return true;
    }

    public static void changeMasterPassword(String fileName, SecureString currentPassword, SecureString newPassword) throws SecureStorageException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, WinAPICallFailedException {
        if(!Files.exists(Paths.get(fileName))) throw new SecureStorageException(SecureStorageException.FILE_NOT_EXISTS + fileName);
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
        if(!_storage.is_secureMode() && currentPassword == null)
        {
            String saltedHash = Password.getSaltedHash(newPassword.get_value());
            _storage.addUnsecuredProperty(MASTER_PASSWORD_HASH, saltedHash);
            if(WinDPAPI.isPlatformSupported())
            {
                _storage.addWindowsCheck(newPassword);
            }
            return;
        }
        if(!_storage.checkMasterKey(currentPassword))
        {
            throw new SecureStorageException(SecureStorageException.PASSWORD_NOT_CORRECT);
        }
        String saltedHash = Password.getSaltedHash(newPassword.get_value());
        _storage.addUnsecuredProperty(MASTER_PASSWORD_HASH, saltedHash);
        if(WinDPAPI.isPlatformSupported())
        {
            _storage.addWindowsCheck(newPassword);
        }
    }

    public static void secureWithCurrentUser(String fileName, SecureString masterPassword) throws Exception {
        if(!WinDPAPI.isPlatformSupported()) throw new Exception("Can not determine, WinDPAPI is not supported");
        _storage = new SecStorage();
        _storage._secureProperties = SecureProperties.openSecuredProperties(fileName);
        if(!_storage.checkMasterKey(masterPassword))
        {
            throw new Exception("Provided password does not match the one in the file");
        }
        _storage._winDPAPI = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
        byte[] cipherTextBytes = _storage._winDPAPI.protectData(masterPassword.getBytes());
        String encoded2 = Base64.getEncoder().encodeToString(cipherTextBytes);
        SecureProperty secureProperty = SecureProperty.createNewSecureProperty(MASTER_PASSWORD_WIN_SECURED,encoded2,true);
        _storage._secureMode=true;
        _storage.addProperty(secureProperty);
        _storage.addSecuredProperty(_storage.TEST_STRING, new SecureString(_storage.TEST_STRING));
        SecStorage.destroy();
    }

    public boolean checkMasterKey(SecureString masterPassword) {
        SecureString master_key_hash = _storage.getPropValue(MASTER_PASSWORD_HASH);
        if(master_key_hash != null && master_key_hash.toString() != null)
        {
            try {
                if(!Password.check(masterPassword,master_key_hash)) return false;
                return true;
            } catch (Exception e) {
               logger.error("error opening with master key",e);
               return false;
            }
        }
        return false;
    }

    private void checkWindowsSecurity() throws SecureStorageException {
        if(_secureProperties.getProperty(MASTER_PASSWORD_WIN_SECURED) == null)
        {
            throw new SecureStorageException(SecureStorageException.NO_MASTER_KEY);
        }
        SecureProperty encrypted = _secureProperties.getProperty(TEST_STRING) ;
        if(encrypted == null)
        {
            throw new SecureStorageException(SecureStorageException.WINDOWS_CHECK_KEY_MISSING);
        }
        SecureString decrypted = unprotect(encrypted.get_value());
        if(decrypted == null || !decrypted.equals(TEST_STRING))
        {
            throw new SecureStorageException(SecureStorageException.WINDOWS_ENCRYPTED_WITH_OTHER_USER);
        }
    }

    private void addWindowsCheck(SecureString masterPassword) throws InvalidKeySpecException, NoSuchAlgorithmException, WinAPICallFailedException, SecureStorageException {
        String saltedHash = Password.getSaltedHash(masterPassword.get_value());
        byte[] cipherTextBytes = _storage._winDPAPI.protectData(masterPassword.getBytes());

        String encoded2 = Base64.getEncoder().encodeToString(cipherTextBytes);
        SecureProperty secureProperty = SecureProperty.createNewSecureProperty(MASTER_PASSWORD_WIN_SECURED, encoded2, true);
        _storage._secureMode=true;
        _storage.addProperty(secureProperty);
        _storage.addSecuredProperty(TEST_STRING,new SecureString(TEST_STRING));
        _storage.addUnsecuredProperty(MASTER_PASSWORD_HASH, saltedHash);
    }

    public void addProperty(SecureProperty prop )
    {
        _secureProperties.addProperty(prop);
        _secureProperties.saveProperties();
    }

    public List<String> getAllKeys()
    {
        return _secureProperties.getAllKeys();
    }

    public List<SecureProperty> getAllProperties(String key)
    {
        return _secureProperties.getAllProperties(key);
    }

    public Set<String> getAllChildLabels(String key)
    {
        return _secureProperties.getAllChildLabels(key);
    }

    public Set<String> getAllLabels()
    {
        return _secureProperties.getAllLabels();
    }

    public void addProperty(String key, SecureString value, boolean encrypt) throws SecureStorageException {
        if(encrypt && !_secureMode)
        {
            throw new SecureStorageException(SecureStorageException.SECURE_MODE_NOT_ON);
        }
        String addValue = null;
        if(encrypt)
        {
            addValue = protect(value);
            value.destroyValue();
        }
        else
        {
            addValue = value.toString();
        }
        SecureProperty secureProperty = SecureProperty.createNewSecureProperty(key,addValue,encrypt);
        addProperty(secureProperty);
    }

    public SecureProperty secure(SecureProperty secureProperty)
    {
        String secured = protect(new SecureString(secureProperty.get_value()));

        SecureProperty secProp = SecureProperty.createNewSecureProperty(
                SecureProperty.createKeyWithSeparator(secureProperty.get_key()),
                secured,true);

        addProperty(secProp);
        return secProp;
    }

    public SecureProperty unsecure(SecureProperty secureProperty)
    {
        SecureString unprotected = unprotect(secureProperty.get_value());

        SecureProperty secpr = SecureProperty.createNewSecureProperty(
                SecureProperty.createKeyWithSeparator(secureProperty.get_key()),unprotected.toString(),false);
        addProperty(secpr);
        return secpr;
    }

    public void addUnsecuredProperty( String key, String value) throws SecureStorageException {
        addProperty(key,new SecureString(value),false);
    }

    public void addUnsecuredProperty( LinkedHashSet<String> key, String value) throws SecureStorageException {
        addProperty(SecureProperty.createKeyWithSeparator(key),new SecureString(value),false);
    }


    public void addSecuredProperty(String key, SecureString value) throws SecureStorageException {
        addProperty(key,value,true);
    }

    public void addSecuredProperty(LinkedHashSet<String> key, SecureString value) throws SecureStorageException {

        addProperty(SecureProperty.createKeyWithSeparator(key),value,true);
    }


    public SecureProperty getProperty(String key)
    {
        return _secureProperties.getProperty(key);
    }

    public String getPropStringValue(String key)
    {
        try {
            SecureString secureString = getPropValue(key);
            return secureString.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public SecureString unprotect(SecureProperty secureProperty)
    {
        return unprotect(secureProperty.get_value());
    }

    public boolean hasProperty(String key)
    {
        SecureProperty secureProperty = getProperty(key);
        if(secureProperty == null) return false;
        return true;
    }

    public SecureString getPropValue(String key)  {
        SecureProperty secureProperty = getProperty(key);
        if(secureProperty == null) return null;
        if(secureProperty.is_encrypted())
        {
            SecureString secureString = unprotect(secureProperty.get_value());
            return secureString;
        }
        return new SecureString(secureProperty.get_value());
    }

    public Map<String,String> getAllPropertiesAsMap(String key)
    {
        List<SecureProperty> properties = getAllProperties(key);
        Map<String,String> retValue = new HashMap<>();

        for(SecureProperty property : properties)
        {
            retValue.put(property.get_valueKey(), property.get_value());
        }
        return retValue;
    }

    private String protect(SecureString secureString)  {
        if(secureString.get_value() == null) return null;
        if(_winDPAPI == null && _masterPassword != null)
        {
            String encrypted = null;
            try
            {
                encrypted = Password.encrypt(secureString,_masterPassword);
            }
            catch (Exception e)
            {
              logger.error("Exception during encryption",e);
            }
            secureString.destroyValue();
            return encrypted;
        }

        String encrypted  = null;
        SecureString masterPassword = get_masterPassword();
        try {
             encrypted = Password.encrypt(secureString,masterPassword);
        } catch (Exception e) {
            logger.error("Error encrypting",e);
        }
        masterPassword.destroyValue();
        return encrypted;
    }

    private SecureString unprotect(String protectedValue){

        if(_winDPAPI == null && _masterPassword != null)
        {
            SecureString decrypted = null;
            try {
                decrypted = Password.decrypt(protectedValue,_masterPassword);

            } catch (Exception e) {
                logger.error("Error decrypting",e);
            }
            return decrypted;
        }
        else if(_winDPAPI != null && _masterPassword == null)
        {
            SecureString masterPassword = get_masterPassword();
            if(masterPassword==null) return null;
            SecureString decrypted = null;
            try {
                 decrypted = Password.decrypt(protectedValue,masterPassword);
            } catch (Exception e) {
                logger.error("Error decrypting",e);
            }
            masterPassword.destroyValue();
            return decrypted;
        }

        return null;
    }

    private SecureString unprotectWithWindows(String value)
    {
        byte[] base64Decoded = new byte[0];
        try {

            base64Decoded = Base64.getDecoder().decode(value);
            byte[] decryptedBytes = _winDPAPI.unprotectData(base64Decoded);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decryptedBytes);
            CharBuffer buf = StandardCharsets.UTF_8.decode(byteBuffer);
            return new SecureString(buf.array());
        } catch (WinAPICallFailedException e) {
            logger.error("Exception occured",e);
        }
        return null;

    }

    private SecureString get_masterPassword()
    {
        if(_winDPAPI != null && _masterPassword == null)
        {
            SecureProperty protectedMaster = _secureProperties.getProperty(MASTER_PASSWORD_WIN_SECURED);
            if(protectedMaster == null) return null;
            return unprotectWithWindows(protectedMaster.get_value());
        }
        return null;
    }


    public void deleteProperty(SecureProperty secureProperty) throws IOException {
        _secureProperties.deleteProperty(secureProperty);
        _secureProperties.saveProperties();
    }

    public void deleteProperty(String key) {

        SecureProperty secureProperty= _secureProperties.getProperty(key);
        if(secureProperty != null)
        {
            _secureProperties.deleteProperty(secureProperty);
            _secureProperties.saveProperties();
        }
    }
}
