/*
 * Copyright (c) 2013, OpenCloudDB/HotDB and/or its affiliates. All rights reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software;Designed and Developed mainly by many Chinese opensource volunteers.
 * you can redistribute it and/or modify it under the terms of the GNU General Public License
 * version 2 only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version 2 along with this work;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 */
package net.data.technology.jraft;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.data.technology.jraft.Security.AES128;
import net.data.technology.jraft.Security.SEA128;


/**
 * @author hotdb
 */
public class StringUtil {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final Random RANDOM = new Random();
    private static final char[] CHARS = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'q', 'w',
            'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l',
            'z', 'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P',
            'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'};
    /**
     * The empty String {@code ""}.
     * 
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * 字符串hash算法：s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1] <br>
     * 其中s[]为字符串的字符数组，换算成程序的表达式为：<br>
     * h = 31*h + s.charAt(i); => h = (h << 5) - h + s.charAt(i); <br>
     * 
     * @param start hash for s.substring(start, end)
     * @param end hash for s.substring(start, end)
     */
    public static long hash(String s, int start, int end) {
        if (start < 0) {
            start = 0;
        }
        if (end > s.length()) {
            end = s.length();
        }
        long h = 0;
        for (int i = start; i < end; ++i) {
            h = (h << 5) - h + s.charAt(i);
        }
        return h & 0x7FFFFFFFFFFFFFFFL;
    }

    public static String getRandomString(int size) {
        StringBuilder s = new StringBuilder(size);
        int len = CHARS.length;
        for (int i = 0; i < size; i++) {
            int x = RANDOM.nextInt();
            s.append(CHARS[(x < 0 ? -x : x) % len]);
        }
        return s.toString();
    }

    public static String safeToString(Object object) {
        try {
            return object.toString();
        } catch (Throwable t) {
            return "<toString() failure: " + t + ">";
        }
    }

    public static boolean isEmpty(String str) {
        return ((str == null) || (str.length() == 0));
    }


    public static byte[] hexString2Bytes(char[] hexString, int offset, int length) {
        if (hexString == null)
            return null;
        if (length == 0)
            return EMPTY_BYTE_ARRAY;
        boolean odd = length << 31 == Integer.MIN_VALUE;
        byte[] bs = new byte[odd ? (length + 1) >> 1 : length >> 1];
        for (int i = offset, limit = offset + length; i < limit; ++i) {
            char high, low;
            if (i == offset && odd) {
                high = '0';
                low = hexString[i];
            } else {
                high = hexString[i];
                low = hexString[++i];
            }
            int b;
            switch (high) {
                case '0':
                    b = 0;
                    break;
                case '1':
                    b = 0x10;
                    break;
                case '2':
                    b = 0x20;
                    break;
                case '3':
                    b = 0x30;
                    break;
                case '4':
                    b = 0x40;
                    break;
                case '5':
                    b = 0x50;
                    break;
                case '6':
                    b = 0x60;
                    break;
                case '7':
                    b = 0x70;
                    break;
                case '8':
                    b = 0x80;
                    break;
                case '9':
                    b = 0x90;
                    break;
                case 'a':
                case 'A':
                    b = 0xa0;
                    break;
                case 'b':
                case 'B':
                    b = 0xb0;
                    break;
                case 'c':
                case 'C':
                    b = 0xc0;
                    break;
                case 'd':
                case 'D':
                    b = 0xd0;
                    break;
                case 'e':
                case 'E':
                    b = 0xe0;
                    break;
                case 'f':
                case 'F':
                    b = 0xf0;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "illegal hex-string: " + new String(hexString, offset, length));
            }
            switch (low) {
                case '0':
                    break;
                case '1':
                    b += 1;
                    break;
                case '2':
                    b += 2;
                    break;
                case '3':
                    b += 3;
                    break;
                case '4':
                    b += 4;
                    break;
                case '5':
                    b += 5;
                    break;
                case '6':
                    b += 6;
                    break;
                case '7':
                    b += 7;
                    break;
                case '8':
                    b += 8;
                    break;
                case '9':
                    b += 9;
                    break;
                case 'a':
                case 'A':
                    b += 10;
                    break;
                case 'b':
                case 'B':
                    b += 11;
                    break;
                case 'c':
                case 'C':
                    b += 12;
                    break;
                case 'd':
                case 'D':
                    b += 13;
                    break;
                case 'e':
                case 'E':
                    b += 14;
                    break;
                case 'f':
                case 'F':
                    b += 15;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "illegal hex-string: " + new String(hexString, offset, length));
            }
            bs[(i - offset) >> 1] = (byte) b;
        }
        return bs;
    }

    public static String dumpAsHex(byte[] src, int length) {
        StringBuilder out = new StringBuilder(length * 4);
        int p = 0;
        int rows = length / 8;
        for (int i = 0; (i < rows) && (p < length); i++) {
            int ptemp = p;
            for (int j = 0; j < 8; j++) {
                String hexVal = Integer.toHexString(src[ptemp] & 0xff);
                if (hexVal.length() == 1)
                    out.append('0');
                out.append(hexVal).append(' ');
                ptemp++;
            }
            out.append("    ");
            for (int j = 0; j < 8; j++) {
                int b = 0xff & src[p];
                if (b > 32 && b < 127) {
                    out.append((char) b).append(' ');
                } else {
                    out.append(". ");
                }
                p++;
            }
            out.append('\n');
        }
        int n = 0;
        for (int i = p; i < length; i++) {
            String hexVal = Integer.toHexString(src[i] & 0xff);
            if (hexVal.length() == 1)
                out.append('0');
            out.append(hexVal).append(' ');
            n++;
        }
        for (int i = n; i < 8; i++) {
            out.append("   ");
        }
        out.append("    ");
        for (int i = p; i < length; i++) {
            int b = 0xff & src[i];
            if (b > 32 && b < 127) {
                out.append((char) b).append(' ');
            } else {
                out.append(". ");
            }
        }
        out.append('\n');
        return out.toString();
    }

    public static byte[] escapeEasternUnicodeByteStream(byte[] src, String srcString, int offset,
            int length) {
        if ((src == null) || (src.length == 0))
            return src;
        int bytesLen = src.length;
        int bufIndex = 0;
        int strIndex = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytesLen);
        while (true) {
            if (srcString.charAt(strIndex) == '\\') {// write it out as-is
                out.write(src[bufIndex++]);
            } else {// Grab the first byte
                int loByte = src[bufIndex];
                if (loByte < 0)
                    loByte += 256; // adjust for signedness/wrap-around
                out.write(loByte);// We always write the first byte
                if (loByte >= 0x80) {
                    if (bufIndex < (bytesLen - 1)) {
                        int hiByte = src[bufIndex + 1];
                        if (hiByte < 0)
                            hiByte += 256; // adjust for signedness/wrap-around
                        out.write(hiByte);// write the high byte here, and
                                          // increment the index for the high
                                          // byte
                        bufIndex++;
                        if (hiByte == 0x5C)
                            out.write(hiByte);// escape 0x5c if necessary
                    }
                } else if (loByte == 0x5c) {
                    if (bufIndex < (bytesLen - 1)) {
                        int hiByte = src[bufIndex + 1];
                        if (hiByte < 0)
                            hiByte += 256; // adjust for signedness/wrap-around
                        if (hiByte == 0x62) {// we need to escape the 0x5c
                            out.write(0x5c);
                            out.write(0x62);
                            bufIndex++;
                        }
                    }
                }
                bufIndex++;
            }
            if (bufIndex >= bytesLen)
                break;// we're done
            strIndex++;
        }
        return out.toByteArray();
    }

    public static String toString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (byte byt : bytes) {
            buffer.append((char) byt);
        }
        return buffer.toString();
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equalsIgnoreCase(str2);
    }

    public static int countChar(String str, char c) {
        if (str == null || str.isEmpty())
            return 0;
        final int len = str.length();
        int cnt = 0;
        for (int i = 0; i < len; ++i) {
            if (c == str.charAt(i)) {
                ++cnt;
            }
        }
        return cnt;
    }

    public static String replaceOnce(String text, String repl, String with) {
        return replace(text, repl, with, 1);
    }

    public static String replace(String text, String repl, String with) {
        return replace(text, repl, with, -1);
    }

    public static String replace(String text, String repl, String with, int max) {
        if ((text == null) || (repl == null) || (with == null) || (repl.length() == 0)
                || (max == 0)) {
            return text;
        }
        StringBuffer buf = new StringBuffer(text.length());
        int start = 0;
        int end = 0;
        while ((end = text.indexOf(repl, start)) != -1) {
            buf.append(text.substring(start, end)).append(with);
            start = end + repl.length();
            if (--max == 0) {
                break;
            }
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static String replaceChars(String str, char searchChar, char replaceChar) {
        if (str == null) {
            return null;
        }
        return str.replace(searchChar, replaceChar);
    }

    public static String replaceChars(String str, String searchChars, String replaceChars) {
        if ((str == null) || (str.length() == 0) || (searchChars == null)
                || (searchChars.length() == 0)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        boolean modified = false;
        for (int i = 0, isize = searchChars.length(); i < isize; i++) {
            char searchChar = searchChars.charAt(i);
            if ((replaceChars == null) || (i >= replaceChars.length())) {// 删除
                int pos = 0;
                for (int j = 0; j < len; j++) {
                    if (chars[j] != searchChar) {
                        chars[pos++] = chars[j];
                    } else {
                        modified = true;
                    }
                }
                len = pos;
            } else {// 替换
                for (int j = 0; j < len; j++) {
                    if (chars[j] == searchChar) {
                        chars[j] = replaceChars.charAt(i);
                        modified = true;
                    }
                }
            }
        }
        if (!modified) {
            return str;
        }
        return new String(chars, 0, len);
    }

    public static long hash(String key) {
        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
        int seed = 0x1234ABCD;
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        long m = 0xc6a4a7935bd1e995L;
        int r = 47;
        long h = seed ^ (buf.remaining() * m);
        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();
            k *= m;
            k ^= k >>> r;
            k *= m;
            h ^= k;
            h *= m;
        }
        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }
        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;
        buf.order(byteOrder);
        return h & Long.MAX_VALUE;
    }

    public static String escapeSpecialCharacter(String str) {
        if (str.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]", "")
                .length() < 1) {
            return str;
        }
        String clean_string = str;
        clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
        clean_string = clean_string.replaceAll("\\n", "\\\\n");
        clean_string = clean_string.replaceAll("\\r", "\\\\r");
        clean_string = clean_string.replaceAll("\\t", "\\\\t");
        clean_string = clean_string.replaceAll("\\00", "\\\\0");
        clean_string = clean_string.replaceAll("'", "\\\\'");
        clean_string = clean_string.replaceAll("\\\"", "\\\\\"");
        if (clean_string
                .replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/?\\\\\"' ]", "")
                .length() < 1) {
            return clean_string;
        }
        return clean_string;
    }

    /*
     * 判断是数值类型，包括double和float和int 以及负数
     * 
     * @param str 传入的字符串
     * 
     * @return 是浮点数返回true,否则返回false
     */
    public static boolean isNumber(String str) {
        return null != str && str.matches("^-?[0-9]+(.[0-9]+)?$");
    }

    /**
     * 获取密码
     * 
     * @param plainText
     * @param isCryptMandatory 是否加密
     * @return
     * @throws Exception 如加密时 且 解密密码失败
     */
    public static String getDecryptPassword(String id, String plainText, boolean isCryptMandatory)
            throws Exception {
        String db_password = "";
        if (!plainText.equals("")) {
            byte[] source = id.getBytes();
            byte[] code = new byte[Security.ENCRYPT_CODE.length + source.length];
            System.arraycopy(Security.ENCRYPT_CODE, 0, code, 0, Security.ENCRYPT_CODE.length);
            System.arraycopy(source, 0, code, Security.ENCRYPT_CODE.length, source.length);
            db_password = SEA128.INSTANCE.decrypt(Security.Utils.parseHexStr2Byte(plainText), code);
            if (!isCryptMandatory) {
                if (plainText.length() % 32 != 0) {
                    db_password = plainText;
                }
            }
            if (db_password == null) {
                throw new Exception("Failed to decrypt password !");
            }
        } else {
            db_password = plainText;
        }
        return db_password;
    }

    /**
     * 加密密码
     * 
     * @param plainText
     * @param isCryptMandatory
     * @return
     * @throws UnsupportedEncodingException
     * @throws Exception 解密密码失败
     */
    public static String getEncryptPassword(String password, String id) {
        return AES128.INSTANCE.encrypt(password.getBytes(), new String(Security.ENCRYPT_CODE) + id);
    }

    /**
     * 
     * @return Liunx 下获取主机名
     */
    public static String getHostNameForLiunx() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage(); // host = "hostname: hostname"
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }

    /**
     * 
     * @return 获取主机名 兼容windows和Liunx
     */
    public static String getHostName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME")) {
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            return env.get("HOSTNAME");
        } else {
            return getHostNameForLiunx();
        }
    }

    /**
     * <p>
     * Splits the provided text into an array, separator string specified.
     * </p>
     *
     * <p>
     * The separator is not included in the returned String array. Adjacent separators are treated
     * as separators for empty tokens. For more control over the split use the StrTokenizer class.
     * </p>
     *
     * <p>
     * A {@code null} input String returns {@code null}. A {@code null} separator splits on
     * whitespace.
     * </p>
     *
     * <pre>
     * StringUtils.splitByWholeSeparatorPreserveAllTokens(null, *)               = null
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("", *)                 = []
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null)      = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null)    = ["ab", "", "", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
     * </pre>
     *
     * @param str the String to parse, may be null
     * @param separator String containing the String to be used as a delimiter, {@code null} splits
     *        on whitespace
     * @return an array of parsed Strings, {@code null} if null String was input
     * @since 2.4
     */
    public static String[] splitByWholeSeparatorPreserveAllTokens(final String str,
            final String separator) {
        return splitByWholeSeparatorWorker(str, separator, -1, true);
    }

    /**
     * Performs the logic for the {@code splitByWholeSeparatorPreserveAllTokens} methods.
     *
     * @param str the String to parse, may be {@code null}
     * @param separator String containing the String to be used as a delimiter, {@code null} splits
     *        on whitespace
     * @param max the maximum number of elements to include in the returned array. A zero or
     *        negative value implies no limit.
     * @param preserveAllTokens if {@code true}, adjacent separators are treated as empty token
     *        separators; if {@code false}, adjacent separators are treated as one separator.
     * @return an array of parsed Strings, {@code null} if null String input
     * @since 2.4
     */
    private static String[] splitByWholeSeparatorWorker(final String str, final String separator,
            final int max, final boolean preserveAllTokens) {
        if (str == null) {
            return null;
        }

        final int len = str.length();

        if (len == 0) {
            return new String[0];
        }

        if (separator == null || EMPTY.equals(separator)) {
            // Split on whitespace.
            return splitWorker(str, null, max, preserveAllTokens);
        }

        final int separatorLength = separator.length();

        final ArrayList<String> substrings = new ArrayList<String>();
        int numberOfSubstrings = 0;
        int beg = 0;
        int end = 0;
        while (end < len) {
            end = str.indexOf(separator, beg);

            if (end > -1) {
                if (end > beg) {
                    numberOfSubstrings += 1;

                    if (numberOfSubstrings == max) {
                        end = len;
                        substrings.add(str.substring(beg));
                    } else {
                        // The following is OK, because String.substring( beg, end ) excludes
                        // the character at the position 'end'.
                        substrings.add(str.substring(beg, end));

                        // Set the starting point for the next search.
                        // The following is equivalent to beg = end + (separatorLength - 1) + 1,
                        // which is the right calculation:
                        beg = end + separatorLength;
                    }
                } else {
                    // We found a consecutive occurrence of the separator, so skip it.
                    if (preserveAllTokens) {
                        numberOfSubstrings += 1;
                        if (numberOfSubstrings == max) {
                            end = len;
                            substrings.add(str.substring(beg));
                        } else {
                            substrings.add(EMPTY);
                        }
                    }
                    beg = end + separatorLength;
                }
            } else {
                // String.substring( beg ) goes from 'beg' to the end of the String.
                substrings.add(str.substring(beg));
                end = len;
            }
        }

        return substrings.toArray(new String[substrings.size()]);
    }

    /**
     * Performs the logic for the {@code split} and {@code splitPreserveAllTokens} methods that
     * return a maximum array length.
     *
     * @param str the String to parse, may be {@code null}
     * @param separatorChars the separate character
     * @param max the maximum number of elements to include in the array. A zero or negative value
     *        implies no limit.
     * @param preserveAllTokens if {@code true}, adjacent separators are treated as empty token
     *        separators; if {@code false}, adjacent separators are treated as one separator.
     * @return an array of parsed Strings, {@code null} if null String input
     */
    private static String[] splitWorker(final String str, final String separatorChars,
            final int max, final boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        final List<String> list = new ArrayList<String>();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            final char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 判断null 转为"" 输出，否则原样返回
     * 
     * @param str
     * @return
     */
    public static String getNullToEmptyStr(String str) {
        return str == null ? "" : str;
    }

}
