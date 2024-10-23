import ch.vilki.secured.Console;

import java.io.IOException;
import java.util.LinkedHashSet;

public class RunGUI {

    public static void main(String args[]) throws IOException {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        linkedHashSet.add("-gui");
        run(linkedHashSet);
    }



    public static void run(LinkedHashSet<String> arguments) throws IOException {
        String[] args = arguments.toArray(new String[arguments.size()]);

        Console.main(args);
    }

}
