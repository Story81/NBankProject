package requests.steps;

import io.restassured.common.mapper.TypeRef;
import models.accounts.CreateAccountResponse;
import models.accounts.DeleteAccountResponse;
import models.accounts.DepositMoneyRequest;
import models.accounts.DepositMoneyResponse;
import models.accounts.TransferMoneyRequest;
import models.accounts.TransferMoneyResponse;
import models.customer.GetAccountsResponse;
import models.customer.GetCustomerProfileResponse;
import models.customer.UpdateCustomerProfileRequest;
import models.customer.UpdateCustomerProfileResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponceSpecs;
import utils.AccountData;
import utils.UserData;

import java.util.List;
import java.util.Map;

import static models.accounts.TransferMoneyResponse.TRANSFER_SUCCESSFUL;
import static models.customer.UpdateCustomerProfileResponse.PROFILE_UPDATED_SUCCESSFULLY;

public class UserSteps {

    public static List<GetAccountsResponse> getAccounts(UserData user) {
        return new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponceSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(new TypeRef<List<GetAccountsResponse>>() {
                });
    }

    public static double getBalance(UserData user, AccountData account) {
        float balance = new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponceSpecs.requestReturnsOK())
                .get()
                .extract()
                .path("find { it.id == " + account.id() + " }.balance");
        return balance;
    }

    public static AccountData createAccount(UserData user) {
        CreateAccountResponse createAccountResponse = new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNTS,
                ResponceSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);
        return new AccountData(createAccountResponse);
    }

    public static DepositMoneyResponse deposit(UserData user, AccountData account, Double balance) {
        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(account.id())
                .balance(balance)
                .build();

        return new ValidatedCrudRequester<DepositMoneyResponse>(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponceSpecs.requestReturnsOK())
                .post(depositMoneyRequest);
    }

    public static TransferMoneyResponse transfer(UserData user, AccountData account, AccountData receiverAccount, Double amount) {
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        return new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturnsOKWithMessage(TRANSFER_SUCCESSFUL))
                .post(transferMoneyRequest);
    }

    public static AccountData createAndDepositAccount(UserData user, Double amount, Integer count) {
        AccountData account = createAccount(user);
        if (amount != null && count != null) {
            UserSteps.depositMultipleTimes(user, account, amount, count);
        }
        return account;
    }

    public static void depositMultipleTimes(UserData user, AccountData account, Double balance, int times) {
        for (int i = 0; i < times; i++) {
            deposit(user, account, balance);
        }
    }

    public static DeleteAccountResponse deleteAccount(UserData user, int accountId) {
        return new ValidatedCrudRequester<DeleteAccountResponse>(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.DELETE_ACCOUNTS,
                ResponceSpecs.requestReturnsOK())
                .delete(accountId);
    }

    public static void deleteAllAccounts(Map<UserData, List<Integer>> countsIds) {
        for (Map.Entry<UserData, List<Integer>> entry : countsIds.entrySet()) {
            UserData authHeader = entry.getKey();
            List<Integer> accountIds = entry.getValue();

            for (int accountId : accountIds) {
                try {
                    deleteAccount(authHeader, accountId);
                } catch (Exception e) {
                    System.out.println("All counts:  " + countsIds);
                    System.err.println("Failed to delete account " + accountId + ": " + e.getMessage());
                }
            }
        }
    }

    public static UpdateCustomerProfileResponse updateCustomerProfile(UserData user, String name) {
        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(name)
                .build();

        return new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.UPDATE_PROFILE,
                ResponceSpecs.requestReturnsOKWithMessage(PROFILE_UPDATED_SUCCESSFULLY))
                .put(updateRequest);
    }

    public static GetCustomerProfileResponse getCustomerProfile(UserData user) {
        return new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.PROFILE,
                ResponceSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);
    }
}
