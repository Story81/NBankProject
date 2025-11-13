package nBankTests.iteration2_middle_level;

import generatos.RandomData;
import models.UserRole;
import models.customer.GetCustomerProfileResponse;
import models.customer.UpdateCustomerProfileRequest;
import models.customer.UpdateCustomerProfileResponse;
import nBankTests.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.requesters.UpdateCustomerProfileRequestSender;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static nBankTests.CommonSteps.assertCustomerProfile;
import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.deleteAllUsers;
import static specs.ResponceSpecs.PROFILE_UPDATED_SUCCESSFULLY;

public class ChangeUsersNameTest extends BaseTest {
    private static String password = RandomData.getPassword();
    private static List<String> createdUserIds = new ArrayList<>();

    @Test
    public void userCanUpdateName() {
        String username = RandomData.getUserName();
        String userId = createUser(username, password);
        createdUserIds.add(userId);

        String newName = RandomData.getRandomFullName();

        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        UpdateCustomerProfileResponse updateResponse = new UpdateCustomerProfileRequestSender(
                RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturnsOK())
                .put(updateRequest)
                .extract()
                .as(UpdateCustomerProfileResponse.class);

        softly.assertThat(updateResponse.getMessage()).isEqualTo(PROFILE_UPDATED_SUCCESSFULLY);
        GetCustomerProfileResponse customer = updateResponse.getCustomer();
        softly.assertThat(customer.getId()).isEqualTo(Integer.parseInt(userId));
        softly.assertThat(customer.getPassword()).isNotEqualTo(password);
        softly.assertThat(customer.getName()).isEqualTo(newName);
        softly.assertThat(customer.getRole()).isEqualTo(UserRole.USER.toString());
        softly.assertThat(customer.getAccounts()).isEmpty();

        assertCustomerProfile(newName, username, password, userId, UserRole.USER.toString());
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
        String username = RandomData.getUserName();
        String userId = createUser(username, password);
        createdUserIds.add(userId);

        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        new UpdateCustomerProfileRequestSender(
                RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturns400WithoutKeyValue(errorMassage))
                .put(updateRequest);

        assertCustomerProfile(null, username, password, userId, UserRole.USER.toString());
    }

    @Test
    public void userCannotUpdateNameWithInvalidToken() {
        String username = RandomData.getUserName();
        String userId = createUser(username, password);
        createdUserIds.add(userId);
        String newName = RandomData.getRandomFullName();

        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        new UpdateCustomerProfileRequestSender(
                RequestSpecs.authAsUserInvalidToken(username, password),
                ResponceSpecs.requestReturnsUnauthorized())
                .put(updateRequest);

        assertCustomerProfile(null, username, password, userId, UserRole.USER.toString());
    }

    @AfterAll
    public static void deleteTestData() {
        deleteAllUsers(createdUserIds);
    }
}
