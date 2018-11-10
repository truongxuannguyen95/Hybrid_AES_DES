package com.example.nguyen.hybrid_aes_des.model;

import com.example.nguyen.hybrid_aes_des.Utilities;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Hybrid_AES_DES {

    public static void cryptFile(String key, String cipher, File file, boolean check) throws IOException {
        String BYTES_DES = "11111111111111111111111111111111";
        BYTES_DES += BYTES_DES;
        DES stDes = new DES();
        DES ndDes = new DES();
        AES aes = new AES();

        String md5Key = Utilities.md5(key);
        aes.setKey(md5Key.substring(0, 16));
        stDes.setKey(md5Key.substring(16, 24));
        ndDes.setKey(md5Key.substring(24));

        int len = cipher.length();
        byte[] cipher_DES_1 = stDes.encrypt(BYTES_DES.getBytes());
        byte[] cipher_DES_2 = ndDes.encrypt(BYTES_DES.getBytes());
        byte[] cipher_AES = new byte[128];

        //Mã hóa
        if (check) {
            BufferedOutputStream bos = null;
            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            String encryptKey = encrypt_String("TruongXuanNguyen", md5Key);
            String encryptLen = encrypt_String(key, len + "");
            byte[] bytesKey = Utilities.stringToByteArray(encryptKey);
            byte[] bytesLen = Utilities.stringToByteArray(encryptLen);
            bos.write(bytesKey);
            bos.write(bytesLen);
            while (len % 128 != 0) {
                cipher += " ";
                len = cipher.length();
            }
            while (len >= 128) {
                String temp_cipher = cipher.substring(0, 64);
                cipher_DES_1 = stDes.encrypt(Utilities.stringToByteArray(temp_cipher));
                temp_cipher = cipher.substring(64, 128);
                cipher_DES_2 = ndDes.encrypt(Utilities.stringToByteArray(temp_cipher));
                cipher = cipher.substring(128);
                len = cipher.length();
                for (int i = 0; i < 128; i++) {
                    if (i % 2 == 0) {
                        cipher_AES[i] = cipher_DES_1[i / 2];
                    } else {
                        cipher_AES[i] = cipher_DES_2[i / 2];
                    }
                }
                byte[] bytes = Utilities.stringToByteArray(aes.encrypt(Utilities.byteArrayToString(cipher_AES)));
                bos.write(bytes);
            }
            bos.flush();
            bos.close();
        } else { //Giải mã
            String decryptLen = cipher.substring(0, 16);
            decryptLen = decrypt_String(key, decryptLen);
            int oldLen = Integer.parseInt(decryptLen.trim());
            cipher = cipher.substring(16);
            len = cipher.length();
            int subLen = len - oldLen;
            BufferedOutputStream bos = null;
            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            while (len > 128) {
                String temp_cipher = cipher.substring(0, 128);
                cipher = cipher.substring(128);
                len = cipher.length();
                cipher_AES = Utilities.stringToByteArray(aes.decrypt(temp_cipher));
                for (int i = 0; i < 128; i++) {
                    if (i % 2 == 0) {
                        cipher_DES_1[i / 2] = cipher_AES[i];
                    } else {
                        cipher_DES_2[i / 2] = cipher_AES[i];
                    }
                }
                byte[] bytes = (stDes.decrypt(cipher_DES_1));
                bos.write(bytes);
                bytes = (ndDes.decrypt(cipher_DES_2));
                bos.write(bytes);
            }
            cipher_AES = Utilities.stringToByteArray(aes.decrypt(cipher));
            for (int i = 0; i < 128; i++) {
                if (i % 2 == 0) {
                    cipher_DES_1[i / 2] = cipher_AES[i];
                } else {
                    cipher_DES_2[i / 2] = cipher_AES[i];
                }
            }
            String last = Utilities.byteArrayToString(stDes.decrypt(cipher_DES_1));
            last += Utilities.byteArrayToString(ndDes.decrypt(cipher_DES_2));
            last = last.substring(0, last.length() - subLen);
            byte[] bytes = Utilities.stringToByteArray(last);
            bos.write(bytes);
            bos.flush();
            bos.close();
        }
    }

    public static void encrypt_File(String key, String cipher, File file) throws IOException {
        cryptFile(key, cipher, file, true);
    }

    public static void decrypt_File(String key, String cipher, File file) throws IOException {
        cryptFile(key, cipher, file, false);
    }


    public static String encrypt_String(String key, String cipher) {
        DES stDes = new DES();
        DES ndDes = new DES();
        AES aes = new AES();

        key = Utilities.md5(key);
        aes.setKey(key.substring(0, 16));
        stDes.setKey(key.substring(16, 24));
        ndDes.setKey(key.substring(24));

        String result = "";
        byte[] cipher_AES = new byte[16];
        int len = cipher.length();

        //Mã hóa
        while (len % 16 != 0) {
            cipher += " ";
            len = cipher.length();
        }
        while (len >= 16) {
            String temp_cipher = cipher.substring(0, 8);
            byte[] cipher_DES_1 = stDes.encrypt(Utilities.stringToByteArray(temp_cipher));
            temp_cipher = cipher.substring(8, 16);
            byte[] cipher_DES_2 = ndDes.encrypt(Utilities.stringToByteArray(temp_cipher));
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
        return result;
    }

    public static String decrypt_String(String key, String cipher) {
        String BYTES_DES = "11111111";
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
        byte[] cipher_AES = new byte[16];

        //Giải mã
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
        return result;
    }
}
