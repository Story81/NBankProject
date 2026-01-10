package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import common.utils.RetryUtils;
import lombok.Getter;
import ui.elements.UserBage;

import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserButton = $(Selectors.byText("Add User"));
    private SelenideElement allUsersTitle = $(Selectors.byText("All Users"));


    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password) {
        return StepLogger.log("Admin create user", () -> {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.click();
        return this;
        });
    }

    public List<UserBage> getAllUsers() {
        return StepLogger.log("Get all users from Dashboard", () -> {
            allUsersTitle.shouldBe(visible);
            ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
            return generatePageElements(elementsCollection, UserBage::new);
        });
    }

    public UserBage findUserByUsername(String username) {
        return RetryUtils.retry( "Find user by username " + username,
                () -> getAllUsers().stream().filter(it -> it.getUsername().equals(username)).findAny().orElse(null),
                result -> result != null,
                4,
                2000
        );
    }

    public AdminPanel checkAdminPanelIsVisible() {
        return StepLogger.log("Admin Panel should has Title", () -> {
            adminPanelText.shouldBe(visible);
            return this;
        });
    }
}
