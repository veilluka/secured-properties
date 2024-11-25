package ch.vilki.secured;

import com.github.windpapi4j.InitializationFailedException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static ch.vilki.secured.SecStorage.MASTER_PASSWORD_HASH;

public class Console  {
    static Logger logger = LoggerFactory.getLogger(Console.class);

    public static void run(LinkedHashSet<String> arguments) throws IOException {
        String[] args = arguments.toArray(new String[arguments.size()]);
        Console.main(args);
    }
    public static void main(String[] args) throws IOException {
        if(args.length == 0)
        {
            System.out.println("No input. Use -help for help");
            System.out.println("secured-properties, Bauer Vedran \n"  +  ApplicationVersion.VERSION + " \n \n ");
            return;
        }
        ConsoleParser.initParser();
        try {
            ConsoleParser.parse(args);
        } catch (ParseException e) {
            logger.error("Exception occured in parser",e);
        }
        if(ConsoleParser.cmd.hasOption("gui"))
        {
            Gui g = new Gui();
            try {
                g.start(null);
            } catch (Exception e1) {
                logger.error("error showing gui",e1);
                System.exit(0);
            }
            return;
        }
        if(ConsoleParser.cmd.hasOption("print"))
        {
            SecStorage secStorage = null;
            String fileName = ConsoleParser.cmd.getOptionValue("print");
            System.out.println("\n \nPrinting content of the file->" + fileName + "\n");
            if(isFileSecuredWithCurrentUser(fileName))
            {
                System.out.println("File is WIN-secured with current user, can be open in protected mode without password");
             }
            else
            {
                System.out.println("File is not secured with current user, can not be open in protected mode without password");
            }
            System.out.println(System.lineSeparator());
            try {
                secStorage = SecStorage.open_SecuredStorage(fileName,false);
            }catch (SecureStorageException | IOException | InitializationFailedException e) {
                System.err.println("Can not print ->" + e.getMessage());
            }

            Set<String> allLabels = secStorage.getAllLabels();
            for(String label: allLabels)
            {
                Map<String, String> map = secStorage.getAllPropertiesAsMap(label);
                System.out.println("-------------------" + label + "---------------");
                map.keySet().stream().forEach(x->{
                    System.out.println(x + "=" + map.get(x));
                });
            }
            System.out.println("----------------------------------");
            return;
        }
        if(ConsoleParser.cmd.hasOption("generatePassword"))
        {
            System.out.println(new SecureString(new Enc().generatePassword(6,6,6,6)));
            System.exit(0);
        }

        if(ConsoleParser.cmd.hasOption("recrypt"))
        {
            String fileName = ConsoleParser.cmd.getOptionValue("recrypt");
            if(fileName == null)
            {
                System.err.println("File not provided to recrypt");
                return;
            }
            SecureString pass = null;
            if(ConsoleParser.cmd.hasOption("pass")) pass = new SecureString(ConsoleParser.cmd.getOptionValue("pass"));
            else
            {
                System.err.println("Master password not provided, can not recrypt");
                return;
            }
             try {
                SecStorage.recryptSecuredStorage(fileName,pass);
            } catch (Exception e) {
               System.err.println("Recrypt failed->" + e.getMessage());
               pass.destroyValue();
            }
            pass.destroyValue();
             System.out.println("Recrypt SUCCESS!");
             return;
        }
        try
        {
            if(ConsoleParser.cmd.hasOption("help"))
            {
                HelpFormatter formatter = new HelpFormatter();
                String header = "first argument after every command is allways the full path of the file! \n \n Example: \n " +
                        "secured-properties  -addSecured c:\\users\\foo\\secFile -key user -value john \n \n ";
                String footer = "\n Secured-Properties \n (c) 2019 [Bauer Vedran] \n Version: " + ApplicationVersion.VERSION + " \n \n ";

                formatter.printHelp(120,"secureStorage",header,ConsoleParser._options,footer,true);

                return;
            }
            if(ConsoleParser.cmd.hasOption("create"))
            {
                if(ConsoleParser.cmd.hasOption("pass"))
                {
                    System.out.println("INFO: You have provided your own password. Please avoid using AND or $ ir ; or | char in your password, " +
                            " as it causes problems in different enviroments when scripting. use rather longer password with more then 20 chars ");
                }

                String fileName = ConsoleParser.cmd.getOptionValue("create");
                SecureString pass = null;
                boolean secured = true; ;
                if(ConsoleParser.cmd.hasOption("pass")) pass = new SecureString(ConsoleParser.cmd.getOptionValue("pass"));
                if(ConsoleParser.cmd.hasOption("unsecured")) secured = false;
                if(secured && pass==null)
                {
                    pass = new SecureString(new Enc().generatePassword(6,6,6,6));
                    System.out.println("Using random password "+ pass);
                }
                SecStorage.createNewSecureStorage(fileName,pass,secured);
                System.out.println("Storage created");
                return;
            }
            if(ConsoleParser.cmd.hasOption("addSecured"))
            {
                addProperty(ConsoleParser.cmd,true);
                return;
            }
            if(ConsoleParser.cmd.hasOption("addUnsecured"))
            {
                addProperty(ConsoleParser.cmd,false);
                return;
            }
            if(ConsoleParser.cmd.hasOption("getValue"))
            {
                SecureString value = getPropertyValue(ConsoleParser.cmd);
                if(value != null && value.get_value() != null)
                {
                    System.out.println(value);
                    value.destroyValue();
                }
                return;
            }
            if(ConsoleParser.cmd.hasOption("delete"))
            {
                deleteProperty(ConsoleParser.cmd);
                return;
            }
        }
        catch (Exception e)
        {
            System.err.println("ERROR->" + e.getMessage());
        }
        System.exit(0);
    }

    public static boolean isFileSecuredWithCurrentUser(String fileName)
    {
        try {
            SecStorage.open_SecuredStorage(fileName,true);
        } catch (SecureStorageException | IOException | InitializationFailedException e) {
            if (e.getMessage().equalsIgnoreCase(SecureStorageException.WINDOWS_ENCRYPTED_WITH_OTHER_USER)) {
               return false;
            }
        }
        return true;
    }


    private static SecureString getPropertyValue(CommandLine cmd) throws Exception {
        String key = null;
        if(!cmd.hasOption("key")) throw new Exception("key for property not provided");
        key = cmd.getOptionValue("key");
        String fileName = ConsoleParser.cmd.getOptionValue("getValue");
        SecureString pass = null;
        if(cmd.hasOption("pass")) pass = new SecureString(cmd.getOptionValue("pass"));
        SecStorage secStorage = null;
        boolean secured = true;
        if(cmd.hasOption("unsecured")) secured = false;
        if(pass != null && pass.get_value() != null)
        {
            secStorage = SecStorage.open_SecuredStorage(fileName,pass);
        }
        else
        {
            secStorage = SecStorage.open_SecuredStorage(fileName,secured);
        }
        if(secStorage != null)
        {
            return secStorage.getPropValue(key);
        }
        return null;

    }

    private static void addProperty(CommandLine cmd, boolean secured) throws Exception {

        String key = null;
        if(!cmd.hasOption("key")) throw new Exception("key for property not provided");
        key = cmd.getOptionValue("key");
        String val = "";
        if(!cmd.hasOption("value")){
            logger.info("value not provided for this key, generate random value ");
            val = new Enc().generatePassword(6,6,6,6);
        }
        else val = cmd.getOptionValue("value");
        SecureString secureString = new SecureString(val);
        String fileName = cmd.getOptionValue("addSecured");
        if(fileName == null) {fileName = cmd.getOptionValue("addUnsecured");}
        if(fileName == null)
        {
            throw new Exception("file name not provided");
        }

        SecureString pass = null;
        if(cmd.hasOption("pass")) pass = new SecureString(cmd.getOptionValue("pass"));
        SecStorage secStorage = null;
        if(pass != null && pass.get_value() != null)
        {
            secStorage = SecStorage.open_SecuredStorage(fileName,pass);
        }
        else
        {
            secStorage = SecStorage.open_SecuredStorage(fileName,secured);
        }

        if(secured)
        {
            secStorage.addSecuredProperty(key,secureString);
        }
        else
        {
            secStorage.addUnsecuredProperty(key,secureString.toString());
        }
        SecStorage.destroy();
    }

    private static void deleteProperty(CommandLine cmd) throws Exception {

        String key = null;
        if(!cmd.hasOption("key")) throw new Exception("key for property not provided");
        key = cmd.getOptionValue("key");
        String fileName = cmd.getOptionValue("delete");
        if(fileName == null)
        {
            throw new Exception("file name not provided");
        }

        SecureString pass = null;
        if(cmd.hasOption("pass")) pass = new SecureString(cmd.getOptionValue("pass"));
        SecStorage secStorage = null;
        if(pass != null && pass.get_value() != null)
        {
            secStorage = SecStorage.open_SecuredStorage(fileName,pass);
        }
        else
        {
            secStorage = SecStorage.open_SecuredStorage(fileName,false);
        }
        List<SecureProperty> props  = secStorage.getAllProperties(key);
        for(SecureProperty secureProperty: props)
        {
            System.out.println("Deleting property ->" + SecureProperty.createKeyWithSeparator(secureProperty.get_key()));
            secStorage.deleteProperty(SecureProperty.createKeyWithSeparator(secureProperty.get_key()));
        }


        SecStorage.destroy();
    }

}
