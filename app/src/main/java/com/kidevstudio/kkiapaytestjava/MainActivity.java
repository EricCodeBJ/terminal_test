package com.kidevstudio.kkiapaytestjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.util.Log;

import com.kidevstudio.mobilemoneyhelper.PaymentHelper;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    AppCompatButton buttonTest;
    JSONObject transactionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonTest = findViewById(R.id.buttonTest);

        buttonTest.setOnClickListener(v -> {
            PaymentHelper.startPayment(MainActivity.this, 1500, new PaymentHelper.PaymentCallback() {
                @Override
                public void onTransactionCompleted(Boolean status, String transactionId) {
                    if (!status) {
                        // Transaction failed
                        return;
                    }
                    // ..... continue process
                }
            });
        });
    }
}