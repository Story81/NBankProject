package nBankTests.ui.iteration_1;

import api.models.customer.GetAccountsResponse;
import api.requests.steps.UserSteps;
import api.utils.AccountData;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static api.requests.steps.UserSteps.getAllUserAccounts;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        List<GetAccountsResponse> existingUserAccounts = getAllUserAccounts();
        assertThat(existingUserAccounts).hasSize(1);

        AccountData createdAccount = new AccountData(existingUserAccounts.get(0));
        String expectedAlertText = BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccount.accountNumber();
        assertThat(alertText).isEqualTo(expectedAlertText);
        assertThat(createdAccount.balance()).isZero();

        UserSteps.deleteAccount(SessionStorage.getUser(), createdAccount.id());
    }
}

