package com.example.nguyen.hybrid_aes_des.model;

import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.activity.FragmentDecrypt;
import com.example.nguyen.hybrid_aes_des.activity.FragmentEncrypt;

public class Hybrid_AES_DES {

    public static String hybird_AES_DES(String key, String cipher, boolean check) {
        String BYTES_DES = "11111111";
        String BYTES_AES = "1111111111111111";
        DES firstDES = new DES();
        DES secondDES = new DES();
        AES aes = new AES();

        int[] mix = {7, 1, 3, 4, 6, 9, 2, 11, 15, 0, 8, 14, 12, 5, 13, 10};

        key = Utilities.md5(key);
        String firstDESKey = key.substring(16, 24);
        String secondDESKey = key.substring(24);
        aes.setKey(key.substring(0, 16));
        firstDES.setKey(firstDESKey);
        secondDES.setKey(secondDESKey);

        String result = "";
        int len = cipher.length();
        byte[] cipher_DES_1 = firstDES.encrypt(BYTES_DES.getBytes());
        byte[] cipher_DES_2 = secondDES.encrypt(BYTES_DES.getBytes());
        byte[] cipher_AES = BYTES_AES.getBytes();
        byte[] stKey = firstDESKey.getBytes();
        byte[] ndKey = secondDESKey.getBytes();

        int divide = (len + 16) / 16000;
        divide = divide + 1;
        String[] arrResult = new String[divide];
        for (int i = 0; i < divide; i++) {
            arrResult[i] = "";
        }
        int percent = 0;
        //Mã hóa
        if (check) {
            while (len % 16 != 0) {
                cipher += " ";
                len = cipher.length();
            }
            int oldLen = len;
            for (int k = 0; k < divide; k++) {
                String divideCipher = "";
                if (k+1 == divide) {
                    divideCipher = cipher;
                } else {
                    divideCipher = cipher.substring(0, 16000);
                    cipher = cipher.substring(16000);
                }
                len = divideCipher.length();
                while (len >= 16) {
                    FragmentEncrypt.percent_Encrypted = 100 - (oldLen - percent)*100/oldLen;
                    if (FragmentEncrypt.cancel) {
                        FragmentEncrypt.cancel = false;
                        return result;
                    }
                    String temp_cipher = divideCipher.substring(0, 8);
                    cipher_DES_1 = firstDES.encrypt(Utilities.stringToByteArray(temp_cipher));
                    temp_cipher = divideCipher.substring(8, 16);
                    cipher_DES_2 = secondDES.encrypt(Utilities.stringToByteArray(temp_cipher));
                    divideCipher = divideCipher.substring(16);
                    len = divideCipher.length();
                    percent += 16;
                    for (int i = 0; i < 16; i++) {
                        if (i < 8) {
                            cipher_AES[mix[i]] = (byte) (cipher_DES_1[i] ^ ndKey[i]);
                        } else {
                            cipher_AES[mix[i]] = (byte) (cipher_DES_2[i - 8] ^ stKey[i-8]);
                        }
                    }
                    arrResult[k] += aes.encrypt(Utilities.byteArrayToString(cipher_AES));
                }
            }
        } else {  //Giải mã
            int oldLen = len;
            for (int k = 0; k < divide; k++) {
                String divideCipher = "";
                if (k + 1 == divide) {
                    divideCipher = cipher;
                } else {
                    divideCipher = cipher.substring(0, 16000);
                    cipher = cipher.substring(16000);
                }
                len = divideCipher.length();
                while (len >= 16) {
                    FragmentDecrypt.percent_Decrypted = 100 - (oldLen - percent)*100/oldLen;
                    if (FragmentDecrypt.cancel) {
                        return result;
                    }
                    String temp_cipher = divideCipher.substring(0, 16);
                    divideCipher = divideCipher.substring(16);
                    len = divideCipher.length();
                    percent += 16;
                    cipher_AES = Utilities.stringToByteArray(aes.decrypt(temp_cipher));
                    for (int i = 0; i < 16; i++) {
                        if (i < 8) {
                            cipher_DES_1[i] = (byte) (cipher_AES[mix[i]] ^ ndKey[i]);
                        } else {
                            cipher_DES_2[i - 8] = (byte) (cipher_AES[mix[i]] ^ stKey[i-8]);
                        }
                    }
                    arrResult[k] += (Utilities.byteArrayToString((firstDES.decrypt(cipher_DES_1))));
                    arrResult[k] += (Utilities.byteArrayToString((secondDES.decrypt(cipher_DES_2))));
                }
            }
        }
        for (int i = 0; i < divide; i++) {
            result += arrResult[i];
        }
        return result;
    }

    public static String encrypt(String key, String cipher) {
        return hybird_AES_DES(key, cipher, true);
    }

    public static String decrypt(String key, String cipher) {
        return hybird_AES_DES(key, cipher, false);
    }
}