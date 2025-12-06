package nBankTests.ui.iteration_1;

import api.models.customer.GetAccountsResponse;
import api.requests.steps.UserSteps;
import api.storage.SessionStorage;
import api.utils.AccountData;
import common.annotations.UserSession;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

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
        SessionStorage.printState();
        assertThat(createdAccount.balance()).isZero();

        UserSteps.deleteAccount(SessionStorage.getUser(), createdAccount.id());
    }
}

