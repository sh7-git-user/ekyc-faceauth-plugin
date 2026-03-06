package com.bank.faceauth;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import com.sdk.CLServices;
import com.sdk.CLRemoteResultReceiver;
import com.sdk.ServiceConnectionStatusNotifier;
import com.sdk.Constant;

public class FaceAuthPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("faceAuth")) {

            Activity activity = cordova.getActivity();
            String salt = args.getString(0);

            cordova.getThreadPool().execute(() -> {

                try {

                    String cred = "{\"CredAllowed\":[{\"type\":\"BIOMETRIC\",\"subtype\":\"FACE_AUTH\"}]}";

                    String keyCode = "EKYC";
                    String langPref = "en_US";

                    CLServices.initService(activity, new ServiceConnectionStatusNotifier() {

                        @Override
                        public void serviceConnected(CLServices services) {

                            Constant.clServices = services;

                            CLRemoteResultReceiver receiver =
                                    new CLRemoteResultReceiver(new ResultReceiver(new Handler()) {

                                        @Override
                                        protected void onReceiveResult(int resultCode, Bundle resultData) {

                                            if (resultData != null) {

                                                callbackContext.success(resultData.toString());

                                            } else {

                                                callbackContext.error("Empty result from FaceAuth");

                                            }

                                        }

                                    });

                            Constant.clServices.getCredential(
                                    keyCode,
                                    "",
                                    cred,
                                    "",
                                    salt,
                                    "",
                                    "",
                                    langPref,
                                    receiver
                            );
                        }

                        @Override
                        public void serviceDisconnected() {

                            callbackContext.error("SDK service disconnected");

                        }

                    });

                } catch (Exception e) {

                    callbackContext.error(e.getMessage());

                }

            });

            return true;
        }

        return false;
    }
}
