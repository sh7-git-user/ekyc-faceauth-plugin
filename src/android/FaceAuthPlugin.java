package com.bank.faceauth;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import org.npci.upi.security.services.CLServices;
import org.npci.upi.security.services.CLRemoteResultReceiver;
import org.npci.upi.security.services.ServiceConnectionStatusNotifier;

public class FaceAuthPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (!action.equals("faceAuth")) {
            return false;
        }

        android.util.Log.d("FaceAuthPlugin", "FaceAuth action triggered");

        Activity activity = cordova.getActivity();
        String salt = args.getString(0);

        activity.runOnUiThread(() -> {

            try {

                String cred = "{\"CredAllowed\":[{\"type\":\"BIOMETRIC\",\"subtype\":\"FACE_AUTH\"}]}";
                String keyCode = "EKYC";
                String langPref = "en_US";

                String txnId = String.valueOf(System.currentTimeMillis());

                String deviceId = Settings.Secure.getString(
                        activity.getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );

                android.util.Log.d("FaceAuthPlugin", "Transaction ID: " + txnId);
                android.util.Log.d("FaceAuthPlugin", "Device ID: " + deviceId);

                CLServices.initService(activity, new ServiceConnectionStatusNotifier() {

                    @Override
                    public void serviceConnected(CLServices services) {

                        android.util.Log.d("FaceAuthPlugin", "RD service connected");

                        CLServices clServices = services;

                        CLRemoteResultReceiver receiver =
                                new CLRemoteResultReceiver(new ResultReceiver(new Handler()) {

                                    @Override
                                    protected void onReceiveResult(int resultCode, Bundle resultData) {

                                        android.util.Log.d("FaceAuthPlugin", "RD Result Code: " + resultCode);
                                        android.util.Log.d("FaceAuthPlugin", "RD Result Bundle: " + resultData);

                                        if (resultData != null) {

                                            String result;

                                            if (resultData.containsKey("PID_DATA")) {
                                                result = resultData.getString("PID_DATA");
                                            }
                                            else if (resultData.containsKey("PID_DATA_XML")) {
                                                result = resultData.getString("PID_DATA_XML");
                                            }
                                            else if (resultData.containsKey("RESULT")) {
                                                result = resultData.getString("RESULT");
                                            }
                                            else {
                                                result = resultData.toString();
                                            }

                                            android.util.Log.d("FaceAuthPlugin", "FaceAuth result: " + result);

                                            callbackContext.success(result);

                                        } else {

                                            android.util.Log.e("FaceAuthPlugin", "Empty result from FaceAuth");
                                            callbackContext.error("Empty result from FaceAuth");

                                        }
                                    }

                                });

                        try {

                            android.util.Log.d("FaceAuthPlugin", "Calling getCredential...");
                            String payer = "{\"payee\":\"user@upi\"}";
                            String saltJson = "{\"salt\":\"" + salt + "\"}";

                            clServices.getCredential(
                                    keyCode,
                                    txnId,
                                    cred,
                                    payer,
                                    saltJson,
                                    deviceId,
                                    "ANDROID",
                                    langPref,
                                    receiver
                            );

                        } catch (Exception e) {

                            android.util.Log.e("FaceAuthPlugin", "getCredential failed: " + e.getMessage());
                            callbackContext.error(e.getMessage());

                        }
                    }

                    @Override
                    public void serviceDisconnected() {

                        android.util.Log.e("FaceAuthPlugin", "SDK service disconnected");
                        callbackContext.error("SDK service disconnected");

                    }

                });

            } catch (Exception e) {

                android.util.Log.e("FaceAuthPlugin", "Exception: " + e.getMessage());
                callbackContext.error(e.getMessage());

            }

        });

        return true;
    }
}
