package ch.vilki.secured;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class SecureString {

    public char[] get_value() {
        return _value;
    }

    private char[] _value = null;

    public SecureString(String input)
    {
        if(input == null || input.equalsIgnoreCase(""))
        {
            _value = null;
            return;
        }
        _value = new char[input.length()];
        input.getChars(0,input.length(), _value,0);
    }

    public SecureString(char[] input){_value = input;}
    public SecureString(CharBuffer charBuffer)
    {
        if(charBuffer == null || charBuffer.length() == 0) _value = null;
        _value = cleanInvalidCharacters(charBuffer.array());
    }

    public void destroyValue()
    {
        if(_value == null) return;
        for(int i=0; i< _value.length;i++) _value[i]= '*';
        _value = null;
    }


    public byte[] getBytes(Charset utf8)
    {
       CharBuffer charBuffer = CharBuffer.wrap(_value);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    @Override
    public String toString() {
        if(_value == null) return null;
        return new String(_value);
    }

    public String cleanInvalidCharacters(String in) {
        StringBuilder out = new StringBuilder();
        char current;
        if (in == null || ("".equals(in))) {
            return "";
        }
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i);
            if ((current == 0x9)
                    || (current == 0xA)
                    || (current == 0xD)
                    || ((current >= 0x20) && (current <= 0xD7FF))
                    || ((current >= 0xE000) && (current <= 0xFFFD))
                    || ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
            }

        }
        return out.toString().replaceAll("\\s", " ");
    }

    public char[] cleanInvalidCharacters(char[] in) {
        StringBuilder out = new StringBuilder();
        char current;

        if (in == null || ("".equals(in))) {
            return in;
        }
        for (int i = 0; i < in.length; i++) {
            current = in[i];
            if ((current == 0x9)
                    || (current == 0xA)
                    || (current == 0xD)
                    || ((current >= 0x20) && (current <= 0xD7FF))
                    || ((current >= 0xE000) && (current <= 0xFFFD))
                    || ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
            }

        }
        char[] newChars = new char[out.length()];
        out.getChars(0,out.length(),newChars,0);
        return newChars;

    }



    public SecureString copy()
    {
        SecureString secureString = new SecureString(get_value());
        return secureString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o.getClass() == String.class)
        {
            String other = (String) o;
            return Arrays.equals(_value, other.toCharArray());
        }
        if (o == null || getClass() != o.getClass()) return false;
        SecureString that = (SecureString) o;
        return Arrays.equals(_value, that._value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_value);
    }
}
