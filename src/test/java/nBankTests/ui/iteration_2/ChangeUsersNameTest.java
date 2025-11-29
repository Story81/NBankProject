package nBankTests.ui.iteration_2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import generatos.RandomData;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import utils.UserData;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUsersNameTest {
    private static UserData user;

    public final SelenideElement editProfileTitle = $(byText("✏️ Edit Profile"));
    public final SelenideElement profileHeader = $(".profile-header");
    public final SelenideElement userInfo = profileHeader.$(".user-info");
    public final SelenideElement name = userInfo.$(".user-name");
    public final SelenideElement userName = userInfo.$(".user-username");
    public final SelenideElement welcomeHeader = $(".welcome-text");
    public final SelenideElement editNameField = $(byAttribute("placeholder", "Enter new name"));
    public final SelenideElement saveButton = $(byText("\uD83D\uDCBE Save Changes"));
    public final SelenideElement homeButton = $(byText("\uD83C\uDFE0 Home"));


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
    public void UserCanChangeNameTest() {
        user = AdminSteps.createUser();
        String nameofUser = "Noname";
        String newName = RandomData.getRandomFullName();
        String expectedAlert = "✅ Name updated successfully!";
        String profileEndpoint = "/edit-profile";

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        //Проверка элементов на странице и их значений
        welcomeHeader.shouldBe(visible).shouldHave(text("Welcome, " + nameofUser.toLowerCase() + "!"));
        profileHeader.shouldBe(visible);
        userInfo.shouldBe(visible);
        name.shouldBe(visible).shouldHave(text(nameofUser));
        userName.shouldBe(visible).shouldHave(text(user.username()));

        //Переход в меню профайл
        profileHeader.click();
        assertThat(WebDriverRunner.url()).contains(profileEndpoint);

        //Установка нового корректного имени
        editProfileTitle.shouldBe(visible);
        editNameField.shouldBe(visible).setValue(newName);
        saveButton.shouldBe(visible).click();

        //Проверка аллерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains(expectedAlert);
        alert.accept();
        assertThat(WebDriverRunner.url()).contains(profileEndpoint);

        //обновление и проверка отображение нового имени на странице
        Selenide.refresh();
        editProfileTitle.shouldBe(visible);

        name.shouldBe(visible).shouldHave(text(newName));

        //Переход на главный экран и проверка отображения нового имени
        homeButton.shouldBe(visible).click();
        welcomeHeader.shouldBe(visible).shouldHave(text("Welcome, " + newName + "!"));

        //проверка смены имени на бэке через getCustomerProfile
        UserData expectedUser = new UserData(user.username(), user.password(), user.id(), user.authHeader(), newName, user.role());
        ModelAssertions.assertThatModels(expectedUser, UserSteps.getCustomerProfile(expectedUser)).match();
    }

    @Test
    public void UserCanNotChangeNameWithEmptyNewNameTest() {
        user = AdminSteps.createUser();
        String nameofUser = "Noname";
        String expectedAlert = "❌ Please enter a valid name.";
        String profileEndpoint = "/edit-profile";

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        //Проверка элементов на странице и их значений
        welcomeHeader.shouldBe(visible).shouldHave(text("Welcome, " + nameofUser.toLowerCase() + "!"));
        profileHeader.shouldBe(visible);
        userInfo.shouldBe(visible);
        name.shouldBe(visible).shouldHave(text(nameofUser));
        userName.shouldBe(visible).shouldHave(text(user.username()));

        //Переход в меню профайл
        profileHeader.click();
        assertThat(WebDriverRunner.url()).contains(profileEndpoint);

        //Клик без ввода нового имени
        saveButton.shouldBe(visible).click();

        //Проверка аллерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains(expectedAlert);
        alert.accept();
        assertThat(WebDriverRunner.url()).contains(profileEndpoint);

        //обновление и проверка отображения имени на странице
        Selenide.refresh();
        editProfileTitle.shouldBe(visible);

        name.shouldBe(visible).shouldHave(text(nameofUser));

        //Переход на главный экран и проверка отображения имени
        homeButton.shouldBe(visible).click();
        welcomeHeader.shouldBe(visible).shouldHave(text("Welcome, " + nameofUser + "!"));

        //проверка имени на бэке через getCustomerProfile
        ModelAssertions.assertThatModels(user, UserSteps.getCustomerProfile(user)).match();
    }

    @Test
    public void UserCanNotChangeNameWithInvalidNewNameTest() {
        user = AdminSteps.createUser();
        String nameofUser = "Noname";
        String newName = RandomData.getUserName();
        String expectedAlert = "Name must contain two words with letters only";
        String profileEndpoint = "/edit-profile";

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        //Проверка элементов на странице и их значений
        welcomeHeader.shouldBe(visible).shouldHave(text("Welcome, " + nameofUser.toLowerCase() + "!"));
        profileHeader.shouldBe(visible);
        userInfo.shouldBe(visible);
        name.shouldBe(visible).shouldHave(text(nameofUser));
        userName.shouldBe(visible).shouldHave(text(user.username()));

        //Переход в меню профайл
        profileHeader.click();
        assertThat(WebDriverRunner.url()).contains(profileEndpoint);

        //Установка нового невалидного имени
        editNameField.shouldBe(visible).setValue(newName);
        saveButton.shouldBe(visible).click();

        //Проверка аллерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains(expectedAlert);
        alert.accept();
        assertThat(WebDriverRunner.url()).contains(profileEndpoint);

        //обновление и проверка отображения имени на странице
        Selenide.refresh();
        editProfileTitle.shouldBe(visible);

        name.shouldBe(visible).shouldHave(text(nameofUser));

        //Переход на главный экран и проверка отображения имени
        homeButton.shouldBe(visible).click();
        welcomeHeader.shouldBe(visible).shouldHave(text("Welcome, " + nameofUser + "!"));

        //проверка имени на бэке через getCustomerProfile
        ModelAssertions.assertThatModels(user, UserSteps.getCustomerProfile(user)).match();
    }
}
