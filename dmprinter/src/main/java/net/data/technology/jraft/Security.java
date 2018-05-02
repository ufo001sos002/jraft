package net.data.technology.jraft;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public interface Security {

    public static final byte[] ENCRYPT_CODE = {72, 111, 116, 112, 117, 64, 50, 48, 49, 51, 35, 115,
            104, 97, 110, 103, 104, 97, 105, 35, 50, 48, 49, 55};

    interface EncryptBySHA {
        byte[] encrypt(String plainText);
    }

    interface EncryptByAES {
        String encrypt(byte[] plainText, String password);
    }

    interface Decrypt {
        String decrypt(byte[] encryptedBytes, byte[] password);
    }

    enum Utils implements Security {
        ;

        public static String bytes2Hex(byte[] bts) {
            String des = "";
            String tmp = null;
            for (int i = 0; i < bts.length; i++) {
                tmp = (Integer.toHexString(bts[i] & 0xFF));
                if (tmp.length() == 1) {
                    des += "0";
                }
                des += tmp;
            }
            return des;
        }

        public static String parseByte2HexStr(byte buf[]) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buf.length; i++) {
                String hex = Integer.toHexString(buf[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();
        }

        public static byte[] parseHexStr2Byte(String hexStr) {
            if (hexStr.length() < 1)
                return null;

            byte[] result = new byte[hexStr.length() / 2];
            try {
                for (int i = 0; i < hexStr.length() / 2; i++) {
                    int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                    int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                    result[i] = (byte) (high * 16 + low);
                }
            } catch (Exception e) {
            }
            return result;
        }
    }

    enum SHA256 implements EncryptBySHA {
        INSTANCE;

        @Override
        public byte[] encrypt(String plainText) {
            MessageDigest md = null;
            byte[] strDes = null;
            plainText = plainText + ENCRYPT_CODE;
            byte[] bt = plainText.getBytes();
            try {
                md = MessageDigest.getInstance("SHA-256");
                md.update(bt);
                strDes = md.digest();
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
            return strDes;
        }
    }

    int keySize = 128;
    String keyGeneratorType = "AES";

    enum AES128 implements EncryptByAES {
        INSTANCE;

        @Override
        public String encrypt(byte[] plainText, String password) {
            try {
                // encrypt the password(key) via MD5
                byte[] btInput = password.getBytes("UTF-8");
                MessageDigest mdInst = MessageDigest.getInstance("MD5");
                mdInst.update(btInput);
                byte[] enCodeFormat = mdInst.digest();

                SecretKeySpec key = new SecretKeySpec(enCodeFormat, keyGeneratorType);
                Cipher cipher = Cipher.getInstance(keyGeneratorType);
                byte[] byteContent = plainText;
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] result = cipher.doFinal(byteContent);
                return Utils.parseByte2HexStr(result);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            return null;
        }

    }


    enum SEA128 implements Decrypt {
        INSTANCE;

        @Override
        public String decrypt(byte[] content, byte[] password) {
            try {
                // encrypt the password(key) via MD5
                byte[] btInput = password;
                MessageDigest mdInst = MessageDigest.getInstance("MD5");
                mdInst.update(btInput);
                byte[] enCodeFormat = mdInst.digest();

                SecretKeySpec key = new SecretKeySpec(enCodeFormat, keyGeneratorType);
                Cipher cipher = Cipher.getInstance(keyGeneratorType);
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] result = cipher.doFinal(content);
                return new String(result);
            } catch (NoSuchAlgorithmException e) {
            } catch (NoSuchPaddingException e) {
            } catch (InvalidKeyException e) {
            } catch (IllegalBlockSizeException e) {
            } catch (BadPaddingException e) {
            }
            return null;
        }

    }
}
