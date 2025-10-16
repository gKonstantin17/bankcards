package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardMaskingUtil {

    private static final String MASK_CHAR = "*";
    private static final int VISIBLE_DIGITS = 4;

    /**
     * Маскирует номер карты, оставляя видимыми последние 4 цифры
     * Пример: 1234567890123456 -> **** **** **** 3456
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return MASK_CHAR.repeat(16);
        }

        // Удаляем пробелы если есть
        String cleanNumber = cardNumber.replaceAll("\\s+", "");

        if (cleanNumber.length() < VISIBLE_DIGITS) {
            return MASK_CHAR.repeat(16);
        }

        // Получаем последние 4 цифры
        String lastFour = cleanNumber.substring(cleanNumber.length() - VISIBLE_DIGITS);

        // Создаем маску
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0 && i % 4 == 0) {
                masked.append(" ");
            }
            masked.append(MASK_CHAR);
        }
        masked.append(" ").append(lastFour);

        return masked.toString();
    }

    /**
     * Форматирует номер карты с пробелами
     * Пример: 1234567890123456 -> 1234 5678 9012 3456
     */
    public String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "";
        }

        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < cleanNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(cleanNumber.charAt(i));
        }

        return formatted.toString();
    }

    /**
     * Маскирует CVV
     */
    public String maskCVV() {
        return MASK_CHAR.repeat(3);
    }
}
