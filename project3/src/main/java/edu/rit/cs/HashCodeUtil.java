package edu.rit.cs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class HashCodeUtil {

    private final static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getFileMD5( String fileData) {
        String fileHash = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(fileData.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            fileHash = sb.toString();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return fileHash;
    }

    public static boolean checkHash(String fileHash){
        boolean cond1 = (fileHash.length() == 32);
        if (!cond1){
            return false;
        }
        char[] hashCodes = fileHash.toCharArray();
        String hexDigitsStr = new String(hexDigits, 0, 16);
        boolean isValid = true;
        for (int i=0; i<32; i++){
            if(!hexDigitsStr.contains(Character.toString(hashCodes[i]))){
                isValid = false;
            }
        }
        return isValid;
    }

}