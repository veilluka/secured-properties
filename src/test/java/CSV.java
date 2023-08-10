import ch.vilki.secured.SecureStorageException;
import com.github.windpapi4j.InitializationFailedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.fail;

public class CSV {


    public static void main(String args[]) throws InitializationFailedException, IOException, SecureStorageException, NoSuchAlgorithmException {

        //SecStorage storage = SecStorage.open_SecuredStorage("C:\\tmp\\t5.json",true);
        //String pass = storage.getPropStringValue("pass@@testerver@@ldapconnections");
        //System.out.println(pass);

    }
    /*
    @Test
    public void createFromCSV()
    {
        Helper.createTestDir();
        Helper.deleteFile("D:\\TMP\\connections.json");
        try {
            SecStorage.createNewSecureStorageFromCSVFile
                    ("D:\\TMP\\connections - Kopie.csv","label","name", new String[]{"Password"},
                            new SecureString("helloOne2"),"D:\\TMP\\connections.json");
        } catch (Exception e) {
            fail(e);
        }

        try {

            if(!SecStorage.isSecuredWithCurrentUser("D:\\TMP\\connections.json"))
            {
                fail("not secured with current user");
            }
        } catch (IOException e) {
            fail(e);
        }

        SecStorage secStorage = null;
        try {
            secStorage = SecStorage.open_SecuredStorage("D:\\TMP\\conci-ldap.json",true);
        } catch (Exception e) {
            fail(e);
        }

        Set<String> keys = secStorage.getAllChildLabels("conn");
        secStorage.getAllKeys().stream().forEach(x->System.out.println("KEY =" + x ));

        for(String k: keys)
        {
            List<SecureProperty> props = secStorage.getAllProperties(k);
            for(SecureProperty secureProperty : props)
            {
                System.out.println(secureProperty.toString());
            }
        }

        Map<String, String> allValues = secStorage.getAllPropertiesAsMap("conn@@MIGR_PROV");


        List<SecureProperty> allProperties = secStorage.getAllProperties("connection");
        for(SecureProperty s: allProperties)
        {
            System.out.println(s.toString());
        }
        SecStorage.destroy();

        try {
            secStorage = SecStorage.open_SecuredStorage("d:\\tmp\\test1.json",true);
        } catch (Exception e) {
            fail(e);
        }

    }
    */

}
