package generatos;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.math.BigDecimal;
import java.util.Random;

public class RandomData {
    private RandomData(){}
    public static String getUserName(){
        return "user" + RandomStringUtils.randomAlphanumeric(10);
    }

    public static String getRandomFullName() {
        String firstName = RandomStringUtils.randomAlphabetic(5, 10);
        String lastName = RandomStringUtils.randomAlphabetic(6, 12);
        return firstName + " " + lastName;
    }

    public static String getPassword(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase()+
                RandomStringUtils.randomAlphabetic(5).toLowerCase()+
                RandomStringUtils.randomNumeric(5)+"-!";
    }
    public static BigDecimal getDepositAmount() {
        Random random = new Random();
        int min = 1;          // 0,01 минимум
        int max = 500000;     // 5000.00 — максимум 5000 рублей
        int randomCents = random.nextInt(max - min + 1) + min;
        return BigDecimal.valueOf(randomCents).divide(BigDecimal.valueOf(100));
    }
}
