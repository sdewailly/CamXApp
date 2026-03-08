/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.vending.licensing;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;

import com.android.vending.licensing.util.Base64;
import com.android.vending.licensing.util.Base64DecoderException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Client library for Google Play license verifications.
 */
public class LicenseChecker implements ServiceConnection {
    private static final String TAG = "LicenseChecker";

    private static final String KEY_FACTORY_ALGORITHM = "RSA";

    // Timeout value (in milliseconds) for calls to service.
    private static final int TIMEOUT_MS = 10 * 1000;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final boolean DEBUG_LICENSE_ERROR = false;

    private ILicensingService mService;

    private PublicKey mPublicKey;
    private final Context mContext;
    private final Policy mPolicy;
    private Handler mHandler;
    private final String mPackageName;
    private final String mVersionCode;
    private final Set<LicenseValidator> mChecksInProgress = new HashSet<LicenseValidator>();
    private final Queue<LicenseValidator> mPendingChecks = new LinkedList<LicenseValidator>();

    public LicenseChecker(Context context, Policy policy, String encodedPublicKey) {
        mContext = context;
        mPolicy = policy;
        mPublicKey = generatePublicKey(encodedPublicKey);
        mPackageName = mContext.getPackageName();
        mVersionCode = getVersionCode(context, mPackageName);
        HandlerThread handlerThread = new HandlerThread("background thread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    private static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);

            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (Base64DecoderException e) {
            Log.e(TAG, "Could not decode from Base64.");
            throw new IllegalArgumentException(e);
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, "Invalid key specification.");
            throw new IllegalArgumentException(e);
        }
    }

    public synchronized void checkAccess(LicenseCheckerCallback callback) {
        if (mPolicy.allowAccess()) {
            Log.i(TAG, "Using cached license response");
            callback.allow(Policy.LICENSED);
        } else {
            LicenseValidator validator = new LicenseValidator(mPolicy, new NullDeviceLimiter(),
                    callback, generateNonce(), mPackageName, mVersionCode);

            if (mService == null) {
                Log.i(TAG, "Binding to licensing service.");
                try {
                    boolean bindResult = mContext
                            .bindService(
                                    new Intent(
                                            new String(
                                                    Base64.decode(
                                                            "Y29tLmFuZHJvaWQudmVuZGluZy5saWNlbnNpbmcuSUxpY2Vuc2luZ1NlcnZpY2U=")))
                                                                    .setPackage(
                                                                            new String(
                                                                                    Base64.decode(
                                                                                            "Y29tLmFuZHJvaWQudmVuZGluZw=="))),
                                    this,
                                    Context.BIND_AUTO_CREATE);
                    if (bindResult) {
                        mPendingChecks.offer(validator);
                    } else {
                        Log.e(TAG, "Could not bind to service.");
                        handleServiceConnectionError(validator);
                    }
                } catch (SecurityException e) {
                    callback.applicationError(LicenseCheckerCallback.ERROR_MISSING_PERMISSION);
                } catch (Base64DecoderException e) {
                    e.printStackTrace();
                }
            } else {
                mPendingChecks.offer(validator);
                runChecks();
            }
        }
    }

    public void followLastLicensingUrl(Context context) {
        String licensingUrl = mPolicy.getLicensingUrl();
        if (licensingUrl == null) {
            licensingUrl = "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        }
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(licensingUrl));
        marketIntent.setPackage("com.android.vending");
        if (!(context instanceof Activity)) {
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(marketIntent);
    }

    private void runChecks() {
        LicenseValidator validator;
        while ((validator = mPendingChecks.poll()) != null) {
            try {
                Log.i(TAG, "Calling checkLicense on service for " + validator.getPackageName());
                mService.checkLicense(
                        validator.getNonce(), validator.getPackageName(),
                        new ResultListener(validator));
                mChecksInProgress.add(validator);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException in checkLicense call.", e);
                handleServiceConnectionError(validator);
            }
        }
    }

    private synchronized void finishCheck(LicenseValidator validator) {
        mChecksInProgress.remove(validator);
        if (mChecksInProgress.isEmpty()) {
            cleanupService();
        }
    }

    private class ResultListener extends ILicenseResultListener.Stub {
        private final LicenseValidator mValidator;
        private Runnable mOnTimeout;

        public ResultListener(LicenseValidator validator) {
            mValidator = validator;
            mOnTimeout = new Runnable() {
                public void run() {
                    Log.i(TAG, "Check timed out.");
                    handleServiceConnectionError(mValidator);
                    finishCheck(mValidator);
                }
            };
            startTimeout();
        }

        public void verifyLicense(final int responseCode, final String signedData,
                final String signature) {
            mHandler.post(new Runnable() {
                public void run() {
                    Log.i(TAG, "Received response.");
                    if (mChecksInProgress.contains(mValidator)) {
                        clearTimeout();
                        mValidator.verify(mPublicKey, responseCode, signedData, signature);
                        finishCheck(mValidator);
                    }
                }
            });
        }

        private void startTimeout() {
            Log.i(TAG, "Start monitoring timeout.");
            mHandler.postDelayed(mOnTimeout, TIMEOUT_MS);
        }

        private void clearTimeout() {
            Log.i(TAG, "Clearing timeout.");
            mHandler.removeCallbacks(mOnTimeout);
        }
    }

    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        mService = ILicensingService.Stub.asInterface(service);
        runChecks();
    }

    public synchronized void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "Service unexpectedly disconnected.");
        mService = null;
    }

    private synchronized void handleServiceConnectionError(LicenseValidator validator) {
        mPolicy.processServerResponse(Policy.RETRY, null);

        if (mPolicy.allowAccess()) {
            validator.getCallback().allow(Policy.RETRY);
        } else {
            validator.getCallback().dontAllow(Policy.RETRY);
        }
    }

    private void cleanupService() {
        if (mService != null) {
            try {
                mContext.unbindService(this);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unable to unbind from licensing service (already unbound)");
            }
            mService = null;
        }
    }

    public synchronized void onDestroy() {
        cleanupService();
        mHandler.getLooper().quit();
    }

    private int generateNonce() {
        return RANDOM.nextInt();
    }

    private static String getVersionCode(Context context, String packageName) {
        try {
            return String.valueOf(
                    context.getPackageManager().getPackageInfo(packageName, 0).versionCode);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package not found. could not get version code.");
            return "";
        }
    }
}
