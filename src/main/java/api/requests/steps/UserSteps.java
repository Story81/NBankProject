package api.requests.steps;

import api.models.accounts.CreateAccountResponse;
import api.models.accounts.DeleteAccountResponse;
import api.models.accounts.DepositMoneyRequest;
import api.models.accounts.DepositMoneyResponse;
import api.models.accounts.TransferMoneyRequest;
import api.models.accounts.TransferMoneyResponse;
import api.models.customer.GetAccountsResponse;
import api.models.customer.GetCustomerProfileResponse;
import api.models.customer.UpdateCustomerProfileRequest;
import api.models.customer.UpdateCustomerProfileResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.utils.AccountData;
import api.utils.UserData;
import common.storage.SessionStorage;
import common.utils.RetryUtils;
import io.restassured.common.mapper.TypeRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static api.models.accounts.TransferMoneyResponse.TRANSFER_SUCCESSFUL;
import static api.models.customer.UpdateCustomerProfileResponse.PROFILE_UPDATED_SUCCESSFULLY;

public class UserSteps {
    private UserData user;
    private static Map<UserData, List<Integer>> accountsIds = new HashMap<>();

    public UserSteps(UserData user) {
        this.user = user;
    }

    public static List<GetAccountsResponse> getAccounts(UserData user) {
        return new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(new TypeRef<List<GetAccountsResponse>>() {
                });
    }

    public List<GetAccountsResponse> getAllAccounts() {
        return new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(new TypeRef<List<GetAccountsResponse>>() {
                });
    }
    public static List <GetAccountsResponse> getAllUserAccounts() {
        return RetryUtils.retry( "Find all user accounts",
                () ->  SessionStorage.getSteps().getAllAccounts().stream().collect(Collectors.toList()),
                result -> result != null,
                4,
                1000
        );
    }

    public static double getBalance(UserData user, AccountData account) {
        float balance = new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .path("find { it.id == " + account.id() + " }.balance");
        return balance;
    }

    public static AccountData createAccount(UserData user) {
        CreateAccountResponse createAccountResponse = new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
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
                ResponseSpecs.requestReturnsOK())
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
                ResponseSpecs.requestReturnsOKWithMessage(TRANSFER_SUCCESSFUL))
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
                ResponseSpecs.requestReturnsOK())
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
                ResponseSpecs.requestReturnsOKWithMessage(PROFILE_UPDATED_SUCCESSFULLY))
                .put(updateRequest);
    }

    public static GetCustomerProfileResponse getCustomerProfile(UserData user) {
        return new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);
    }

    public static void addAccountToAccountsIdsMap(UserData user, AccountData account) {
        accountsIds.computeIfAbsent(user, k -> new ArrayList<>()).add(account.id());
    }
}
