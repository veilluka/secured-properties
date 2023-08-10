import ch.vilki.secured.Console;
import ch.vilki.secured.SecStorage;
import ch.vilki.secured.SecureStorageException;
import ch.vilki.secured.SecureString;
import com.github.windpapi4j.InitializationFailedException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

public class CommandLine {

    @Test
    public void print()
    {
        LinkedHashSet<String> arguments = new LinkedHashSet<>();
        arguments.add("-print");
        arguments.add("D:\\TMP\\conci-ldap.json");
        try {
            Console.run(arguments);
        } catch (Exception e) {

            fail(e.getMessage());
        }
    }

    @Test
    public void print2()
    {
        LinkedHashSet<String> arguments = new LinkedHashSet<>();
        arguments.add("-print");
        arguments.add("D:\\TMP\\v1.json");
        try {
            Console.run(arguments);
        } catch (Exception e) {

            fail(e.getMessage());
        }
    }


    @Test
    public void createStorageWithPassword()
    {
        Helper.createTestDir();
        String fileName = "test//cmdCreate1.json";
        Helper.deleteFile(fileName);
        LinkedHashSet<String> arguments = new LinkedHashSet<>();
        arguments.add("-create");
        arguments.add(fileName);
        arguments.add("-pass");
        arguments.add("ç35$p4343");
        try {
            Console.run(arguments);
        } catch (Exception e) {

            fail(e.getMessage());
        }
    }
    @Test
    public void createWindowsStorageWithoutPassword()
    {
        Helper.createTestDir();
        String fileName = "test//cmdCreate2.json";
        Helper.deleteFile(fileName);
        LinkedHashSet<String> arguments = new LinkedHashSet<>();
        arguments.add("-create");
        arguments.add(fileName);
        arguments.add("-unsecured");

        try {
            Console.run(arguments);
        } catch (Exception e) {

            fail(e.getMessage());
        }
    }
    @Test
    public void showHelp()
    {
        String ar[] = new String[1];
        ar[0] = "-help";
        try {
            Console.main(ar);
        } catch (Exception e) {

            fail(e.getMessage());
        }
    }


    @Test
    public void addSecuredPropertyWindows()
    {
        Helper.createTestDir();
        String fileName = "test//cmdCreate3.json";
        Helper.deleteFile(fileName);
        LinkedHashSet<String> arguments = new LinkedHashSet<>();
        arguments.add("-create");
        arguments.add(fileName);
        arguments.add("-pass");
        arguments.add("ç35$p4343");
        try {
            Console.run(arguments);
        } catch (Exception e) {

            fail(e.getMessage());
        }
        arguments.clear();
        arguments.add("-addUnsecured");
        arguments.add(fileName);
        arguments.add("-key");
        arguments.add("global@@ip");
        arguments.add("-value");
        arguments.add("192.168.2.55");
        try {
            Console.run(arguments);
        } catch (Exception e) {
        fail("adding secured value failed",e);
        }
        arguments.clear();
        arguments.add("-addSecured");
        arguments.add(fileName);
        arguments.add("-key");
        arguments.add("global@@password");
        arguments.add("-value");
        arguments.add("Öäe!*554BAU4");
        try {
            Console.run(arguments);
        } catch (Exception e) {
            fail(e);
        }
        arguments.clear();
        arguments.add("-getValue");
        arguments.add(fileName);
        arguments.add("-key");
        arguments.add("global@@password");

        try {
            System.out.println("Value from storage is->");
            Console.run(arguments);
        } catch (Exception e) {
            fail(e);
        }
        arguments.clear();
        arguments.add("-getValue");
        arguments.add(fileName);
        arguments.add("-key");
        arguments.add("global@@ip");

        try {
            System.out.println("Value from storage is->");
            Console.run(arguments);
        } catch (Exception e) {
            fail(e);
        }
        SecStorage secStorage = null;
        try {
            secStorage = SecStorage.open_SecuredStorage(fileName,true);
        } catch (SecureStorageException e) {
            fail(e);
        } catch (IOException e) {
           fail(e);
        } catch (InitializationFailedException e) {
            fail(e);
        }
        SecureString pass = secStorage.getPropValue("global@@password");
        assertNotNull(pass);
        assertNotNull(pass.get_value());
        assertEquals(pass.toString(),"Öäe!*554BAU4");

    }


}
