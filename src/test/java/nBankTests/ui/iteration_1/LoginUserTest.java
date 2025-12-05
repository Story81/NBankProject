package nBankTests.ui.iteration_1;


import api.models.admin.CreateUserRequest;
import common.annotations.AdminSession;
import common.annotations.Browsers;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;
import api.utils.UserData;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static ui.pages.UserDashboard.DEFAULT_NAME;

public class LoginUserTest extends BaseUiTest {
    private static List<UserData> createdUserIds = new ArrayList<>();
    @Test
    @AdminSession
    @Browsers({"chrome"})
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
