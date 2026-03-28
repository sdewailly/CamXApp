// Portions copyright 2002, Google, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.android.vending.licensing.util;

/**
 * Base64 converter class.
 */
public class Base64 {
    public final static boolean ENCODE = true;
    public final static boolean DECODE = false;
    private final static byte EQUALS_SIGN = (byte) '=';
    private final static byte NEW_LINE = (byte) '\n';

    private final static byte[] ALPHABET =
        {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
         (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K',
         (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P',
         (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
         (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
         (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e',
         (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
         (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o',
         (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't',
         (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y',
         (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3',
         (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8',
         (byte) '9', (byte) '+', (byte) '/'};

    private final static byte[] DECODABET = {
        -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
        -5,-5,                                      // Whitespace: Tab and Linefeed
        -9,-9,                                      // Decimal 11 - 12
        -5,                                         // Whitespace: Carriage Return
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
        -9,-9,-9,-9,-9,                             // Decimal 27 - 31
        -5,                                         // Whitespace: Space
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
        62,                                         // Plus sign at decimal 43
        -9,-9,-9,                                   // Decimal 44 - 46
        63,                                         // Slash at decimal 47
        52,53,54,55,56,57,58,59,60,61,              // Numbers zero through nine: 48 - 57
        -9,-9,-9,                                   // Decimal 58 - 60
        -1,                                         // Equals sign at decimal 61
        -9,-9,-9,                                   // Decimal 62 - 64
        0,1,2,3,4,5,6,7,8,9,10,11,12,13,            // Letters 'A' through 'N': 65 - 78
        14,15,16,17,18,19,20,21,22,23,24,25,        // Letters 'O' through 'Z': 79 - 90
        -9,-9,-9,-9,-9,-9,                          // Decimal 91 - 96
        26,27,28,29,30,31,32,33,34,35,36,37,38,     // Letters 'a' through 'm': 97 - 109
        39,40,41,42,43,44,45,46,47,48,49,50,51,     // Letters 'n' through 'z': 110 - 122
        -9,-9,-9,-9,-9                              // Decimal 123 - 127
    };

    private final static byte WHITE_SPACE_ENC = -5;
    private final static byte EQUALS_SIGN_ENC = -1;

    private Base64() {}

    public static byte[] decode(String s) throws Base64DecoderException {
        byte[] bytes = s.getBytes();
        return decode(bytes, 0, bytes.length);
    }

    public static byte[] decode(byte[] source, int off, int len) throws Base64DecoderException {
        int len34 = len * 3 / 4;
        byte[] outBuff = new byte[2 + len34];
        int outBuffPosn = 0;
        byte[] b4 = new byte[4];
        int b4Posn = 0;
        for (int i = 0; i < len; i++) {
            byte sbiCrop = (byte) (source[i + off] & 0x7f);
            byte sbiDecode = DECODABET[sbiCrop];
            if (sbiDecode >= WHITE_SPACE_ENC) {
                if (sbiDecode >= EQUALS_SIGN_ENC) {
                    if (sbiCrop == EQUALS_SIGN) {
                        break;
                    }
                    b4[b4Posn++] = sbiCrop;
                    if (b4Posn == 4) {
                        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
                        b4Posn = 0;
                    }
                }
            } else {
                throw new Base64DecoderException("Bad Base64 input character at " + i + ": " + source[i + off] + "(decimal)");
            }
        }
        if (b4Posn != 0) {
            if (b4Posn == 1) {
                throw new Base64DecoderException("single trailing character at offset " + (len - 1));
            }
            b4[b4Posn++] = EQUALS_SIGN;
            outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
        }
        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }

    private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
        if (source[srcOffset + 2] == EQUALS_SIGN) {
            int outBuff = ((DECODABET[source[srcOffset]] << 24) >>> 6) | ((DECODABET[source[srcOffset + 1]] << 24) >>> 12);
            destination[destOffset] = (byte) (outBuff >>> 16);
            return 1;
        } else if (source[srcOffset + 3] == EQUALS_SIGN) {
            int outBuff = ((DECODABET[source[srcOffset]] << 24) >>> 6) | ((DECODABET[source[srcOffset + 1]] << 24) >>> 12) | ((DECODABET[source[srcOffset + 2]] << 24) >>> 18);
            destination[destOffset] = (byte) (outBuff >>> 16);
            destination[destOffset + 1] = (byte) (outBuff >>> 8);
            return 2;
        } else {
            int outBuff = ((DECODABET[source[srcOffset]] << 24) >>> 6) | ((DECODABET[source[srcOffset + 1]] << 24) >>> 12) | ((DECODABET[source[srcOffset + 2]] << 24) >>> 18) | ((DECODABET[source[srcOffset + 3]] << 24) >>> 24);
            destination[destOffset] = (byte) (outBuff >> 16);
            destination[destOffset + 1] = (byte) (outBuff >> 8);
            destination[destOffset + 2] = (byte) (outBuff);
            return 3;
        }
    }

    public static String encode(byte[] source) {
        byte[] outBuff = encode(source, 0, source.length);
        return new String(outBuff);
    }

    public static byte[] encode(byte[] source, int off, int len) {
        int lenDiv3 = (len + 2) / 3;
        int len43 = lenDiv3 * 4;
        byte[] outBuff = new byte[len43];
        int d = 0;
        int e = 0;
        for (; d < len - 2; d += 3, e += 4) {
            int inBuff = ((source[d + off] << 24) >>> 8) | ((source[d + 1 + off] << 24) >>> 16) | ((source[d + 2 + off] << 24) >>> 24);
            outBuff[e] = ALPHABET[(inBuff >>> 18)];
            outBuff[e + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
            outBuff[e + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
            outBuff[e + 3] = ALPHABET[(inBuff) & 0x3f];
        }
        if (d < len) {
            int inBuff = (d + 0 < len ? ((source[d + off] << 24) >>> 8) : 0) | (d + 1 < len ? ((source[d + 1 + off] << 24) >>> 16) : 0);
            outBuff[e] = ALPHABET[(inBuff >>> 18)];
            outBuff[e + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
            outBuff[e + 2] = (d + 1 < len) ? ALPHABET[(inBuff >>> 6) & 0x3f] : EQUALS_SIGN;
            outBuff[e + 3] = EQUALS_SIGN;
        }
        return outBuff;
    }
}
