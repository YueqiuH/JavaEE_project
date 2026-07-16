package com.smartcampus.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateConvertAgeUtils {

    /**
     * 根据生日计算周岁
     */
    public static int getAge(String birthday) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(birthday, formatter);
            LocalDate currentDate = LocalDate.now();
            if (birthDate.isAfter(currentDate)) {
                return -1;
            }
            int age = currentDate.getYear() - birthDate.getYear();
            if (currentDate.getMonthValue() < birthDate.getMonthValue() ||
                    (currentDate.getMonthValue() == birthDate.getMonthValue() &&
                            currentDate.getDayOfMonth() < birthDate.getDayOfMonth())) {
                age--;
            }
            return age;
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

}
