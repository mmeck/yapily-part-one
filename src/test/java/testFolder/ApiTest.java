package testFolder;


import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import util.TestBase;

import java.io.IOException;
import java.util.*;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static util.TestUtil.generateHash;
import static util.TestUtil.timeStamp;

public class ApiTest {

    private static RequestSpecification requestSpec;
    public static Properties prop;
    private static String MARVEL_BASE_URI = "https://gateway.marvel.com";
    private static String privateKey;
    private static String publicKey;
    private static String md5Hash;
    private static String timeStamp;


    private static final String[] mandatoryProperties = new String[]{"id", "name", "description", "modified",
            "resourceURI", "thumbnail", "comics", "stories", "events", "urls"};

    private static final int MAX_RESOURCE_LIMIT = 100;
    private static final int MIN_RESOURCE_LIMIT = 1;
    private static int MIN_OFFSET = 0;
    private static final String GET_CHARACTERS_PATH = "/v1/public/characters";
    private static TestBase testBase;

    @BeforeClass
    public static void createRequestSpecification() throws IOException {
         testBase = new TestBase();
        privateKey = testBase.getPrivateKey();
        publicKey = testBase.getPublicKey();
        timeStamp = timeStamp();
        md5Hash = generateHash(timeStamp, privateKey, publicKey);

    }

    @Test
    public void successTest() {
        requestSpec = testBase.initialise(timeStamp, publicKey, md5Hash);
        given().spec(requestSpec).
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().statusCode(200).
                body("data.results.size", greaterThan(0));
    }

    @Test
    public void marvelPartOneTest() {
        requestSpec = testBase.initialise(timeStamp, publicKey, md5Hash);
        int total = given().spec(requestSpec).
                when().
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().statusCode(200).
                extract().path("data.total");

        List<Map<String, Object>> allCharacters = new ArrayList<>();
        while (MIN_OFFSET < total) {
            List<Map<String, Object>> characterOnPage = given().spec(requestSpec).
                    when().
                    queryParam("limit", MAX_RESOURCE_LIMIT).
                    queryParam("offset", MIN_OFFSET).
                    get(GET_CHARACTERS_PATH).
                    then().assertThat().statusCode(200).
                    extract().
                    response().path("data.results");
            MIN_OFFSET += MAX_RESOURCE_LIMIT;
            allCharacters.addAll(characterOnPage);
        }
        Assert.assertEquals(total, allCharacters.size());
        for (Map<String, Object> marvel : allCharacters) {
            Assert.assertTrue(marvel.keySet().containsAll(Arrays.asList(mandatoryProperties)));
        }
    }

    @Test
    public void limitGreaterThan100Test() {
        requestSpec = testBase.initialise(timeStamp, publicKey, md5Hash);
        given().spec(requestSpec).
                when().
                queryParam("limit", MAX_RESOURCE_LIMIT + 1).
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().statusCode(409).
                body("status", is("You may not request more than 100 items."));
    }

    @Test
    public void limitBelow1Test() {
      RequestSpecification  requestSpec = testBase.initialise(timeStamp,publicKey, md5Hash);
        given().spec(requestSpec).
                when().
                queryParam("limit", MIN_RESOURCE_LIMIT - 1).
                get(GET_CHARACTERS_PATH).
                then().assertThat().statusCode(409).
                body("status", is("You must pass an integer limit greater than 0."));
    }

    @Test
    public void emptyParameterTest() {
        requestSpec = testBase.initialise(timeStamp,publicKey, md5Hash);
        given().spec(requestSpec).
                when().
                queryParam("name", "").
                get(GET_CHARACTERS_PATH).
                then().assertThat().statusCode(409).
                body("status", is("name cannot be blank if it is set"));
    }

    @Test
    public void wrongDataTypeTest() {
        requestSpec = testBase.initialise(timeStamp,publicKey, md5Hash);
        given().spec(requestSpec).
                when().
                queryParam("modifiedBy", 1234).
                get(GET_CHARACTERS_PATH).
                then().assertThat().
                statusCode(409).
                body("status", is("We don't recognize the parameter modifiedBy"));
    }

    @Test
    public void invalidOrderingParameterTest() {
        requestSpec = testBase.initialise(timeStamp,publicKey, md5Hash);
        given().spec(requestSpec).
                when().
                queryParam("orderBy", "typ").
                get(GET_CHARACTERS_PATH).
                then().assertThat().
                statusCode(409).
                body("status", is("typ is not a valid ordering parameter."));
    }

    @Test
    public void invalidParameterBTest() {
        given().spec(requestSpec).
                when().
                queryParam("series", "firstSeries").
                get(GET_CHARACTERS_PATH).
                then().assertThat().
                statusCode(409).
                body("status",
                        is("You must pass at least one valid series if you set the series filter."));
    }

    @Test
    public void emptyFilterValueTest() {
        requestSpec = testBase.initialise(timeStamp,publicKey, md5Hash);
        given().spec(requestSpec).
                when().
                queryParam("series", "").
                get(GET_CHARACTERS_PATH).
                then().assertThat().
                statusCode(409).
                body("status",
                        is("You must pass at least one valid series if you set the series filter."));
        ;
    }

    @Test
    public void tooManyFilterValueTest() {
        requestSpec = testBase.initialise(timeStamp,publicKey, md5Hash);
        given().spec(requestSpec).
                when().
                queryParam("series", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().
                statusCode(409).
                body("status",
                        is("You may not submit more than 10 series ids."));
    }

    @Test
    public void wrongMd5ValueTest() {
        requestSpec = testBase.initialise(timeStamp, publicKey, "A666A66A66A6A666");

        given().spec(requestSpec).
                when().
                get(GET_CHARACTERS_PATH).
                then().assertThat().
                statusCode(401).
                body("code", is("InvalidCredentials"),
                        "message", is("That hash, timestamp and key combination is invalid."));
    }

    @Test
    public void invalidLogInDetailsTest() {
        String md5Hash = generateHash(timeStamp, privateKey, publicKey);
        requestSpec = testBase.initialise(timeStamp, "wrongPublicKey", md5Hash);

        given().spec(requestSpec).
                when().
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().
                statusCode(401).
                body("code", is("InvalidCredentials"),
                        "message", is("The passed API key is invalid."));
    }

    @Test
    public void noAuthParametersTest() {
        RequestSpecification requestSpec = new RequestSpecBuilder().
                setUrlEncodingEnabled(false).
                setBaseUri(MARVEL_BASE_URI).
                build();

        given().spec(requestSpec).
                when().
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().
                statusCode(409).
                body("code", is("MissingParameter"),
                        "message", is("You must provide a user key."));
    }

    @Test
    public void emptyPublicApiKeyDetailsTest() {
        requestSpec = testBase.initialise(timeStamp, "", md5Hash);

        given().spec(requestSpec).
                when().
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().
                statusCode(401).
                body("code", is("InvalidCredentials"),
                        "message", is("The passed API key is invalid."));
    }

    @Test
    public void emptyTimeStampTest() {
        requestSpec = testBase.initialise("", publicKey, md5Hash);

        given().spec(requestSpec).
                when().
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().
                statusCode(401).
                body("code", is("InvalidCredentials"),
                        "message", is("That hash, timestamp and key combination is invalid."));
    }

    @Test
    public void wrongHashTest() {
        String md5Hash = generateHash("anything" + timeStamp, privateKey, publicKey);
        requestSpec = testBase.initialise(timeStamp, publicKey, md5Hash);

        given().spec(requestSpec).
                when().
                get(GET_CHARACTERS_PATH).
                then().
                assertThat().
                statusCode(401).
                body("code", is("InvalidCredentials"),
                        "message", is("That hash, timestamp and key combination is invalid."));
    }
}