package nBankTests.iteration1_middle_level;

import generatos.RandomData;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.customer.GetAccountsResponse;
import models.UserRole;
import models.accounts.CreateAccountRequest;
import models.accounts.CreateAccountResponse;
import nBankTests.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.requesters.AdminCreateUserRequestSender;
import requests.requesters.CreateAccountRequestSender;
import requests.requesters.GetAccountsRequestSender;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import static nBankTests.CommonSteps.deleteUser;
import static nBankTests.CommonSteps.deleteUsersAccount;

public class CreateAccountTest extends BaseTest {

    private static String userAuthHeader;
    private static int accountId;
    private static String userId;

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        var extractableResponse = new AdminCreateUserRequestSender(
                RequestSpecs.adminSpec(),
                ResponceSpecs.entityWasCreated())
                .post(userRequest)
                .extract();

        userAuthHeader = extractableResponse.header("Authorization");
        userId = String.valueOf(extractableResponse.as(CreateUserResponse.class).getId());

        CreateAccountResponse createAccountResponse =
                new CreateAccountRequestSender(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        ResponceSpecs.entityWasCreated())
                        .post(new CreateAccountRequest())
                        .extract()
                        .as(CreateAccountResponse.class);

        accountId = createAccountResponse.getId();

        softly.assertThat(createAccountResponse.getId()).isPositive();
        softly.assertThat(createAccountResponse.getAccountNumber()).isNotEmpty();
        softly.assertThat(createAccountResponse.getBalance()).isZero();
        softly.assertThat(createAccountResponse.getTransactions().size()).isZero();

        GetAccountsResponse account = new GetAccountsRequestSender(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponceSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetAccountsResponse[].class)[0];

        softly.assertThat(createAccountResponse.getId()).isEqualTo(account.getId());
        softly.assertThat(createAccountResponse.getAccountNumber()).isEqualTo(account.getAccountNumber());
        softly.assertThat(account.getBalance()).isZero();
        softly.assertThat(account.getTransactions().size()).isZero();
    }

    @AfterAll
    public static void deleteTestData() {
        deleteUsersAccount(userAuthHeader, accountId);
        deleteUser(ADMIN_AUTH, userId);
    }
}
