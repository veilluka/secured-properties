package ch.cnc;

import com.github.windpapi4j.InitializationFailedException;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.cli.*;
import org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.*;

import static ch.cnc.SecStorage.MASTER_PASSWORD_HASH;

public class Console  {
    static Logger logger = LoggerFactory.getLogger(Console.class);

    public static void run(LinkedHashSet<String> arguments)
    {
        String[] args = arguments.toArray(new String[arguments.size()]);
        Console.main(args);
    }
    public static void main(String args[]) {

        if(args.length == 0)
        {
            System.out.println("No input. Use -help for help");
            System.out.println("secured-properties, Bauer Vedran \n"  + Version.VERSION + " \n \n ");
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
            Gui gui = new Gui();
-           try {
                gui.start(null);
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
        if(ConsoleParser.cmd.hasOption("checkPassword"))
        {
            String fileName = ConsoleParser.cmd.getOptionValue("checkPassword");
            if(fileName == null)
            {
                System.err.println("File not provided to recrypt");
                return;
            }
            SecureString pass = null;
            if(ConsoleParser.cmd.hasOption("pass")) pass = new SecureString(ConsoleParser.cmd.getOptionValue("pass"));
            else
            {
                System.err.println("Master password not provided, can not check");
                return;
            }
            try {
                SecStorage storage = SecStorage.open_SecuredStorage(fileName,false);
                String master_key_hash = storage.getPropStringValue(MASTER_PASSWORD_HASH);
                if(master_key_hash != null && master_key_hash.toString() != null)
                {
                    try {
                        if(!Password.check(pass,new SecureString(master_key_hash)))
                        {
                            System.out.println("NOK");
                        }
                        else {
                            System.out.println("OK");
                        }
                    } catch (Exception e) {
                        logger.error("error checking key ",e);
                    }
                }
                else
                {
                    System.err.println("File does not contain password hash");
                }
            } catch (SecureStorageException | IOException  | InitializationFailedException e) {
                System.err.println("Check failed->" + e.getMessage());
                pass.destroyValue();
            }
            pass.destroyValue();
            return;
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
                SecStorage.open_SecuredStorage(fileName,pass);
            } catch (SecureStorageException | IOException  | InitializationFailedException e) {
               System.err.println("Recrypt failed->" + e.getMessage());
               pass.destroyValue();
               return;
            }
            pass.destroyValue();
             System.out.println("Recrypt SUCCESS!");
             return;
        }
        if(ConsoleParser.cmd.hasOption("secureFile"))
        {
            String fileName = ConsoleParser.cmd.getOptionValue("secureFile");
            if(fileName == null)
            {
                System.err.println("File not provided to be secured");
                return;
            }
             try {
                if(SecStorage.isSecured(fileName))
                {
                    System.out.println("File is secured, check if secured with current user");
                    if(SecStorage.isSecuredWithCurrentUser(fileName))
                    {
                        System.out.println("File secured with current user, try to find unsecured properties in file now");
                        // SECURE HIER
                    }
                    else
                    {
                        System.err.println("File is not secured with current user, recrypt first ");
                        return;
                    }
                }
                else{
                    System.out.println("File is not secured, define master password first");
                }

            } catch (IOException |SecureStorageException  e) {
                 System.out.println("Error securing file" + e.getMessage());
                 e.printStackTrace();
            }
            SecureString pass = null;
            if(ConsoleParser.cmd.hasOption("pass")) pass = new SecureString(ConsoleParser.cmd.getOptionValue("pass"));
            else
            {
                System.err.println("Master password not provided, can not recrypt");
                return;
            }
            try {
                SecStorage.open_SecuredStorage(fileName,pass);
            } catch (SecureStorageException | IOException  | InitializationFailedException e) {
                System.err.println("Recrypt failed->" + e.getMessage());
                pass.destroyValue();
                return;
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
                String footer = "\n Secured-Properties \n (c) 2019 [Bauer Vedran] \n Version: " + Version.VERSION + " \n \n ";

                formatter.printHelp(120,"secureStorage",header,ConsoleParser._options,footer,true);

                return;
            }
            if(ConsoleParser.cmd.hasOption("create"))
            {
                String fileName = ConsoleParser.cmd.getOptionValue("create");
                SecureString pass = null;
                boolean secured = true; ;
                if(ConsoleParser.cmd.hasOption("pass")) pass = new SecureString(ConsoleParser.cmd.getOptionValue("pass"));
                if(ConsoleParser.cmd.hasOption("unsecured")) secured = false;
                if(secured && pass==null)
                {
                    pass = Password.getRandomPassword(12);
                    System.out.println("Using random password "+pass.toString());
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
                    System.out.println(value.toString());
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
        if(!cmd.hasOption("value")) throw new Exception("no value for property provided");
        String val = cmd.getOptionValue("value");
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
