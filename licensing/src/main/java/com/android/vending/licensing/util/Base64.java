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

  private final static byte[] WEBSAFE_ALPHABET =
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
          (byte) '9', (byte) '-', (byte) '_'};

  private final static byte[] DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, 
      -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 
      -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 
      52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 
      -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 
      -9, -9, -9, -9, -9};

  private final static byte[] WEBSAFE_DECODABET =
      {-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 
          -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, 
          52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 
          -9, -9, -9, -9, 63, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 
          -9, -9, -9, -9, -9};

  private final static byte WHITE_SPACE_ENC = -5;
  private final static byte EQUALS_SIGN_ENC = -1;

  private Base64() {}

  private static byte[] encode3to4(byte[] source, int srcOffset,
      int numSigBytes, byte[] destination, int destOffset, byte[] alphabet) {
    int inBuff =
        (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
            | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
            | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

    switch (numSigBytes) {
      case 3:
        destination[destOffset] = alphabet[(inBuff >>> 18)];
        destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 0x3f];
        destination[destOffset + 3] = alphabet[(inBuff) & 0x3f];
        return destination;
      case 2:
        destination[destOffset] = alphabet[(inBuff >>> 18)];
        destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 0x3f];
        destination[destOffset + 3] = EQUALS_SIGN;
        return destination;
      case 1:
        destination[destOffset] = alphabet[(inBuff >>> 18)];
        destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = EQUALS_SIGN;
        destination[destOffset + 3] = EQUALS_SIGN;
        return destination;
      default:
        return destination;
    }
  }

  public static String encode(byte[] source) {
    return encode(source, 0, source.length, ALPHABET, true);
  }

  public static String encodeWebSafe(byte[] source, boolean doPadding) {
    return encode(source, 0, source.length, WEBSAFE_ALPHABET, doPadding);
  }

  public static String encode(byte[] source, int off, int len, byte[] alphabet,
      boolean doPadding) {
    byte[] outBuff = encode(source, off, len, alphabet, Integer.MAX_VALUE);
    int outLen = outBuff.length;
    while (doPadding == false && outLen > 0) {
      if (outBuff[outLen - 1] != '=') {
        break;
      }
      outLen -= 1;
    }
    return new String(outBuff, 0, outLen);
  }

  public static byte[] encode(byte[] source, int off, int len, byte[] alphabet,
      int maxLineLength) {
    int lenDiv3 = (len + 2) / 3;
    int len43 = lenDiv3 * 4;
    byte[] outBuff = new byte[len43 + (len43 / maxLineLength)];
    int d = 0;
    int e = 0;
    int len2 = len - 2;
    int lineLength = 0;
    for (; d < len2; d += 3, e += 4) {
      int inBuff =
          ((source[d + off] << 24) >>> 8)
              | ((source[d + 1 + off] << 24) >>> 16)
              | ((source[d + 2 + off] << 24) >>> 24);
      outBuff[e] = alphabet[(inBuff >>> 18)];
      outBuff[e + 1] = alphabet[(inBuff >>> 12) & 0x3f];
      outBuff[e + 2] = alphabet[(inBuff >>> 6) & 0x3f];
      outBuff[e + 3] = alphabet[(inBuff) & 0x3f];
      lineLength += 4;
      if (lineLength == maxLineLength) {
        outBuff[e + 4] = NEW_LINE;
        e++;
        lineLength = 0;
      }
    }
    if (d < len) {
      encode3to4(source, d + off, len - d, outBuff, e, alphabet);
      lineLength += 4;
      if (lineLength == maxLineLength) {
        outBuff[e + 4] = NEW_LINE;
        e++;
      }
      e += 4;
    }
    return outBuff;
  }

  private static int decode4to3(byte[] source, int srcOffset,
      byte[] destination, int destOffset, byte[] decodabet) {
    if (source[srcOffset + 2] == EQUALS_SIGN) {
      int outBuff =
          ((decodabet[source[srcOffset]] << 24) >>> 6)
              | ((decodabet[source[srcOffset + 1]] << 24) >>> 12);
      destination[destOffset] = (byte) (outBuff >>> 16);
      return 1;
    } else if (source[srcOffset + 3] == EQUALS_SIGN) {
      int outBuff =
          ((decodabet[source[srcOffset]] << 24) >>> 6)
              | ((decodabet[source[srcOffset + 1]] << 24) >>> 12)
              | ((decodabet[source[srcOffset + 2]] << 24) >>> 18);
      destination[destOffset] = (byte) (outBuff >>> 16);
      destination[destOffset + 1] = (byte) (outBuff >>> 8);
      return 2;
    } else {
      int outBuff =
          ((decodabet[source[srcOffset]] << 24) >>> 6)
              | ((decodabet[source[srcOffset + 1]] << 24) >>> 12)
              | ((decodabet[source[srcOffset + 2]] << 24) >>> 18)
              | ((decodabet[source[srcOffset + 3]] << 24) >>> 24);
      destination[destOffset] = (byte) (outBuff >> 16);
      destination[destOffset + 1] = (byte) (outBuff >> 8);
      destination[destOffset + 2] = (byte) (outBuff);
      return 3;
    }
  }

  public static byte[] decode(String s) throws Base64DecoderException {
    byte[] bytes = s.getBytes();
    return decode(bytes, 0, bytes.length);
  }

  public static byte[] decodeWebSafe(String s) throws Base64DecoderException {
    byte[] bytes = s.getBytes();
    return decodeWebSafe(bytes, 0, bytes.length);
  }

  public static byte[] decode(byte[] source) throws Base64DecoderException {
    return decode(source, 0, source.length);
  }

  public static byte[] decodeWebSafe(byte[] source)
      throws Base64DecoderException {
    return decodeWebSafe(source, 0, source.length);
  }

  public static byte[] decode(byte[] source, int off, int len)
      throws Base64DecoderException {
    return decode(source, off, len, DECODABET);
  }

  public static byte[] decodeWebSafe(byte[] source, int off, int len)
      throws Base64DecoderException {
    return decode(source, off, len, WEBSAFE_DECODABET);
  }

  public static byte[] decode(byte[] source, int off, int len, byte[] decodabet)
      throws Base64DecoderException {
    int len34 = len * 3 / 4;
    byte[] outBuff = new byte[2 + len34];
    int outBuffPosn = 0;
    byte[] b4 = new byte[4];
    int b4Posn = 0;
    int i = 0;
    byte sbiCrop = 0;
    byte sbiDecode = 0;
    for (i = 0; i < len; i++) {
      sbiCrop = (byte) (source[i + off] & 0x7f);
      sbiDecode = decodabet[sbiCrop];
      if (sbiDecode >= WHITE_SPACE_ENC) {
        if (sbiDecode >= EQUALS_SIGN_ENC) {
          if (sbiCrop == EQUALS_SIGN) {
            int bytesLeft = len - i;
            byte lastByte = (byte) (source[len - 1 + off] & 0x7f);
            if (b4Posn == 0 || b4Posn == 1) {
              throw new Base64DecoderException("invalid padding byte '=' at byte offset " + i);
            } else if ((b4Posn == 3 && bytesLeft > 2) || (b4Posn == 4 && bytesLeft > 1)) {
              throw new Base64DecoderException("padding byte '=' falsely signals end of encoded value at offset " + i);
            } else if (lastByte != EQUALS_SIGN && lastByte != NEW_LINE) {
              throw new Base64DecoderException("encoded value has invalid trailing byte");
            }
            break;
          }
          b4[b4Posn++] = sbiCrop;
          if (b4Posn == 4) {
            outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, decodabet);
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
      outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, decodabet);
    }
    byte[] out = new byte[outBuffPosn];
    System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
    return out;
  }
}
