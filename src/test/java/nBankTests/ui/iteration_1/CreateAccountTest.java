package nBankTests.ui.iteration_1;

import models.customer.GetAccountsResponse;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import utils.AccountData;
import utils.UserData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    private static UserData user;
    private static AccountData createdAccount;


    @Test
    public void userCanCreateAccountTest() {
        user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard().open()
                .createNewAccount();

        List<GetAccountsResponse> existingUserAccounts = UserSteps.getAccounts(user);
        assertThat(existingUserAccounts).hasSize(1);

        createdAccount = new AccountData(existingUserAccounts.get(0));

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccount.accountNumber());

        assertThat(createdAccount.balance()).isZero();
    }

    @AfterAll
    public static void deleteTestData() {
        UserSteps.deleteAccount(user, createdAccount.id());
        AdminSteps.deleteUser(user);
    }
}

