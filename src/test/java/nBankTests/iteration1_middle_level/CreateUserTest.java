package nBankTests.iteration1_middle_level;

import generatos.RandomData;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.UserRole;
import nBankTests.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.requesters.AdminCreateUserRequestSender;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import java.util.stream.Stream;

import static nBankTests.CommonSteps.deleteUser;

public class CreateUserTest extends BaseTest {
    private static String userId;

    @Test
    public void adminCanCreateUserWithCorrectData() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequestSender(RequestSpecs.adminSpec(),
                ResponceSpecs.entityWasCreated())
                .post(createUserRequest)
                .extract().as(CreateUserResponse.class);
        userId = String.valueOf(createUserResponse.getId());

        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                Arguments.of("   ", "Password33$", "USER", "username", "Username cannot be blank", "Username must contain only letters, digits, dashes, underscores, and dots" ), //тест падает. приходит массив ошибок
                Arguments.of("ab", "Password33$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

      new AdminCreateUserRequestSender(RequestSpecs.adminSpec(),
                ResponceSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }

    @AfterAll
    public static void deleteTestData() {
        deleteUser(ADMIN_AUTH, userId);
    }
}
