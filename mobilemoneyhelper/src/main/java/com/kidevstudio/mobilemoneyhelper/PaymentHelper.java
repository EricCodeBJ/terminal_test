package com.kidevstudio.mobilemoneyhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PaymentHelper {

    private static final String BASE_PAYMENT_URL = "https://test-terminal.hetudes.com/payments/process?amount=";
    private static final String CHECK_TRANSACTION_URL = "https://test-terminal.hetudes.com/payments/check?transactionId=%s&amount=%d";

    // Method to start payment with a callback
    public static void startPayment(Activity activity, int amount, PaymentCallback callback) {
        String paymentUrl = BASE_PAYMENT_URL + amount;
        Intent intent = new Intent(activity, PaymentWebViewActivity.class);
        intent.putExtra("paymentUrl", paymentUrl);
        intent.putExtra("amount", String.valueOf(amount));
        PaymentWebViewActivity.setPaymentCallback(callback);
        activity.startActivity(intent);
    }

    // Asynchronous method to check transaction status
    public static void checkTransactionAsync(String transactionId, int amount, TransactionStatusCallback callback) {
        new TransactionCheckTask(transactionId, amount, callback).execute();
    }

    // AsyncTask for network call
    private static class TransactionCheckTask extends AsyncTask<Void, Void, JSONObject> {
        private final String transactionId;
        private final int amount;
        private final TransactionStatusCallback callback;

        public TransactionCheckTask(String transactionId, int amount, TransactionStatusCallback callback) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.callback = callback;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            @SuppressLint("DefaultLocale") String requestUrl = String.format(CHECK_TRANSACTION_URL, transactionId, amount);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder().url(requestUrl).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return new JSONObject(responseBody);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (callback != null) {
                callback.onTransactionCheckComplete(result != null, result);
            }
        }
    }

    // Callback interface for transaction check result
    public interface TransactionStatusCallback {
        void onTransactionCheckComplete(boolean success, JSONObject result);
    }

    // Callback interface for payment completion
    public interface PaymentCallback {
        void onTransactionCompleted(Boolean status, String transactionId);
    }
}
