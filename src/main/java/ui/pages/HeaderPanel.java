package ui.pages;

import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class HeaderPanel {

    private SelenideElement profileMenu = $(".user-info");
    private SelenideElement name = profileMenu.$(".user-name");
    private SelenideElement userName = profileMenu.$(".user-username");

    public ProfilePage clickUserProfileMenu() {
        return StepLogger.log("Click on profile menu", () -> {
            ProfilePage profilePage = new ProfilePage();
            profileMenu.shouldBe(visible).click();
            return profilePage;
        });
    }
    public HeaderPanel checkUsersInfo(String nameValue, String userNameValue) {
        return StepLogger.log("Check user's info", () -> {
        name.shouldBe(visible).shouldHave(text(nameValue));
        userName.shouldBe(visible).shouldHave(text(userNameValue));
        return this;
        });
    }
}
