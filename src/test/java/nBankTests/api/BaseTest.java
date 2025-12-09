package nBankTests.api;

import common.extensions.AccountExtension;
import common.extensions.AdminSessionExtension;
import common.extensions.ApiUserSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.TimingExtension;
import common.extensions.UserSessionExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(ApiUserSessionExtension.class)
@ExtendWith(AccountExtension.class)
@ExtendWith(TimingExtension.class)
public class BaseTest {
    protected static SoftAssertions softly;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}
