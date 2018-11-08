package com.example.nguyen.hybrid_aes_des.model;

import com.example.nguyen.hybrid_aes_des.Utilities;

public class Hybrid_AES_DES {

    public static byte[] hybird_AES_DES(String key, String cipher, boolean check) {
        String BYTES_DES = "11111111";
        String BYTES_AES = "1111111111111111";
        DES des = new DES();
        AES aes = new AES();
        des.setKey(key.substring(8));
        aes.setKey(key);

        String result = "";
        int len = cipher.length();
        byte[] cipher_DES_1 = des.encrypt(BYTES_DES.getBytes());
        byte[] cipher_DES_2 = des.encrypt(BYTES_DES.getBytes());
        byte[] cipher_AES = BYTES_AES.getBytes();

        //Mã hóa
        if (check) {
            while (len % 16 != 0) {
                cipher += " ";
                len = cipher.length();
            }
            while (len >= 16) {
                String temp_cipher = cipher.substring(0, 8);
                cipher_DES_1 = des.encrypt(Utilities.stringToByteArray(temp_cipher));
                temp_cipher = cipher.substring(8, 16);
                cipher_DES_2 = des.encrypt(Utilities.stringToByteArray(temp_cipher));
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
            String temp_cipher = key.substring(0, 8);
            cipher_DES_1 = des.encrypt(Utilities.stringToByteArray(temp_cipher));
            temp_cipher = key.substring(8, 16);
            cipher_DES_2 = des.encrypt(Utilities.stringToByteArray(temp_cipher));
            for (int i = 0; i < 16; i++) {
                if (i % 2 == 0) {
                    cipher_AES[i] = cipher_DES_1[i / 2];
                } else {
                    cipher_AES[i] = cipher_DES_2[i / 2];
                }
            }
            String isEncrypted = aes.encrypt(Utilities.byteArrayToString(cipher_AES));
            result = isEncrypted + result;
        } else { //Giải mã
            String isEncrypted = cipher.substring(0, 16);
            cipher = cipher.substring(16);
            cipher_AES = Utilities.stringToByteArray(aes.decrypt(isEncrypted));
            for (int i = 0; i < 16; i++) {
                if (i % 2 == 0) {
                    cipher_DES_1[i / 2] = cipher_AES[i];
                } else {
                    cipher_DES_2[i / 2] = cipher_AES[i];
                }
            }
            isEncrypted = (Utilities.byteArrayToString((des.decrypt(cipher_DES_1))));
            isEncrypted += (Utilities.byteArrayToString((des.decrypt(cipher_DES_2))));
            if (isEncrypted.equals(key)) {
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
                    result += (Utilities.byteArrayToString((des.decrypt(cipher_DES_1))));
                    result += (Utilities.byteArrayToString((des.decrypt(cipher_DES_2))));
                }
                result = result.trim();
            }
        }
        return Utilities.stringToByteArray(result);
    }

    public static byte[] encrypt(String key, String cipher) {
        return hybird_AES_DES(key, cipher, true);
    }

    public static byte[] decrypt(String key, String cipher) {
        return hybird_AES_DES(key, cipher, false);
    }
}
