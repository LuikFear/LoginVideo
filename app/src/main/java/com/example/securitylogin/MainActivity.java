package com.example.securitylogin;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnIn;
    private  DataBaseHelper databasehelper;
    private KeyStore keyStore;
    private String aliasEmail = "EmailAlias";
    private String aliasPassword = "PasswordAlias";
    private int emailCounter = 1;
    private int passwordCounter = 1;

    static  byte[] Iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databasehelper = new DataBaseHelper(this);

        editTextEmail = findViewById(R.id.EditTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnIn = findViewById(R.id.BtnIn);

        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (!isValidEmail(email)) {
                    Toast.makeText(MainActivity.this, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
                } else if (!isValidPassword(password)) {
                    Toast.makeText(MainActivity.this, "Se necesita una contraseña con al menos 6 dígitos y al menos 1 número", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Key keyEmail = generateAESKey();
                        Key keyPassword = generateAESKey();

                        String emailAlias = aliasEmail + emailCounter++;
                        String passwordAlias = aliasPassword + passwordCounter++;


                        byte[] encryptedPassword = encryptData(password, keyPassword).getBytes();
                        byte[] encryptedEmail = encryptData(email, keyEmail).getBytes();

                        saveToKeyStore(emailAlias, keyEmail);
                        saveToKeyStore(passwordAlias, keyPassword);

                        String encryptedEmailStr = new String(encryptedEmail, StandardCharsets.ISO_8859_1);
                        String encryptedPasswordStr = new String(encryptedPassword, StandardCharsets.ISO_8859_1);

                        long newRowId = databasehelper.insertUser(encryptedEmailStr, encryptedPasswordStr,Iv);

                        saveToKeyStore(aliasEmail, keyEmail);
                        saveToKeyStore(aliasPassword, keyPassword);


                        boolean userExists = databasehelper.checkIfUserExists(email);


                        String decryptedEmail = decryptData(encryptedEmailStr, keyEmail);
                        String decryptedPassword = decryptData(encryptedPasswordStr, keyPassword);


                        if (userExists && decryptedEmail.equals(email) && decryptedPassword.equals(password)) {
                            Toast.makeText(MainActivity.this, "Bienvenido de Nuevo <3", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Bienvenido Nuevo Usuario", Toast.LENGTH_SHORT).show();
                        }

                        // Videoplayer
                        Intent intent = new Intent(MainActivity.this, VideoPlayer.class);
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al cifrar o descifrar los datos", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToKeyStore(String alias, Key key) {
        try {
            keyStore.setEntry(alias, new KeyStore.SecretKeyEntry((SecretKey) key), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Key generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private String encryptData(String data, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        Iv= cipher.getIV();
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    private boolean isValidEmail(String email) {
        String patronEmail = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.matches(patronEmail);
    }
    private boolean isValidPassword(String password) {
        String patronPassword = "^(?=.*[0-9]).{6,}$";
        return password.matches(patronPassword);
    }

    private String decryptData(String encryptedData, Key key) throws Exception {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(Iv);
        byte[] encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, key,ivParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}