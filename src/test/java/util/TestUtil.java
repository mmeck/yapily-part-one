package util;

import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Timestamp;

public class TestUtil {
    public static String generateHash(String ts, String privateKey, String publicKey) {
        String stringToHash = ts + privateKey + publicKey;
        return DigestUtils.md5Hex(stringToHash);
    }

    public static String timeStamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return String.valueOf(timestamp.getTime());
    }
}
