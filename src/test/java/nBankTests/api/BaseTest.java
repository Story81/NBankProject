package nBankTests.api;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

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
}
