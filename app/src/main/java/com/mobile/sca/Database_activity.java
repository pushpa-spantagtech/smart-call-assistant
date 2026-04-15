package com.mobile.sca;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Database_activity extends AppCompatActivity {

    EditText etPhone;
    TextView tvResult;

    String API_URL = "https://script.google.com/macros/s/AKfycbxWeNsCF6sLEDOkuKVNg9UuQ-6x40JeKsSHv33LMQxjCMb_gATYXoqzdnlIX9qN8PPg3g/exec";

    public static Database_activity instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_database);

        instance = this;
        askPermission();

        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (telecomManager != null && !getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            startActivity(intent);
        }

        etPhone = findViewById(R.id.etPhone);
        tvResult = findViewById(R.id.tvResult);

        findViewById(R.id.btnAdd).setOnClickListener(v -> send(getApplicationContext(), "add"));
        findViewById(R.id.btnDelete).setOnClickListener(v -> send(getApplicationContext(), "delete"));
        findViewById(R.id.btnGet).setOnClickListener(v -> getAll());
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    100
            );
        }
    }

    public void sendNow(Context context, String action) {

        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL(API_URL);
                con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);
                con.setConnectTimeout(15000);
                con.setReadTimeout(15000);

                JSONObject json = new JSONObject();
                json.put("action", "add");
                json.put("phone", action);

                OutputStream os = con.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                // 🔥 THIS LINE IS REQUIRED
                int responseCode = con.getResponseCode();

                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String response = sb.toString();

//                runOnUiThread(() ->
//                        Toast.makeText(this,
//                                "Server: " + response,
//                                Toast.LENGTH_LONG).show()
//                );
                Log.e("Server", "Response: "+response);

            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                        Toast.makeText(this,
//                                "Error: " + e.getMessage(),
//                                Toast.LENGTH_LONG).show()
//                );
                Log.e("Error", "Error: " + e.getMessage());
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    public void send(Context context, String action) {
        String phone = etPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Enter phone", Toast.LENGTH_SHORT).show()
            );
            return;
        }

        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL(API_URL);
                con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);
                con.setConnectTimeout(15000);
                con.setReadTimeout(15000);

                JSONObject json = new JSONObject();
                json.put("action", action);
                json.put("phone", phone);

                OutputStream os = con.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                // 🔥 THIS LINE IS REQUIRED
                int responseCode = con.getResponseCode();

                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String response = sb.toString();

//                runOnUiThread(() ->
//                        Toast.makeText(this,
//                                "Server: " + response,
//                                Toast.LENGTH_LONG).show()
//                );
                Log.e("Server", "Response: "+response);

            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                        Toast.makeText(this,
//                                "Error: " + e.getMessage(),
//                                Toast.LENGTH_LONG).show()
//                );
                Log.e("Error", "Error: " + e.getMessage());
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    private void getAll() {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) sb.append(line);

                JSONArray arr = new JSONArray(sb.toString());
                StringBuilder data = new StringBuilder();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    data.append(o.getString("phone")).append("\n");
                }

                runOnUiThread(() -> tvResult.setText(data.toString()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}