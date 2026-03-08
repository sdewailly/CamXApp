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

import android.util.Log;
import com.android.vending.licensing.util.URIQueryDecoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Non-caching policy.
 */
public class StrictPolicy implements Policy {

    private static final String TAG = "StrictPolicy";

    private int mLastResponse;
    private String mLicensingUrl;

    public StrictPolicy() {
        mLastResponse = Policy.RETRY;
        mLicensingUrl = null;
    }

    public void processServerResponse(int response, ResponseData rawData) {
        mLastResponse = response;

        if (response == Policy.NOT_LICENSED) {
            Map<String, String> extras = decodeExtras(rawData);
            mLicensingUrl = extras.get("LU");
        }
    }

    public boolean allowAccess() {
        return (mLastResponse == Policy.LICENSED);
    }

    public String getLicensingUrl() {
        return mLicensingUrl;
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
