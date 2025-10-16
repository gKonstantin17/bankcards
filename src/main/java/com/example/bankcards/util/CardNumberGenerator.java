package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String BIN = "400000"; // Bank Identification Number

    /**
     * Генерирует валидный номер карты по алгоритму Луна
     */
    public String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(BIN);

        // Генерируем 9 случайных цифр
        for (int i = 0; i < 9; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }

        // Вычисляем контрольную сумму по алгоритму Луна
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    /**
     * Генерирует CVV код
     */
    public String generateCVV() {
        return String.format("%03d", RANDOM.nextInt(1000));
    }

    /**
     * Вычисляет контрольную цифру по алгоритму Луна
     */
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    /**
     * Валидация номера карты по алгоритму Луна
     */
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }

        String clean = cardNumber.replaceAll("\\s+", "");

        try {
            int sum = 0;
            boolean alternate = false;

            for (int i = clean.length() - 1; i >= 0; i--) {
                int digit = Character.getNumericValue(clean.charAt(i));

                if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }

                sum += digit;
                alternate = !alternate;
            }

            return sum % 10 == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
