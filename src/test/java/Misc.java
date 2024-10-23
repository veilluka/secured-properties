import ch.vilki.secured.Console;

import java.io.IOException;
import java.util.LinkedHashSet;

public class Misc {


    public static void main(String args[]) throws IOException {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        /*
        linkedHashSet.add("-create");
        linkedHashSet.add("test");
        linkedHashSet.add("-pass");
        linkedHashSet.add("test1234");
        */
        linkedHashSet.add("-print");
        linkedHashSet.add("C:\\data\\javaWorkSpace\\secured-properties\\build\\install\\secured-properties\\bin\\test.properties");
        //linkedHashSet.add("-pass");
        //linkedHashSet.add("test1234");

        run(linkedHashSet);
    }



    public static void run(LinkedHashSet<String> arguments) throws IOException {
        String[] args = arguments.toArray(new String[arguments.size()]);

        Console.main(args);
    }


}
