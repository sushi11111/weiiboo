package com.weiiboo.common.Utils;

public class FieldValidationUtil {
    /**
     * 判断手机号是否为空
     * @param phoneNumber 手机号
     * @return 手机号是否为空
     */
    public static boolean phoneNumberNotNull(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    /**
     * 判断是否是手机号
     * @param phoneNumber 手机号
     * @return 是否是手机号
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 判断密码是否合法，密码长度为6-16位，必须包含数字和字母，可以包含特殊字符
     * @param password 密码
     * @return 是否合法
     */
    public static boolean isPassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(.{6,16})$");
    }

    /**
     * 判断验证码格式是否为空
     * @param smsCode 验证码
     * @return 是否为空
     */
    public static boolean smsCodeNotNull(String smsCode) {
        return smsCode != null && !smsCode.trim().isEmpty();
    }

    /**
     * 判断验证码格式是否正确，验证码为6位数字组成的字符串
     * @param smsCode 验证码
     * @return 是否合法
     */
    public static boolean isSmsCode(String smsCode) {
        return smsCode.matches("^\\d{6}$");
    }

    public static boolean isUid(String uid) {
        return uid.matches("^[a-zA-Z0-9]{6,16}$");
    }
}
