package ru.kafpin.lab2.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        String pass1 = "pass1";
        System.out.printf("Пароль для %s - %s%n", pass1, ENCODER.encode(pass1));
    }
}
