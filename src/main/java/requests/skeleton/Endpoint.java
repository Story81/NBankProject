package requests.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.BaseModel;
import models.accounts.CreateAccountRequest;
import models.accounts.CreateAccountResponse;
import models.accounts.DeleteAccountRequest;
import models.accounts.DeleteAccountResponse;
import models.accounts.DepositMoneyRequest;
import models.accounts.DepositMoneyResponse;
import models.accounts.TransferMoneyRequest;
import models.accounts.TransferMoneyResponse;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.customer.GetAccountsResponse;
import models.customer.GetCustomerProfileRequest;
import models.customer.GetCustomerProfileResponse;
import models.customer.UpdateCustomerProfileRequest;
import models.customer.UpdateCustomerProfileResponse;
import models.loginUser.DeleteUserRequest;
import models.loginUser.LoginUserRequest;
import models.loginUser.LoginUserResponse;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),
    ADMIN_DELETE_USER(
            "/admin/users/{id}",
            DeleteUserRequest.class,
            BaseModel.class
    ),
    UPDATE_PROFILE(
            "/customer/profile",
            UpdateCustomerProfileRequest.class,
            UpdateCustomerProfileResponse.class
    ),

    PROFILE(
            "/customer/profile",
            GetCustomerProfileRequest.class,
            GetCustomerProfileResponse.class
    ),
    ACCOUNTS(
            "/accounts",
            CreateAccountRequest.class,
            CreateAccountResponse.class
    ),
    DELETE_ACCOUNTS(
            "/accounts/{id}",
            DeleteAccountRequest.class,
            DeleteAccountResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            GetAccountsResponse.class),
    ACCOUNT_DEPOSIT(
            "/accounts/deposit",
            DepositMoneyRequest.class,
            DepositMoneyResponse.class),
    ACCOUNT_TRANSFER(
            "/accounts/transfer",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    );

    private final String url;
    private final Class<?> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
