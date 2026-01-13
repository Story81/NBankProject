package common.helpers;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

public class StepLogger {

    @FunctionalInterface
    public interface ThrowableRunnable<T> {
        T run() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowableVoidRunnable {
        void run() throws Throwable;
    }

    // Для шагов, которые возвращают значение
    public static <T> T log(String title, ThrowableRunnable<T> runnable) {
        return Allure.step(title, () -> {
            T result = runnable.run();
            if (WebDriverRunner.hasWebDriverStarted()) {
                attachScreenshot(title);
            }
            return result;
        });
    }

    public static <T> T logWithScreenshotBefore(String title, ThrowableRunnable<T> runnable) {
        attachScreenshot(title);
        return Allure.step(title, () -> runnable.run());
    }

    // Для шагов без возвращаемого значения
    public static void log(String title, ThrowableVoidRunnable runnable) {
        Allure.step(title, () -> {
            runnable.run();
            if (WebDriverRunner.hasWebDriverStarted()) {
                attachScreenshot(title);
            }
            return null;
        });
    }

    private static void attachScreenshot(String stepName) {
        // если вебдрайвер не запущен (API- шаг) — просто выходим
        try {
            var driver = WebDriverRunner.getWebDriver();
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            String attachmentName = String.format("Screenshot: %s", stepName);
            Allure.addAttachment(attachmentName, "image/png",
                    new ByteArrayInputStream(bytes), ".png");
        } catch (Exception e) {
            System.err.printf("[StepLogger] Не удалось сделать скриншот для шага '%s': %s%n",
                    stepName, e.getMessage());
        }
    }
}
