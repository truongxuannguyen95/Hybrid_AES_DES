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
        byte [] arrayResult;
        int len = cipher.length();
        byte[] cipher_FirstDES = firstDES.encrypt(BYTES_DES.getBytes());
        byte[] cipher_SecondDES = secondDES.encrypt(BYTES_DES.getBytes());
        byte[] cipher_AES = BYTES_AES.getBytes();
        byte[] stKey = firstDESKey.getBytes();
        byte[] ndKey = secondDESKey.getBytes();

        //Mã hóa
        if (check) {
            while (len % 16 != 0) {
                cipher += " ";
                len = cipher.length();
            }
            arrayResult = new byte[len];
            for(int p=0; p<len/16; p++) {
                FragmentEncrypt.percent_Encrypted = ((p+1)*16)*100/len;
                if (FragmentEncrypt.cancel) {
                    FragmentEncrypt.cancel = false;
                    return result;
                }
                String temp_cipher = cipher.substring(p*16, p*16 + 16);
                cipher_FirstDES = firstDES.encrypt(Utilities.stringToByteArray(temp_cipher.substring(0, 8)));
                cipher_SecondDES = secondDES.encrypt(Utilities.stringToByteArray(temp_cipher.substring(8)));
                for (int i = 0; i < 16; i++) {
                    if (i < 8) {
                        cipher_AES[mix[i]] = (byte) (cipher_FirstDES[i] ^ ndKey[i]);
                    } else {
                        cipher_AES[mix[i]] = (byte) (cipher_SecondDES[i - 8] ^ stKey[i-8]);
                    }
                }
                byte[] partByte = aes.encrypt(Utilities.byteArrayToString(cipher_AES));
                for(int b=0; b<16; b++)
                    arrayResult[p*16+b] = partByte[b];
            }
        } else {  //Giải mã
            arrayResult = new byte[len];
            for(int p=0; p<len/16; p++) {
                FragmentDecrypt.percent_Decrypted = ((p+1)*16)*100/len;
                if (FragmentDecrypt.cancel) {
                    return result;
                }
                String temp_cipher = cipher.substring(p*16, p*16 + 16);
                cipher_AES = aes.decrypt(temp_cipher);
                for (int i = 0; i < 16; i++) {
                    if (i < 8) {
                        cipher_FirstDES[i] = (byte) (cipher_AES[mix[i]] ^ ndKey[i]);
                    } else {
                        cipher_SecondDES[i - 8] = (byte) (cipher_AES[mix[i]] ^ stKey[i-8]);
                    }
                }
                String temp = (Utilities.byteArrayToString((firstDES.decrypt(cipher_FirstDES))));
                temp += (Utilities.byteArrayToString((secondDES.decrypt(cipher_SecondDES))));
                byte[] partByte = Utilities.stringToByteArray(temp);
                for(int b=0; b<16; b++)
                    arrayResult[p*16+b] = partByte[b];
            }
        }
        result = Utilities.byteArrayToString(arrayResult);
        return result;
    }

    public static String encrypt(String key, String cipher) {
        return hybird_AES_DES(key, cipher, true);
    }

    public static String decrypt(String key, String cipher) {
        return hybird_AES_DES(key, cipher, false);
    }
}