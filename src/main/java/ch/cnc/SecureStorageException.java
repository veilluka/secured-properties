package ch.cnc;

public class SecureStorageException extends Exception {

    public static String FILE_NOT_EXISTS="COULD NOT FIND THE FILE";
    public static String FILE_EXISTS_ALREADY="Secured file exists allready!";
    public static String NO_MASTER_KEY = "provided file has no protected master key";
    public static String WINDOWS_CHECK_KEY_MISSING="Can not open with windows, key check is missing in file";
    public static String WINDOWS_ENCRYPTED_WITH_OTHER_USER = "Can not open with windows, file has been encrypted with other user";
    public static String PASSWORD_NOT_CORRECT="password does not match";
    public static String MASTER_KEY_NOT_SET="MASTER_KEY_NOT_SET";
    public static String NOT_WINDOWS_SUPPORTED="Can not open without password, windows is not supported";
    public static String SECURE_MODE_NOT_ON="can not read or add secured property, storage opened in not secured mode";
    public static String CSV_LABEL_KEY_MISSING = "labelKey is defined, but has no value in record";
    public static String CSV_KEY_NOT_UNIQUE = "found same key in different records, key must be unique";
    public static String CSV_LABEL_VALUE_MISSING = "labelKey is defined, but has no value in record";
    public static String PASSWORD_TO_SHORT = "Password is to short, please at least 8 chars";


    public SecureStorageException(String message)
    {
        super(message);
    }



}
