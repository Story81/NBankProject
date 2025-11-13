package nBankTests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

public class BaseTest {
    protected static SoftAssertions softly;
    protected static final String BASE_URL = "http://localhost:4111";
    protected static final String ADMIN_AUTH = "Basic YWRtaW46YWRtaW4=";


    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }
}
