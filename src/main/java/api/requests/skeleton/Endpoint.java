package api.requests.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import api.models.BaseModel;
import api.models.accounts.CreateAccountRequest;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.DeleteAccountRequest;
import api.models.accounts.DeleteAccountResponse;
import api.models.accounts.DepositMoneyRequest;
import api.models.accounts.DepositMoneyResponse;
import api.models.accounts.TransferMoneyRequest;
import api.models.accounts.TransferMoneyResponse;
import api.models.admin.CreateUserRequest;
import api.models.admin.CreateUserResponse;
import api.models.customer.GetAccountsResponse;
import api.models.customer.GetCustomerProfileRequest;
import api.models.customer.GetCustomerProfileResponse;
import api.models.customer.UpdateCustomerProfileRequest;
import api.models.customer.UpdateCustomerProfileResponse;
import api.models.loginUser.DeleteUserRequest;
import api.models.loginUser.LoginUserRequest;
import api.models.loginUser.LoginUserResponse;

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
