package com.automate.loginapp;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SecurityUtils {

    public static String generateToken() {
        int random = getRandomImproved();
        String token = generateRawToken(random);
        byte[] data;
        try {
            data = token.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "tokenError";
        }
        String baseToken = Base64.encodeToString(data, Base64.DEFAULT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < baseToken.length(); i++) {
            char ch = baseToken.charAt(i);
            if (Character.isDigit(ch)) {
                sb.append(SecuredProperties.scrambledNumbers.charAt(Character.getNumericValue(ch)));
            } else if (Character.isLetter(ch)) {
                int index = SecuredProperties.letters.indexOf(ch);
                sb.append(SecuredProperties.scrambledLetters.charAt(index));
            }
        }
        return sb.toString();
    }

    private static String generateRawToken(int strategy) {
        long unixTimeMillis = System.currentTimeMillis();
        long unixTimeSeconds = unixTimeMillis / 1000;
        String unixTimeSecondsString = String.valueOf(unixTimeSeconds);
        String timeStumpLastPart = unixTimeSecondsString
                .substring(Math.max(unixTimeSecondsString.length() - 6, 0));
        Integer timeStumpLast = Integer.parseInt("1" + timeStumpLastPart);
        int[] numbers = SecuredProperties.strategy[strategy];
        StringBuilder builder = new StringBuilder();
        for (int number : numbers) {
            builder.append(number);
        }
        int modifier = Integer.parseInt(builder.toString());
        Integer partial = timeStumpLast + modifier;
        String ranString = Integer.toString(strategy);
        String pre, post;
        if (ranString.length() == 1) {
            pre = "H";
            post = ranString;
        } else {
            pre = String.valueOf(ranString.charAt(0));
            post = String.valueOf(ranString.charAt(1));
        }
        String token = pre + partial.toString().replaceAll("0", "X") + post;
        return token;
    }

    private static int getRandomImproved() {
        long currentMills = System.currentTimeMillis();
        String[] ts = Long.toString(currentMills).split("");
        List<Integer> lastFour = new ArrayList();
        List<Integer> check = new ArrayList();
        for (int i = 1; i <= 4; i++) {
            lastFour.add(Integer.valueOf(ts[ts.length - i]));
            check.add(Integer.valueOf(ts[ts.length - i]));
        }
        do {
            Collections.shuffle(lastFour, new Random());
        } while (lastFour.equals(check));
        Integer firstRandPos = 0;
        Integer secondRandPos = 1;
        firstRandPos = new Random().nextInt(4);
        do {
            secondRandPos = new Random().nextInt(4);
        } while (firstRandPos.equals(secondRandPos));
        Integer first = lastFour.get(firstRandPos.intValue());
        Integer second = lastFour.get(secondRandPos.intValue());
        int finalRandom = Integer.valueOf(String.valueOf(first) + String.valueOf(second));
        return finalRandom;
    }

}
