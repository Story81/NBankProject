package nBankTests.api.iteration1_senior_level;

import api.models.customer.GetAccountsResponse;
import api.requests.steps.UserSteps;
import api.utils.AccountData;
import api.utils.UserData;
import common.annotations.ApiUserSession;
import common.storage.SessionStorage;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static api.requests.steps.UserSteps.getAllUserAccounts;


public class CreateAccountTest extends BaseTest {
    private static AccountData createdAccount;
    private static UserData user;

    @Test
    @ApiUserSession
    @Disabled("Temporarily disabled")
    public void userCanCreateAccountTest() {
        user = SessionStorage.getUser();
        createdAccount = UserSteps.createAccount(user);

        List<GetAccountsResponse> accounts = getAllUserAccounts();
        AccountData retrievedAccount = new AccountData(accounts.get(0));

        softly.assertThat(createdAccount).isEqualTo(retrievedAccount);
        createdAccount.assertIsValidNewAccount(softly);
        retrievedAccount.assertIsValidNewAccount(softly);
        UserSteps.deleteAccount(user, createdAccount.id());
    }
}
