package cl.theroot.passbank;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Benjamin on 05/11/2017.
 */

public class Cifrador {
    private static final String TAG = "BdC-Cifrador";

    public static final int LARGO_MINIMO_LLAVE_MAESTRA = 8;

    private static final int ITERACIONES = 10000;
    private static final int LARGO_SALT = 16;
    private static final int LARGO_DESEADO_LLAVE = 16 * 8;

    private static final int IV_LARGO_BASE64 = 24;
    private static final int IV_LARGO = 16;

    private static byte[] genSalt(int largo) {
        byte[] salida = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            salida = new byte[largo];
            sr.nextBytes(salida);
        } catch (Exception ex) {
            Log.e(TAG, "Error al generar la sal.", ex);
        }
        return salida;
    }

    public static String genSalt() {
        byte[] byteSalt = genSalt(LARGO_SALT);
        return Base64.encodeToString(byteSalt, Base64.NO_WRAP);
    }

    private static byte[] genHash(char[] contrasenna, byte[] salt) {
        byte[] salida = null;
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(contrasenna, salt, ITERACIONES, LARGO_DESEADO_LLAVE);
            salida = skf.generateSecret(spec).getEncoded();
        } catch(Exception ex){
            Log.e(TAG, "Error al generar hash.", ex);
        }
        return salida;
    }

    public static String[] genHashedPass(String contrasenna, String salt) {
        byte[] byteSalt;
        if (salt == null) {
            byteSalt = genSalt(LARGO_SALT);
        } else {
            byteSalt = Base64.decode(salt, Base64.NO_WRAP);
            if (byteSalt.length != LARGO_SALT) {
                byteSalt = genSalt(LARGO_SALT);
            }
        }

        byte[] hashedPass = genHash(contrasenna.toCharArray(), byteSalt);
        String[] resultado = new String[2];
        resultado[0] = Base64.encodeToString(byteSalt, Base64.NO_WRAP);
        resultado[1] = Base64.encodeToString(hashedPass, Base64.NO_WRAP);
        return resultado;
    }

    private static SecretKey StringToSecretKey(String stringKey) {
        byte[] encodedKey = Base64.decode(stringKey, Base64.NO_WRAP);
        //Log.i(TAG, "encodedKey:    " + imprimirArrayBites(encodedKey));
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    public static String encriptar(String textoPlano, String llaveEncrip) {
        String salida = "";
        SecretKey secretKey = StringToSecretKey(llaveEncrip);
        byte[] IV = genSalt(IV_LARGO);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);

        try {
            Cipher encrCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encrCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] byteEncrText = encrCipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            String IVText = Base64.encodeToString(IV, Base64.NO_WRAP);
            String encrText = Base64.encodeToString(byteEncrText, Base64.NO_WRAP);
            salida = IVText + encrText;
        } catch(Exception ex) {
            Log.e(TAG, "Error al encriptar.", ex);
        }
        return salida;
    }

    public static String desencriptar(String textoEncrip, String contrasenna) {
        //Log.i(TAG, "Desencriptador");
        //Log.i(TAG, "Texto Encript: " + textoEncrip);
        //Log.i(TAG, "Llave Encript: " + contrasenna);

        String salida = "";
        SecretKey secretKey = StringToSecretKey(contrasenna);
        String base64IV = textoEncrip.substring(0, IV_LARGO_BASE64);
        byte[] IV = Base64.decode(base64IV, Base64.NO_WRAP);
        //Log.i(TAG, "IV:            " + imprimirArrayBites(IV));
        IvParameterSpec ivSpec = new IvParameterSpec(IV);

        try {
            String base64EncrData = textoEncrip.substring(IV_LARGO_BASE64);
            byte[] byteEncrData = Base64.decode(base64EncrData, Base64.NO_WRAP);
            //Log.i(TAG, "byteEncrData:  " + imprimirArrayBites(byteEncrData));
            Cipher desencrCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            desencrCipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] bytePlainText = desencrCipher.doFinal(byteEncrData);
            //Log.i(TAG, "bytePlainText: " + imprimirArrayBites(bytePlainText));
            salida = new String(bytePlainText, StandardCharsets.UTF_8);
            if (salida.length() == 0) {
                Log.e(TAG, "Error al desencriptar - Resultado vacío...");
            }
        } catch(Exception ex) {
            Log.e(TAG, "Error al desencriptar.", ex);
        }
        return salida;
    }

    private static String imprimirArrayBites(byte[] entrada) {
        StringBuilder debug = new StringBuilder();
        for (byte b: entrada) {
            if (debug.length() > 0) {
                debug.append(", ");
            }
            debug.append(String.format("%4d", b));
        }
        return "[" + debug + "]";
    }
}
