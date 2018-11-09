package com.example.nguyen.hybrid_aes_des.model;

import com.example.nguyen.hybrid_aes_des.Utilities;

public class Hybrid_AES_DES {

    public static String hybird_AES_DES(String key, String cipher, boolean check) {
        String BYTES_DES = "11111111";
        String BYTES_AES = "1111111111111111";
        DES stDes = new DES();
        DES ndDes = new DES();
        AES aes = new AES();

        key = Utilities.md5(key);
        aes.setKey(key.substring(0,16));
        stDes.setKey(key.substring(16, 24));
        ndDes.setKey(key.substring(24));

        String result = "";
        int len = cipher.length();
        byte[] cipher_DES_1 = stDes.encrypt(BYTES_DES.getBytes());
        byte[] cipher_DES_2 = ndDes.encrypt(BYTES_DES.getBytes());
        byte[] cipher_AES = BYTES_AES.getBytes();

        //Mã hóa
        if (check) {
            while (len % 16 != 0) {
                cipher += " ";
                len = cipher.length();
            }
            while (len >= 16) {
                String temp_cipher = cipher.substring(0, 8);
                cipher_DES_1 = stDes.encrypt(Utilities.stringToByteArray(temp_cipher));
                temp_cipher = cipher.substring(8, 16);
                cipher_DES_2 = ndDes.encrypt(Utilities.stringToByteArray(temp_cipher));
                cipher = cipher.substring(16);
                len = cipher.length();
                for (int i = 0; i < 16; i++) {
                    if (i % 2 == 0) {
                        cipher_AES[i] = cipher_DES_1[i / 2];
                    } else {
                        cipher_AES[i] = cipher_DES_2[i / 2];
                    }
                }
                result += aes.encrypt(Utilities.byteArrayToString(cipher_AES));
            }
        } else { //Giải mã
            while (len >= 16) {
                String temp_cipher = cipher.substring(0, 16);
                cipher = cipher.substring(16);
                len = cipher.length();
                cipher_AES = Utilities.stringToByteArray(aes.decrypt(temp_cipher));
                for (int i = 0; i < 16; i++) {
                    if (i % 2 == 0) {
                        cipher_DES_1[i / 2] = cipher_AES[i];
                    } else {
                        cipher_DES_2[i / 2] = cipher_AES[i];
                    }
                }
                result += (Utilities.byteArrayToString((stDes.decrypt(cipher_DES_1))));
                result += (Utilities.byteArrayToString((ndDes.decrypt(cipher_DES_2))));
            }
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
