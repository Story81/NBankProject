package nBankTests.ui.iteration_1;


import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.admin.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import utils.UserData;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class LoginUserTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.1.251:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(byText("Admin Panel")).shouldBe(visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
       UserData user = AdminSteps.createUser();

        Selenide.open("/login");

        $(byAttribute("placeholder", "Username")).sendKeys(user.username());
        $(byAttribute("placeholder", "Password")).sendKeys(user.password());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).shouldBe(visible).shouldHave(text("Welcome, noname!"));
    }
}
