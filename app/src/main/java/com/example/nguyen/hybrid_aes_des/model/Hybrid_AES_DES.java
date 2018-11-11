package com.example.nguyen.hybrid_aes_des.model;

import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.activity.Decrypt;
import com.example.nguyen.hybrid_aes_des.activity.Encrypt;

public class Hybrid_AES_DES {

    public static String hybird_AES_DES(String key, String cipher, boolean check) {
        String BYTES_DES = "11111111";
        String BYTES_AES = "1111111111111111";
        DES stDes = new DES();
        DES ndDes = new DES();
        AES aes = new AES();

        key = Utilities.md5(key);
        aes.setKey(key.substring(0, 16));
        stDes.setKey(key.substring(16, 24));
        ndDes.setKey(key.substring(24));

        String result = "";
        int len = cipher.length();
        byte[] cipher_DES_1 = stDes.encrypt(BYTES_DES.getBytes());
        byte[] cipher_DES_2 = ndDes.encrypt(BYTES_DES.getBytes());
        byte[] cipher_AES = BYTES_AES.getBytes();

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
                    Encrypt.percent_Encrypted = 100 - (oldLen - percent)*100/oldLen;
                    if (Encrypt.cancel) {
                        Encrypt.cancel = false;
                        break;
                    }
                    String temp_cipher = divideCipher.substring(0, 8);
                    cipher_DES_1 = stDes.encrypt(Utilities.stringToByteArray(temp_cipher));
                    temp_cipher = divideCipher.substring(8, 16);
                    cipher_DES_2 = ndDes.encrypt(Utilities.stringToByteArray(temp_cipher));
                    divideCipher = divideCipher.substring(16);
                    len = divideCipher.length();
                    percent += 16;
                    for (int i = 0; i < 16; i++) {
                        if (i % 2 == 0) {
                            cipher_AES[i] = cipher_DES_1[i / 2];
                        } else {
                            cipher_AES[i] = cipher_DES_2[i / 2];
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
                    Decrypt.percent_Decrypted = 100 - (oldLen - percent)*100/oldLen;
                    if (Decrypt.cancel) {
                        break;
                    }
                    String temp_cipher = divideCipher.substring(0, 16);
                    divideCipher = divideCipher.substring(16);
                    len = divideCipher.length();
                    percent += 16;
                    cipher_AES = Utilities.stringToByteArray(aes.decrypt(temp_cipher));
                    for (int i = 0; i < 16; i++) {
                        if (i % 2 == 0) {
                            cipher_DES_1[i / 2] = cipher_AES[i];
                        } else {
                            cipher_DES_2[i / 2] = cipher_AES[i];
                        }
                    }
                    arrResult[k] += (Utilities.byteArrayToString((stDes.decrypt(cipher_DES_1))));
                    arrResult[k] += (Utilities.byteArrayToString((ndDes.decrypt(cipher_DES_2))));
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