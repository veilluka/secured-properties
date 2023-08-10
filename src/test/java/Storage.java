import ch.vilki.secured.SecStorage;
import ch.vilki.secured.SecureProperty;
import ch.vilki.secured.SecureStorageException;
import ch.vilki.secured.SecureString;
import com.github.windpapi4j.InitializationFailedException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class Storage {

    private String  RANDOM_STRING="Where symbolic links are supported, but the underlying FileStore does not " +
            "support symbolic links, then this may fail with an IOException. Additionally, some operating systems " +
            "may require that the Java virtual machine be started with implementation specific privileges to " +
            "create symbolic links, in which case this method may throw IOException.";

    @Test
    public void testStorageCreation()
    {
        Helper.createTestDir();
        if(Files.exists(Paths.get("test//storage1.json"))) {
            try {
                Files.delete(Paths.get("test//storage1.json"));
            } catch (IOException e) {
                fail(e);
            }
        }

        try {
            SecStorage.createNewSecureStorage("test//storage1.json",new SecureString("du*=)889ADG"),true);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testOpenUnsecured() throws NoSuchAlgorithmException, InvalidKeySpecException, SecureStorageException, IOException {
        Helper.createTestDir();
        if(Files.exists(Paths.get("test//unsecured.json"))) {
            try {
                Files.delete(Paths.get("test//unsecured.json"));
            } catch (IOException e) {
                fail(e);
            }
        }

        SecStorage.createNewSecureStorage("test//unsecured.json",null,false);
        try {
            if(SecStorage.isSecured("test//unsecured.json"))
            {
                SecStorage secStorage = SecStorage.open_SecuredStorage("test//unsecured.json",true);
                secStorage.getPropValue("foo");
            }
            else
            {
                SecStorage secStorage = SecStorage.open_SecuredStorage("test//unsecured.json",false);
                secStorage.getPropValue("foo");
                secStorage.addUnsecuredProperty("bam@@foo","testUnsecured");
            }

        } catch (IOException e) {
            fail(e);
        } catch (InitializationFailedException e) {
            fail(e);
        }
    }


    @Test
    public void testOpenStorage()
    {
        Helper.createTestDir();
        if(Files.exists(Paths.get("test//storage2.json"))) {
            try {
                Files.delete(Paths.get("test//storage2.json"));
            } catch (IOException e) {
                fail(e);
            }
        }
        try {
            SecStorage.createNewSecureStorage("test//storage2.json",new SecureString("du*=)889ADG"),true);
        } catch (Exception e) {
            fail(e);
        }
        SecStorage secStorage = null;
        try {
            secStorage = SecStorage.open_SecuredStorage("test//storage2.json",true);

        } catch (Exception e) {
            fail(e);
        }
        if(!secStorage.is_secureMode())
        {
            fail("Secure mode not on windows");
        }
        try {
            secStorage = SecStorage.open_SecuredStorage("test//storage2.json",new SecureString("du*=)889ADG"));
        } catch (Exception e) {
            fail(e);
        }
        if(!secStorage.is_secureMode())
        {
            fail("Secure mode not on windows");
        }
    }


    @Test
    public void addAndReadValues()
    {
        Helper.createTestDir();
        if(Files.exists(Paths.get("test//storage3.json"))) {
            try {
                Files.delete(Paths.get("test//storage3.json"));
            } catch (IOException e) {
                fail(e);
            }
        }
        try {
            SecStorage.createNewSecureStorage("test//storage3.json",new SecureString("du*=)889ADG"),true);
        } catch (Exception e) {
            fail(e);
        }
        SecStorage secStorage = null;
        try {
            secStorage = SecStorage.open_SecuredStorage("test//storage3.json",true);
        } catch (Exception e) {
            fail(e);
        }
        Random random = new Random();
        String[] testKeys = new String[]{"key1","key2","key3","key4","key5","key6","key7"};
        HashMap<String,String> testValues1 = new HashMap<>();
        HashMap<String,String> testValues2 = new HashMap<>();
        for(int i=0; i< testKeys.length; i++)
        {
            StringBuilder key = new StringBuilder();
            for(int j=0; j< i+1; j++)
            {

                key.append(testKeys[j]);
                key.append("@@");
            }
            key.deleteCharAt(key.length()-1);
            int n = random.nextInt(RANDOM_STRING.length() -11);
            String value = RANDOM_STRING.substring(n,n+10);
            testValues1.put(key.toString(),value);
        }
        List<String> list = Arrays.asList(testKeys);
        Collections.reverse(list);
        String[] testKeys2 = (String[]) list.toArray();
        for(int i=0; i< testKeys2.length; i++)
        {
            StringBuilder key = new StringBuilder();
            for(int j=0; j< i+1; j++)
            {

                key.append(testKeys2[j]);
                key.append("@@");
            }
            key.deleteCharAt(key.length()-1);
            int n = random.nextInt(RANDOM_STRING.length() -11);
            String value = RANDOM_STRING.substring(n,n+10);
            testValues2.put(key.toString(),value);
        }
        boolean secured = false;
        for(String key: testValues1.keySet())
        {
            if(secured)
            {
                try {
                    secStorage.addSecuredProperty(key,new SecureString(testValues1.get(key)));
                } catch (Exception e) {
                    fail(e);
                }
                secured = false;
                continue;
            }
            else
            {
                try {
                    secStorage.addUnsecuredProperty(key,testValues1.get(key));
                } catch (Exception e) {
                    fail(e);
                }
                secured = true;

            }
        }
        for(String key: testValues2.keySet())
        {
            if(secured)
            {
                try {
                    secStorage.addSecuredProperty(key,new SecureString(testValues2.get(key)));
                } catch (Exception e) {
                    fail(e);
                }
                secured = false;
                continue;
            }
            else
            {
                try {
                    secStorage.addUnsecuredProperty(key,testValues2.get(key));
                } catch (Exception e) {
                    fail(e);
                }
                secured = true;

            }
        }
        for(String key: testValues1.keySet())
        {
            String value = secStorage.getPropStringValue(key);
            assertEquals(value,testValues1.get(key));
        }

        List<SecureProperty> ret = secStorage.getAllProperties("key1@@key2");
        for(SecureProperty secureProperty: ret)
        {
            System.out.println(secureProperty.toString());
        }


    }
}
