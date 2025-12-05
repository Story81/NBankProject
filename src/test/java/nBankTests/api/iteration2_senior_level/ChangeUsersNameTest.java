package nBankTests.api.iteration2_senior_level;

import api.generatos.RandomData;
import api.models.comparison.ModelAssertions;
import api.models.customer.UpdateCustomerProfileRequest;
import api.models.customer.UpdateCustomerProfileResponse;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.utils.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static api.utils.UserData.UNUSED;


public class ChangeUsersNameTest extends BaseTest {
    private static List<UserData> createdUserIds = new ArrayList<>();

    @Test
    public void userCanUpdateName() {
        UserData user = AdminSteps.createUser();
        createdUserIds.add(user);
        String newName = RandomData.getRandomFullName();

        UpdateCustomerProfileResponse updateResponse = UserSteps.updateCustomerProfile(user, newName);

        UserData expectedUser = new UserData(user.username(), UNUSED, user.id(), UNUSED, newName, user.role());
        ModelAssertions.assertThatModels(expectedUser, updateResponse.getCustomer()).match();
        ModelAssertions.assertThatModels(expectedUser, UserSteps.getCustomerProfile(expectedUser)).match();
    }

    public static Stream<Arguments> changeNameInvalidData() {
        return Stream.of(
                Arguments.of("Mark1999 dfd dfd", "Name must contain two words with letters only"),
                Arguments.of("Mark1999 Samsonov", "Name must contain two words with letters only"),
                Arguments.of("Mark_Samsonov", "Name must contain two words with letters only"),
                Arguments.of("'   '", "Name must contain two words with letters only"),
                Arguments.of("Mark Junior Samsonov", "Name must contain two words with letters only")
        );
    }

    @ParameterizedTest
    @MethodSource("changeNameInvalidData")
    public void userCanNotUpdateName(String newName, String errorMassage) {
        UserData user = AdminSteps.createUser();
        createdUserIds.add(user);

        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.UPDATE_PROFILE,
                ResponseSpecs.requestReturns400WithoutKeyValue(errorMassage))
                .put(updateRequest);

        ModelAssertions.assertThatModels(user, UserSteps.getCustomerProfile(user)).match();
    }

    @Test
    public void userCannotUpdateNameWithInvalidToken() {
        UserData user = AdminSteps.createUser();
        createdUserIds.add(user);
        String newName = RandomData.getRandomFullName();

        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        new CrudRequester(RequestSpecs.authAsUserInvalidToken(user.username(), user.password()),
                Endpoint.UPDATE_PROFILE,
                ResponseSpecs.requestReturnsUnauthorized())
                .put(updateRequest);

        ModelAssertions.assertThatModels(user, UserSteps.getCustomerProfile(user)).match();
    }

    @AfterAll
    public static void deleteTestData() {
        AdminSteps.deleteAllUsers(createdUserIds);
    }
}
