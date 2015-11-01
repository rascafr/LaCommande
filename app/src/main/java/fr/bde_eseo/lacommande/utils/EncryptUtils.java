package fr.bde_eseo.lacommande.utils;

import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Random;

import fr.bde_eseo.lacommande.R;

/**
 * Created by Rascafr on 20/10/2015.
 */
public class EncryptUtils {

    public static String sha256(String password)
    {
        String sha256 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-256");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha256 = byteToHex(crypt.digest());
        } catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sha256;
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32)
                md5 = "0" + md5;

            return md5;
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getLocalizedMessage());
            return null;
        }
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    // Generate a password with a-z A-Z 0-9 characters
    public static String randomPassword (int length) {
        String pass = "";
        Random rand = new Random();

        // 0  ... 25 -> a ... z
        // 26 ... 51 -> A ... Z
        // 52 ... 61 -> 0 ... 9

        for (int i=0;i<length;i++) {

            int next = rand.nextInt(62);
            Character c = 0;
            if (next <= 25) {
                c = (char) ('a' + next);
            } else if (next >= 26 && next <= 51) {
                c = (char) ('A' + next - 26);
            } else if (next >= 52 && next <= 61) {
                c = (char) ('0' + next - 52);
            } else if (next >= 62) {
                c = (char) ('-');
            }

            pass += c;
        }

        return pass;
    }

    // Generate a password with azAZ09 chars + pseudo latin word
    // count : number of words
    public static String latinPassword (Context ctx, int count) {

        String pass = "";
        Random rand = new Random();
        CharSequence latins[] = ctx.getResources().getStringArray(R.array.password_samples);

        for (int i=0;i<count;i++) {
            int next = rand.nextInt(latins.length);
            pass += latins[next];

            if (i != count-1) {
                pass += "-" + randomPassword(2) + "-";
            }
        }

        return pass;
    }

}
