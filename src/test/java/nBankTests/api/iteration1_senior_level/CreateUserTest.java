package nBankTests.api.iteration1_senior_level;


import api.generatos.RandomModelGenerator;
import api.models.admin.CreateUserRequest;
import api.models.admin.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.utils.UserData;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;


public class CreateUserTest extends BaseTest {
    private static UserData user;

    @Test
    public void adminCanCreateUserWithCorrectData() {
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        user = UserData.createFrom(createUserResponse);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of(
                        CreateUserRequest.builder().username("   ").password("Password33$").role("USER").build(), //тест падает. приходит массив ошибок
                        "username", "Username cannot be blank. Username must contain only letters, digits, dashes, underscores, and dots"
                ),
                Arguments.of(
                        CreateUserRequest.builder().username("ab").password("Password33$").role("USER").build(),
                        "username", "Username must be between 3 and 15 characters"
                ),
                Arguments.of(
                        CreateUserRequest.builder().username("abc$").password("Password33$").role("USER").build(),
                        "username", "Username must contain only letters, digits, dashes, underscores, and dots"
                ),
                Arguments.of(
                        CreateUserRequest.builder().username("abc%").password("Password33$").role("USER").build(),
                        "username", "Username must contain only letters, digits, dashes, underscores, and dots"
                )
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(CreateUserRequest createUserRequest, String errorKey, String errorValue) {
        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }

    @AfterAll
    public static void deleteTestData() {
        AdminSteps.deleteUser(user);
    }
}
