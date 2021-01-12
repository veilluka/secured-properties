import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

public class Helper {

    public static String TEST_DIR="test";

    public static  void createTestDir()
    {
        if(!Files.exists(Paths.get(TEST_DIR))) {
            try {
                Files.createDirectory(Paths.get("test"));

            } catch (IOException e) {
                fail(e);
            }
        }
    }


    public static void deleteFile(String fileName) {
        if(Files.exists(Paths.get(fileName)))
        {
            try {
                Files.delete(Paths.get(fileName));
            } catch (IOException e) {
                fail(e);
            }
        }

    }
}
