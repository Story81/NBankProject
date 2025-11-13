package nBankTests;

import io.restassured.http.ContentType;
import models.UserRole;
import models.accounts.CreateAccountRequest;
import models.accounts.CreateAccountResponse;
import models.accounts.DepositMoneyRequest;
import models.accounts.DepositMoneyResponse;
import models.admin.CreateUserRequest;
import models.customer.GetCustomerProfileResponse;
import models.loginUser.LoginUserRequest;
import org.apache.http.HttpStatus;
import requests.requesters.AdminCreateUserRequestSender;
import requests.requesters.CreateAccountRequestSender;
import requests.requesters.DepositMoneyRequestSender;
import requests.requesters.GetCustomerProfileRequestSender;
import requests.requesters.LoginUserRequestSender;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

public class CommonSteps extends BaseTest {
    public static void deleteUsersAccount(String userAuthHeader, int accountId) {
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .delete("http://localhost:4111/api/v1/accounts/{accountId}", accountId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    public static void deleteUser(String userAuthHeader, String userId) {
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .delete("http://localhost:4111/api/v1/admin/users/{id}", userId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    public static void deleteAllUsers(List<String> createdUserIds) {
        for (String userId : createdUserIds) {
            try {
                deleteUser(ADMIN_AUTH, userId);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String createUser(String name, String password) {
        CreateUserRequest user = CreateUserRequest.builder()
                .username(name)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        return new AdminCreateUserRequestSender(RequestSpecs.adminSpec(), ResponceSpecs.entityWasCreated())
                .post(user)
                .extract()
                .path("id").toString();
    }

    public static int createUsersAccount(String name, String password) {
        CreateAccountResponse createAccountResponse =
                new CreateAccountRequestSender(RequestSpecs.authAsUser(name, password),
                        ResponceSpecs.entityWasCreated())
                        .post(new CreateAccountRequest())
                        .extract()
                        .as(CreateAccountResponse.class);
        return createAccountResponse.getId();
    }

    public static String getAuthToken(String username, String password) {
        return new LoginUserRequestSender(RequestSpecs.unauthSpec(),
                ResponceSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(username)
                        .password(password)
                        .build())
                .extract()
                .header("Authorization");
    }

    public static DepositMoneyResponse deposit(String authHeader, int accountId, Float amount) {
        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        return new DepositMoneyRequestSender(
                RequestSpecs.authAsUser(authHeader),
                ResponceSpecs.requestReturnsOK())
                .post(userDepositRequest)
                .extract()
                .as(DepositMoneyResponse.class);
    }

    public static void depositMultipleTimes(String authHeader, int accountId, Float amount, int times) {
        for (int i = 0; i < times; i++) {
            deposit(authHeader, accountId, amount);
        }
    }

    public static float getBalance(String authHeader, int accountId) {
        float balance =
                given()
                        .header("Authorization", authHeader)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .get("/api/v1/customer/accounts")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .body("id", hasItem(accountId))
                        .body("transactions", not(empty()))
                        .extract()
                        .path("find { it.id == " + accountId + " }.balance");
        return balance;
    }

    public static void deleteAllAccounts(Map<String, List<Integer>> countsIds) {
        for (Map.Entry<String, List<Integer>> entry : countsIds.entrySet()) {
            String authHeader = entry.getKey();
            List<Integer> accountIds = entry.getValue();

            for (int accountId : accountIds) {
                try {
                    deleteUsersAccount(authHeader, accountId);
                } catch (Exception e) {
                    System.out.println("All counts:  " + countsIds);
                    System.err.println("Failed to delete account " + accountId + ": " + e.getMessage());
                }
            }
        }
    }

    public static GetCustomerProfileResponse getCustomerProfile(String username, String password) {
        return new GetCustomerProfileRequestSender(
                RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);
    }

    public static void assertCustomerProfile(
            String newName,
            String username,
            String password,
            String expectedUserId,
            String expectedRole
    ) {
        GetCustomerProfileResponse response = getCustomerProfile(username, password);

        softly.assertThat(response.getId()).isEqualTo(Integer.parseInt(expectedUserId));
        softly.assertThat(response.getPassword()).isNotEqualTo(password);
        softly.assertThat(response.getName()).isEqualTo(newName);
        softly.assertThat(response.getUsername()).isEqualTo(username);
        softly.assertThat(response.getRole()).isEqualTo(expectedRole);
        softly.assertThat(response.getAccounts()).isEmpty();
    }
}
