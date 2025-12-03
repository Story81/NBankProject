package nBankTests.api.iteration1_senior_level;

import models.customer.GetAccountsResponse;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import utils.AccountData;
import utils.UserData;

import java.util.List;


public class CreateAccountTest extends BaseTest {
    private static AccountData createdAccount;
    private static UserData user;

    @Test
    public void userCanCreateAccountTest() {
        user = AdminSteps.createUser();
        createdAccount = UserSteps.createAccount(user);

        List<GetAccountsResponse> accounts = UserSteps.getAccounts(user);
        AccountData retrievedAccount = new AccountData(accounts.get(0));

        softly.assertThat(createdAccount).isEqualTo(retrievedAccount);
        createdAccount.assertIsValidNewAccount(softly);
        retrievedAccount.assertIsValidNewAccount(softly);
    }

    @AfterAll
    public static void deleteTestData() {
        UserSteps.deleteAccount(user, createdAccount.id());
        AdminSteps.deleteUser(user);
    }
}
