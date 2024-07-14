package com.weiiboo.common.Utils;

import java.time.LocalDate;
import java.time.Period;

public class TimeUtil {
    public static int calculateAge(LocalDate birthDate) {
        LocalDate currentDate = LocalDate.now();
        if (birthDate != null) {
            return Period.between(birthDate, currentDate).getYears();
        } else {
            return 0;
        }
    }
}
