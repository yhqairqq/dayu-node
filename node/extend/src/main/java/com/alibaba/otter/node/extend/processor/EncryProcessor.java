package com.alibaba.otter.node.extend.processor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
/**
 * Created by yanghuanqing@wdai.com on 2018/6/19.
 */
public class EncryProcessor extends AbstractEventProcessor {
    String filter = "{\n" +
            "    \"level\": \"self\",\n" +
            "    \"schema\": \"test1\",\n" +
            "    \"table\": \"example6\",\n" +
            "    \"columns\": [\n" +
            "        {\n" +
            "            \"name\": \"addr3\",\n" +
            "            \"encry\": \"encry1\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"name\",\n" +
            "            \"encry\": \"encry2\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    static class AESCipher {
        private static final String IV_STRING = "A-16-Byte-String";
        private static final String charset = "UTF-8";
        private static final String key="Weidai@123#45987";

        public static String aesEncryptString(String content){
            try {
                byte[] contentBytes = content.getBytes(charset);
                byte[] keyBytes = key.getBytes(charset);
                byte[] encryptedBytes = aesEncryptBytes(contentBytes, keyBytes);
                Encoder encoder = Base64.getEncoder();
                return encoder.encodeToString(encryptedBytes);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            return content;
        }

        public static String aesDecryptString(String content) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
            Decoder decoder = Base64.getDecoder();
            byte[] encryptedBytes = decoder.decode(content);
            byte[] keyBytes = key.getBytes(charset);
            byte[] decryptedBytes = aesDecryptBytes(encryptedBytes, keyBytes);
            return new String(decryptedBytes, charset);
        }

        private static byte[] aesEncryptBytes(byte[] contentBytes, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
            return cipherOperation(contentBytes, keyBytes, Cipher.ENCRYPT_MODE);
        }

        private static byte[] aesDecryptBytes(byte[] contentBytes, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
            return cipherOperation(contentBytes, keyBytes, Cipher.DECRYPT_MODE);
        }

        private static byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] initParam = IV_STRING.getBytes(charset);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, secretKey, ivParameterSpec);

            return cipher.doFinal(contentBytes);
        }
    }
    public boolean process(EventData eventData) {
        JSONObject jsonObject = JSONObject.parseObject(filter);
        if (eventData.getTableName().equals(jsonObject.getString("table")) &&
                eventData.getSchemaName().equals(jsonObject.getString("schema"))) {
            if (jsonObject.getString("level").equals("all")) {
                //所有字段的值加密
            } else {
                JSONArray columns = jsonObject.getJSONArray("columns");
                for (EventColumn eventColumn : eventData.getColumns()) {
                    for (int i = 0; i < columns.size(); i++) {
                        if (columns.getJSONObject(i).get("name").equals(eventColumn.getColumnName())) {
                            String enrtyAlgo = columns.getJSONObject(i).getString("encry");
                            String encodeSTR=AESCipher.aesEncryptString(eventColumn.getColumnValue());
                            eventColumn.setColumnValue(encodeSTR);
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String filter = "{\n" +
                "    \"level\": \"table\",\n" +
                "    \"shcema\": \"schema1\",\n" +
                "    \"table\": \"table1\",\n" +
                "    \"columns\": [\n" +
                "        {\n" +
                "            \"name\": \"col1\",\n" +
                "            \"encry\": \"encry1\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"col2\",\n" +
                "            \"encry\": \"encry2\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
//        JSONObject jsonObject = JSONObject.parseObject(filter);
//        System.out.println(jsonObject);
    }

}
