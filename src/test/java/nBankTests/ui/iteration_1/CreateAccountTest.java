package nBankTests.ui.iteration_1;

import api.models.customer.GetAccountsResponse;
import api.storage.SessionStorage;
import common.annotations.UserSession;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import api.utils.AccountData;
import api.utils.UserData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    private static AccountData createdAccount;


    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();

        List<GetAccountsResponse> existingUserAccounts = SessionStorage.getSteps().getAllAccounts();
        assertThat(existingUserAccounts).hasSize(1);

        createdAccount = new AccountData(existingUserAccounts.get(0));

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccount.accountNumber());

        assertThat(createdAccount.balance()).isZero();
    }

    @AfterAll
    public static void deleteTestData() {
//        UserSteps.deleteAccount(user, createdAccount.id());

    }
}

