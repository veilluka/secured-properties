import ch.vilki.secured.SecureProperties;
import ch.vilki.secured.SecureProperty;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;



public class TestSecProperties {


    @Test
    public void a_testProp()
    {
        SecureProperties secureProperties = new SecureProperties();
        String[] key1 = new String[]{"one","two"};
        String[] key2 = new String[]{"two","one"};
        SecureProperty k1k2 = SecureProperty.createNewSecureProperty(key1,"K1K2",false);
        SecureProperty k2k1 = SecureProperty.createNewSecureProperty(key2,"K2K1",false);

        secureProperties.addProperty(k1k2);
        secureProperties.addProperty(k2k1);

        SecureProperty k1_result = secureProperties.getProperty(key1);
        SecureProperty k2_result = secureProperties.getProperty(key2);
        assertNotEquals(null,k1_result);
        assertNotEquals(null,k2_result);

        assertEquals(k1_result.get_value(),"K1K2");
        assertEquals(k2_result.get_value(),"K2K1");
    }

    @Test
    public void b_testProp()
    {
        SecureProperties secureProperties = new SecureProperties();
        String[] key1 = new String[]{"one","two"};
        String[] key2 = new String[]{"two","one"};
        String[] key3 = new String[]{"b1","b2","b3"};
        String[] key4 = new String[]{"two"};


        secureProperties.addStringProperty(key1,"XDEPPO",false);
        secureProperties.addStringProperty(key2,"%F*DFGD",false);
        secureProperties.addBooleanProperty(key3,true);

        assertEquals(secureProperties.getStringProperty(key1),"XDEPPO");
        assertEquals(secureProperties.getStringProperty(key2),"%F*DFGD");
        assertEquals(secureProperties.getBooleanValue(key3),true);
        assertEquals(secureProperties.getStringProperty(key4),null);

    }

    @Test
    public void c_testProp()
    {
        SecureProperties secureProperties = new SecureProperties();
        String[] key1 = new String[]{"one","two"};

        secureProperties.addStringProperty(key1,"XDEPPO",false);
        assertEquals(secureProperties.getStringProperty(key1),"XDEPPO");

        secureProperties.addStringProperty(key1,"FGDEEDGF",false);
        assertEquals(secureProperties.getStringProperty(key1),"FGDEEDGF");

    }

    @Test
    public void e_testProp()
    {
        createTestDir();
        if(Files.exists(Paths.get("test//storage4.json"))) {
            try {
                Files.delete(Paths.get("test//storage4.json"));
            } catch (IOException e) {
                fail(e);
            }
        }
        LinkedHashSet<String> a = new LinkedHashSet<>();
        a.add("one");
        a.add("two");
        LinkedHashSet<String> b = new LinkedHashSet<>();
        b.add("one");
        Sets.SetView<String> symetric = Sets.symmetricDifference(a,b);
        Sets.SetView<String> non = Sets.difference(a,b);

        Path fileName = Paths.get("test//storage4.json");
        try {
            if(Files.exists(fileName))
            Files.delete(fileName);
        } catch (IOException e) {
            fail(e);
        }
        SecureProperties secureProperties = null;
        try {
            secureProperties = SecureProperties.createSecuredProperties(fileName.toString());
        } catch (Exception e) {
            fail(e);
        }
        String[] key1 = new String[]{"one","two"};
        String[] key2 = new String[]{"two","one"};
        String[] key3 = new String[]{"one"};
        String[] key4 = new String[]{"seven","six","eight"};

        secureProperties.addStringProperty(key1,"key1",false);
        secureProperties.addStringProperty(key2,"key2",false);
        secureProperties.addStringProperty(key3,"key3",false);
        secureProperties.addStringProperty(key4,"key4",false);

        secureProperties.saveProperties();
        secureProperties = null;
        try {
            secureProperties = SecureProperties.openSecuredProperties(fileName.toString(), true);
        } catch (Exception e) {
            fail(e);
        }
        String val = secureProperties.getStringProperty(key1);

        assertEquals(secureProperties.getStringProperty(key1),"key1");
        assertEquals(secureProperties.getStringProperty(key2),"key2");
        assertEquals(secureProperties.getStringProperty(key3),"key3");
        assertEquals(secureProperties.getStringProperty(key4),"key4");
    }

    @Test
    public void f_testProp()
    {
        SecureProperties secureProperties = new SecureProperties();
        String key1 ="connection.dirx.migration.user";
        String key2 = "connection.dirx.migration.password";



        SecureProperty k1k2 = SecureProperty.createNewSecureProperty(SecureProperty.parseKey(key1),"vedran",false);
        SecureProperty k2k1 = SecureProperty.createNewSecureProperty(SecureProperty.parseKey(key2),"noYouCouldNot",false);

        secureProperties.addProperty(k1k2);
        secureProperties.addProperty(k2k1);

        SecureProperty k1_result = secureProperties.getProperty(SecureProperty.parseKey(key1));
        SecureProperty k2_result = secureProperties.getProperty(SecureProperty.parseKey(key2));
        assertNotEquals(null,k1_result);
        assertNotEquals(null,k2_result);

        assertEquals(k1_result.get_value(),"vedran");
        assertEquals(k2_result.get_value(),"noYouCouldNot");
    }


    private void createTestDir()
    {
        if(!Files.exists(Paths.get("test"))) {
            try {
                Files.createDirectory(Paths.get("test"));

            } catch (IOException e) {
                fail(e);
            }
        }
    }

}
