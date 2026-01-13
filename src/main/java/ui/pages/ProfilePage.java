package ui.pages;

import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import lombok.Getter;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
@Getter
public class ProfilePage extends BasePage<ProfilePage> {

    private SelenideElement editProfileTitle = $(byText("✏️ Edit Profile"));
    private SelenideElement profileHeader = $(".profile-header");
    private SelenideElement welcomeHeader = $(".welcome-text");
    private SelenideElement editNameField = $(byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveButton = $(byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }
    public ProfilePage profilePageShouldBeOpen() {
        return StepLogger.log("Verify profile page is opened", () -> {
            shouldBeOpened();
            editProfileTitle.shouldBe(visible);
            return this;
        });
    }

    public ProfilePage setNewUserName(String name) {
        return StepLogger.log("Set new user name: " + name, () -> {
            editProfileTitle.shouldBe(visible);
            editNameField.shouldBe(visible).setValue(name);
            editNameField.shouldHave(attribute("value", name));
            return this;
        });
    }

    public ProfilePage clickSaveButton() {
        return StepLogger.logWithScreenshotBefore("Click save button to update profile", () -> {
            saveButton.shouldBe(visible).click();
            return this;
        });
    }
}
