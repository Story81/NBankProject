package nBankTests.ui.iteration_2;

import api.generatos.RandomData;
import api.models.comparison.ModelAssertions;
import api.requests.steps.UserSteps;
import api.utils.UserData;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.HeaderPanel;
import ui.pages.ProfilePage;
import ui.pages.UserDashboard;

import static ui.pages.BankAlert.ERROR_ENTER_VALID_NAME;
import static ui.pages.BankAlert.ERROR_NAME_MUST_CONTAIN_TWO_WORDS;
import static ui.pages.BankAlert.NAME_UPDATE_USER_CREATED_SUCCESSFULLY;
import static ui.pages.UserDashboard.DEFAULT_NAME;

public class ChangeUsersNameTest extends BaseUiTest {
    UserDashboard dashboard = new UserDashboard();
    HeaderPanel headerPanel = new HeaderPanel();

    @Test
    @UserSession
    public void UserCanChangeNameTest() {
        String newName = RandomData.getRandomFullName();
        UserData user = SessionStorage.getUser();

        dashboard.open().checkWelcomeText(DEFAULT_NAME); //проверка приветственного заголовка

        ProfilePage profilePage = headerPanel
                .checkUsersInfo(DEFAULT_NAME, user.username())   // проверка в хедере инфо об именах юзера
                .clickUserProfileMenu()
                .profilePageShouldBeOpen()                 //проверка что страница профиля открылась
                .setNewUserName(newName)                    //ввод нового имени
                .clickSaveButton()
                .checkAlertMessageAndAccept(NAME_UPDATE_USER_CREATED_SUCCESSFULLY.getMessage())
                .profilePageShouldBeOpen()
                .refreshPage();

        headerPanel.checkUsersInfo(newName, user.username());
        profilePage.clickHomeButton()
                .checkWelcomeText(newName);                 //проверка приветственного заголовка

        //проверка смены имени на бэке через getCustomerProfile
        UserData expectedUser = new UserData(user.username(), user.password(), user.id(), user.authHeader(), newName, user.role());
        ModelAssertions.assertThatModels(expectedUser, UserSteps.getCustomerProfile(expectedUser)).match();
    }

    @Test
    @UserSession
    public void UserCanNotChangeNameWithEmptyNewNameTest() {
        UserData user = SessionStorage.getUser();

        dashboard.open().checkWelcomeText(DEFAULT_NAME); //проверка приветственного заголовка

        ProfilePage profilePage = headerPanel
                .checkUsersInfo(DEFAULT_NAME, user.username())   // проверка в хедере инфо об именах юзера
                .clickUserProfileMenu()
                .profilePageShouldBeOpen()                 //проверка что страница профиля открылась
                .clickSaveButton()
                .checkAlertMessageAndAccept(ERROR_ENTER_VALID_NAME.getMessage())
                .profilePageShouldBeOpen()
                .refreshPage();

        headerPanel.checkUsersInfo(DEFAULT_NAME, user.username());
        profilePage.clickHomeButton()
                .checkWelcomeText(DEFAULT_NAME);                 //проверка приветственного заголовка

        //проверка имени на бэке через getCustomerProfile
        ModelAssertions.assertThatModels(user, UserSteps.getCustomerProfile(user)).match();
    }

    @Test
    @UserSession
    public void UserCanNotChangeNameWithInvalidNewNameTest() {
        UserData user = SessionStorage.getUser();

        String newName = RandomData.getUserName();
        dashboard.open().checkWelcomeText(DEFAULT_NAME); //проверка приветственного заголовка

        ProfilePage profilePage = headerPanel
                .checkUsersInfo(DEFAULT_NAME, user.username())   // проверка в хедере инфо об именах юзера
                .clickUserProfileMenu()
                .profilePageShouldBeOpen()                 //проверка что страница профиля открылась
                .setNewUserName(newName)
                .clickSaveButton()
                .checkAlertMessageAndAccept(ERROR_NAME_MUST_CONTAIN_TWO_WORDS.getMessage())
                .profilePageShouldBeOpen()
                .refreshPage();

        headerPanel.checkUsersInfo(DEFAULT_NAME, user.username());
        profilePage.clickHomeButton()
                .checkWelcomeText(DEFAULT_NAME);                 //проверка приветственного заголовка

        //проверка имени на бэке через getCustomerProfile
        ModelAssertions.assertThatModels(user, UserSteps.getCustomerProfile(user)).match();
    }
}
