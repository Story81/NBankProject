package nBankTests.api.iteration1_senior_level;

import api.generatos.RandomModelGenerator;
import api.models.admin.CreateUserRequest;
import api.models.admin.CreateUserResponse;
import api.models.loginUser.LoginUserRequest;
import common.annotations.Browsers;
import nBankTests.api.BaseTest;
import nBankTests.ui.BaseUiTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.utils.UserData;

import static api.specs.ResponseSpecs.AUTHORIZATION_HEADER;

public class LoginUserTest extends BaseTest {
    private static UserData user;

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        user = UserData.createFrom(createUserResponse);

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(userRequest.getUsername())
                        .password(userRequest.getPassword())
                        .build())
                .header(AUTHORIZATION_HEADER, Matchers.notNullValue());
    }

    @AfterAll
    public static void deleteTestData() {
//        AdminSteps.deleteUser(user);
    }
}
