package nBankTests.api.iteration1_senior_level;

import api.models.customer.GetAccountsResponse;
import api.requests.steps.UserSteps;
import api.storage.SessionStorage;
import api.utils.AccountData;
import api.utils.UserData;
import common.annotations.UserSession;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;


public class CreateAccountTest extends BaseTest {
    private static AccountData createdAccount;
    private static UserData user;

    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        user = SessionStorage.getUser();
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
    }
}
