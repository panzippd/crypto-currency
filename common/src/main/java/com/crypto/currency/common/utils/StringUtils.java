package com.crypto.currency.common.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.UUID;

/**
 * @author Panzi
 * @Description string utils
 * @date 2022/4/29 17:40
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    /**
     * Checks if there is any Emoji in the string given.
     *
     * @param source the string to check
     * @return true if there is Emoji in the string
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isEmojiCharacter(codePoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates UUID
     *
     * @param removeDash whether remove the dash in it
     * @return the UUID generated
     */
    public static String uuid(boolean removeDash) {
        if (removeDash) {
            return UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a UUID without dash
     *
     * @return the UUID generated
     */
    public static String uuid() {
        return uuid(true);
    }

    /**
     * Checks if the current code point is a sign of emoji
     *
     * @param codePoint the code point to check
     * @return true if it's a sign of emoji; false otherwise
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD) || (
            (codePoint >= 0x20) && (codePoint <= 0xD7FF)) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || (
            (codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));
    }

    /**
     * Determine whether seq contains all searchSeq
     *
     * @param seq       the CharSequence to check, may be null
     * @param searchSeq the CharSequence to find, may be null
     * @return
     */
    public static boolean containAll(String seq, String... searchSeq) {

        if (isBlank(seq) || ArrayUtils.isEmpty(searchSeq)) {
            return false;
        }
        for (String s : searchSeq) {
            if (!StringUtils.contains(seq, s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * compare user's app versions
     *
     * @param userVersion    the user's version
     * @param supportVersion the system support version
     * @return 0 equal 1 greater then  -1 less then
     */
    public static int compareVersion(String userVersion, String supportVersion) {

        if (isAllBlank(userVersion, supportVersion)) {
            return 0;
        }
        if (isBlank(userVersion)) {
            return -1;
        }
        if (isBlank(supportVersion)) {
            return 1;
        }
        String[] userVersions = split(userVersion, '.');
        String[] supportVersions = split(supportVersion, '.');
        int minLength = Math.min(userVersions.length, supportVersions.length);
        int diff = 0;
        for (int i = 0; i < minLength; i++) {
            String userSubVersion = userVersions[i];
            String supportSubVersion = supportVersions[i];
            if (!((diff = userSubVersion.length() - supportSubVersion.length()) == 0
                && (diff = compareIgnoreCase(userSubVersion, supportSubVersion)) == 0)) {
                break;
            }
        }
        return diff != 0 ? diff : userVersion.length() - supportVersion.length();
    }
}
