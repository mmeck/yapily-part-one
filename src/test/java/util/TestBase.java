package util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestBase {
    private static RequestSpecification requestSpec;
    private static String MARVEL_BASE_URI = "https://gateway.marvel.com";
    public static Properties prop;

    public TestBase() {
        try {
            prop = new Properties();
            FileInputStream ip = new FileInputStream("./src/test/resources/test.properties");
            prop.load(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPrivateKey() {
        return prop.getProperty("privateKey");
    }

    public String getPublicKey() {
        return prop.getProperty("publicKey");
    }

    public RequestSpecification initialise(String timeStamp, String publicKey, String md5Hash) {
        return new RequestSpecBuilder().
                setUrlEncodingEnabled(false).
                setBaseUri(MARVEL_BASE_URI).
                addQueryParam("ts", timeStamp).
                addQueryParam("apikey", publicKey).
                addQueryParam("hash", md5Hash).
                build();
    }
}
