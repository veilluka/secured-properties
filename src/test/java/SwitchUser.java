import ch.cnc.Console;
import ch.cnc.SecStorage;
import ch.cnc.SecureStorageException;
import ch.cnc.SecureString;
import com.github.windpapi4j.InitializationFailedException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.fail;

public class SwitchUser {


    public static void main(String args[])
    {
        SwitchUser switchUser = new SwitchUser();
        switchUser.print();
    }

    public void print()
    {
        LinkedHashSet<String> arguments = new LinkedHashSet<>();
        arguments.add("-print");
        arguments.add("D:\\TMP\\secure_user_one");

        /*
        try {
            Console.run(arguments);
        } catch (Exception e) {

            System.err.println("Exception" + e.getMessage());
        }
        */
    }
    public void recrypt()
    {
        LinkedHashSet<String> arguments = new LinkedHashSet<>();
        arguments.add("-recrypt");
        arguments.add("D:\\TMP\\secure_user.json");
        arguments.add("-pass");
        arguments.add("582LOJfgu444kkofd");
        try {
          //  Console.run(arguments);
        } catch (Exception e) {

            System.err.println("Exception" + e.getMessage());
        }
    }



    @Test
    public void openFileFromOtherUser()
    {
        try {

            SecStorage secStorage =
                    SecStorage.open_SecuredStorage("D:\\TMP\\test.json",true);
            SecureString passValue = secStorage.getPropValue("test@@password");
            System.out.println(passValue.toString());

        } catch (SecureStorageException e) {
            String message = e.getMessage();
          if(message.equalsIgnoreCase(SecureStorageException.WINDOWS_ENCRYPTED_WITH_OTHER_USER))
          {
              System.out.println(SecureStorageException.WINDOWS_ENCRYPTED_WITH_OTHER_USER);
              System.out.println("Open the file with master password, option -pass <masterpassword> " );
          }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InitializationFailedException e) {
            e.printStackTrace();
        }
    }


}
