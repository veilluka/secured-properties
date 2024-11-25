package ch.vilki.secured;

import org.apache.commons.cli.*;

public class ConsoleParser {

    public static Options _options = new Options();
    public static CommandLine cmd = null;


    public static void parse(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse( _options, args);
    }

    public static void initParser()
    {
        Option help = new Option( "help", "prints help" );
        Option print = new Option( "print",true,"prints content of secure storage");
        print.setArgName("FILENAME");
        Option create = new Option( "create",true, "creates new storage");
        create.setArgName("FILENAME");
        create.setOptionalArg(true);
        Option addSecured = new Option("addSecured",true,"ADD value to file and encrypt it");
        addSecured.setArgName("-key<value> -value<value> -password<value>[if not windows] ");
        addSecured.setOptionalArg(true);

        Option deleteProperty = new Option("delete",true,"Deletes all properties which match label/key");
        deleteProperty.setArgName("FILENAME  -key<value> -value<value> ");
        addSecured.setOptionalArg(true);

        Option addUnsecured = new Option("addUnsecured",true,"adds a property with value not encrypted");
        addUnsecured.setArgName("FILENAME  -key<value> -value<value>  ");
        addUnsecured.setOptionalArg(true);
        Option key = new Option("key",true,"property key ");
        Option value = new Option("value",true,"property value");
        Option pass = new Option( "pass",true, "password for secure storage");
        Option unsecured = new Option( "unsecured",false, "encryption/decryption not used");
        Option getValue = new Option("getValue",true,"reads value from the file");
        Option recrypt = new Option("recrypt",true,"recrypt file with current user");
        recrypt.setArgName("FILENAME");
        recrypt.setOptionalArg(true);
        Option gui = new Option("gui",false,"Run GUI ");
        Option generatePassword = new Option( "generatePassword",false, "generates password with length 24");


        _options.addOption("print",false,"print all values");
        _options.addOption(help);
        _options.addOption(print);
        _options.addOption(create);
        _options.addOption(pass);
        _options.addOption(unsecured);
        _options.addOption(addSecured);
        _options.addOption(addUnsecured);
        _options.addOption(key);
        _options.addOption(value);
        _options.addOption(getValue);
        _options.addOption(recrypt);
        _options.addOption(deleteProperty);
        _options.addOption(gui);
        _options.addOption(generatePassword);
    }


    public void printExamples()
    {

        String create = "secured-properties -create testStorage -pass mySECRET";
        String add1 = "secured-properties -addUnsecured testStorage -key user -value admin";
        String add2 = "secured-properties -addSecured testStorage -key password -value sEcRETString";
        String print = "secured-properties -print testStorage";


    }


}
