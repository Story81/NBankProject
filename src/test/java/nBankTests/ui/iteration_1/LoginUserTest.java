package nBankTests.ui.iteration_1;


import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.admin.CreateUserRequest;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;
import utils.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static ui.pages.UserDashboard.DEFAULT_NAME;

public class LoginUserTest extends BaseUiTest {
    private static List<UserData> createdUserIds = new ArrayList<>();
    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open()
                .login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class)
                .getAdminPanelText().shouldBe(visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        UserData user = AdminSteps.createUser();
        createdUserIds.add(user);

        new LoginPage().open()
                .login(user.username(), user.password())
                .getPage(UserDashboard.class)
                .checkWelcomeText(DEFAULT_NAME);
    }

    @AfterAll
    public static void deleteTestData() {
        AdminSteps.deleteAllUsers(createdUserIds);
    }
}
