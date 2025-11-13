package generatos;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomData {
    private static final Random RANDOM = new Random();
    private RandomData() {
    }

    public static String getUserName() {
        return "user" + RandomStringUtils.randomAlphanumeric(10);
    }

    public static int generateRandomAccountId() {
        return 100_000_000 + RANDOM.nextInt(900_000_000);
    }
    public static String getRandomFullName() {
        String firstName = RandomStringUtils.randomAlphabetic(5, 10);
        String lastName = RandomStringUtils.randomAlphabetic(6, 12);
        return firstName + " " + lastName;
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(5) + "-!";
    }

    public static Double getDepositAmount() {
        int min = 1;
        int max = 500000;
        int randomCents = RANDOM.nextInt(max - min + 1) + min;
        return randomCents / 100.00;
    }
}
