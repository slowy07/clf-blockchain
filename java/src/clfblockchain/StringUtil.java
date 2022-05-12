package clfblockchain;

import java.security.MessageDigest;
import com.google.gson.GsonBuilder;

public class StringUtil {
    public static applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        }
        catch (Exception error) {
            throw new RuntimeException(error);
        }

        public static String getJson(Object o) {
            return new GsonBuilder().setPrettyPrinting().create().toJson(o);
        }

        public static String getDificultyString(int difficulty) {
            return new String(new char[difficulty]).replace('\0', '0');
        }
    }
}
