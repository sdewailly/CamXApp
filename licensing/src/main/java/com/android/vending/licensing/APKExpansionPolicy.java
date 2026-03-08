/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.vending.licensing.util.URIQueryDecoder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Default policy. All policy decisions are based off of response data received
 * from the licensing service.
 */
public class APKExpansionPolicy implements Policy {

    private static final String TAG = "APKExpansionPolicy";
    private static final String PREFS_FILE = "com.android.vending.licensing.APKExpansionPolicy";
    private static final String PREF_LAST_RESPONSE = "lastResponse";
    private static final String PREF_VALIDITY_TIMESTAMP = "validityTimestamp";
    private static final String PREF_RETRY_UNTIL = "retryUntil";
    private static final String PREF_MAX_RETRIES = "maxRetries";
    private static final String PREF_RETRY_COUNT = "retryCount";
    private static final String PREF_LICENSING_URL = "licensingUrl";
    private static final String DEFAULT_VALIDITY_TIMESTAMP = "0";
    private static final String DEFAULT_RETRY_UNTIL = "0";
    private static final String DEFAULT_MAX_RETRIES = "0";
    private static final String DEFAULT_RETRY_COUNT = "0";

    private static final long MILLIS_PER_MINUTE = 60 * 1000;

    private long mValidityTimestamp;
    private long mRetryUntil;
    private long mMaxRetries;
    private long mRetryCount;
    private long mLastResponseTime = 0;
    private int mLastResponse;
    private String mLicensingUrl;
    private PreferenceObfuscator mPreferences;
    private Vector<String> mExpansionURLs = new Vector<String>();
    private Vector<String> mExpansionFileNames = new Vector<String>();
    private Vector<Long> mExpansionFileSizes = new Vector<Long>();

    public static final int MAIN_FILE_URL_INDEX = 0;
    public static final int PATCH_FILE_URL_INDEX = 1;

    public APKExpansionPolicy(Context context, Obfuscator obfuscator) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mPreferences = new PreferenceObfuscator(sp, obfuscator);
        mLastResponse = Integer.parseInt(
                mPreferences.getString(PREF_LAST_RESPONSE, Integer.toString(Policy.RETRY)));
        mValidityTimestamp = Long.parseLong(mPreferences.getString(PREF_VALIDITY_TIMESTAMP,
                DEFAULT_VALIDITY_TIMESTAMP));
        mRetryUntil = Long.parseLong(mPreferences.getString(PREF_RETRY_UNTIL, DEFAULT_RETRY_UNTIL));
        mMaxRetries = Long.parseLong(mPreferences.getString(PREF_MAX_RETRIES, DEFAULT_MAX_RETRIES));
        mRetryCount = Long.parseLong(mPreferences.getString(PREF_RETRY_COUNT, DEFAULT_RETRY_COUNT));
        mLicensingUrl = mPreferences.getString(PREF_LICENSING_URL, null);
    }

    public void resetPolicy() {
        mPreferences.putString(PREF_LAST_RESPONSE, Integer.toString(Policy.RETRY));
        setRetryUntil(DEFAULT_RETRY_UNTIL);
        setMaxRetries(DEFAULT_MAX_RETRIES);
        setRetryCount(Long.parseLong(DEFAULT_RETRY_COUNT));
        setValidityTimestamp(DEFAULT_VALIDITY_TIMESTAMP);
        mPreferences.commit();
    }

    public void processServerResponse(int response, ResponseData rawData) {
        if (response != Policy.RETRY) {
            setRetryCount(0);
        } else {
            setRetryCount(mRetryCount + 1);
        }

        Map<String, String> extras = decodeExtras(rawData);
        if (response == Policy.LICENSED) {
            mLastResponse = response;
            setLicensingUrl(null);
            setValidityTimestamp(Long.toString(System.currentTimeMillis() + MILLIS_PER_MINUTE));
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                if (key.equals("VT")) {
                    setValidityTimestamp(extras.get(key));
                } else if (key.equals("GT")) {
                    setRetryUntil(extras.get(key));
                } else if (key.equals("GR")) {
                    setMaxRetries(extras.get(key));
                } else if (key.startsWith("FILE_URL")) {
                    int index = Integer.parseInt(key.substring("FILE_URL".length())) - 1;
                    setExpansionURL(index, extras.get(key));
                } else if (key.startsWith("FILE_NAME")) {
                    int index = Integer.parseInt(key.substring("FILE_NAME".length())) - 1;
                    setExpansionFileName(index, extras.get(key));
                } else if (key.startsWith("FILE_SIZE")) {
                    int index = Integer.parseInt(key.substring("FILE_SIZE".length())) - 1;
                    setExpansionFileSize(index, Long.parseLong(extras.get(key)));
                }
            }
        } else if (response == Policy.NOT_LICENSED) {
            setValidityTimestamp(DEFAULT_VALIDITY_TIMESTAMP);
            setRetryUntil(DEFAULT_RETRY_UNTIL);
            setMaxRetries(DEFAULT_MAX_RETRIES);
            setLicensingUrl(extras.get("LU"));
        }

        setLastResponse(response);
        mPreferences.commit();
    }

    private void setLastResponse(int l) {
        mLastResponseTime = System.currentTimeMillis();
        mLastResponse = l;
        mPreferences.putString(PREF_LAST_RESPONSE, Integer.toString(l));
    }

    private void setRetryCount(long c) {
        mRetryCount = c;
        mPreferences.putString(PREF_RETRY_COUNT, Long.toString(c));
    }

    public long getRetryCount() {
        return mRetryCount;
    }

    private void setValidityTimestamp(String validityTimestamp) {
        Long lValidityTimestamp;
        try {
            lValidityTimestamp = Long.parseLong(validityTimestamp);
        } catch (NumberFormatException e) {
            Log.w(TAG, "License validity timestamp (VT) missing, caching for a minute");
            lValidityTimestamp = System.currentTimeMillis() + MILLIS_PER_MINUTE;
            validityTimestamp = Long.toString(lValidityTimestamp);
        }

        mValidityTimestamp = lValidityTimestamp;
        mPreferences.putString(PREF_VALIDITY_TIMESTAMP, validityTimestamp);
    }

    public long getValidityTimestamp() {
        return mValidityTimestamp;
    }

    private void setRetryUntil(String retryUntil) {
        Long lRetryUntil;
        try {
            lRetryUntil = Long.parseLong(retryUntil);
        } catch (NumberFormatException e) {
            Log.w(TAG, "License retry timestamp (GT) missing, grace period disabled");
            retryUntil = "0";
            lRetryUntil = 0l;
        }

        mRetryUntil = lRetryUntil;
        mPreferences.putString(PREF_RETRY_UNTIL, retryUntil);
    }

    public long getRetryUntil() {
        return mRetryUntil;
    }

    private void setMaxRetries(String maxRetries) {
        Long lMaxRetries;
        try {
            lMaxRetries = Long.parseLong(maxRetries);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Licence retry count (GR) missing, grace period disabled");
            maxRetries = "0";
            lMaxRetries = 0l;
        }

        mMaxRetries = lMaxRetries;
        mPreferences.putString(PREF_MAX_RETRIES, maxRetries);
    }

    public long getMaxRetries() {
        return mMaxRetries;
    }

    private void setLicensingUrl(String url) {
        mLicensingUrl = url;
        mPreferences.putString(PREF_LICENSING_URL, url);
    }

    public String getLicensingUrl() {
        return mLicensingUrl;
    }

    public int getExpansionURLCount() {
        return mExpansionURLs.size();
    }

    public String getExpansionURL(int index) {
        if (index < mExpansionURLs.size()) {
            return mExpansionURLs.elementAt(index);
        }
        return null;
    }

    public void setExpansionURL(int index, String URL) {
        if (index >= mExpansionURLs.size()) {
            mExpansionURLs.setSize(index + 1);
        }
        mExpansionURLs.set(index, URL);
    }

    public String getExpansionFileName(int index) {
        if (index < mExpansionFileNames.size()) {
            return mExpansionFileNames.elementAt(index);
        }
        return null;
    }

    public void setExpansionFileName(int index, String name) {
        if (index >= mExpansionFileNames.size()) {
            mExpansionFileNames.setSize(index + 1);
        }
        mExpansionFileNames.set(index, name);
    }

    public long getExpansionFileSize(int index) {
        if (index < mExpansionFileSizes.size()) {
            return mExpansionFileSizes.elementAt(index);
        }
        return -1;
    }

    public void setExpansionFileSize(int index, long size) {
        if (index >= mExpansionFileSizes.size()) {
            mExpansionFileSizes.setSize(index + 1);
        }
        mExpansionFileSizes.set(index, size);
    }

    public boolean allowAccess() {
        long ts = System.currentTimeMillis();
        if (mLastResponse == Policy.LICENSED) {
            if (ts <= mValidityTimestamp) {
                return true;
            }
        } else if (mLastResponse == Policy.RETRY &&
                ts < mLastResponseTime + MILLIS_PER_MINUTE) {
            return (ts <= mRetryUntil || mRetryCount <= mMaxRetries);
        }
        return false;
    }

    private Map<String, String> decodeExtras(ResponseData rawData) {
        Map<String, String> results = new HashMap<String, String>();
        if (rawData == null) {
            return results;
        }

        try {
            URI rawExtras = new URI("?" + rawData.extra);
            URIQueryDecoder.DecodeQuery(rawExtras, results);
        } catch (URISyntaxException e) {
            Log.w(TAG, "Invalid syntax error while decoding extras data from server.");
        }
        return results;
    }

}
