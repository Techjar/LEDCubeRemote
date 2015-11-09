package com.techjar.ledcr.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 *
 * @author Techjar
 */
public final class Util {
    private Util() {
    }

    public static String stackTraceToString(Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }

    public static <T> List<T> arrayAsListCopy(T... array) {
        List<T> list = new ArrayList<>();
        list.addAll(Arrays.asList(array));
        return list;
    }

    public static void shuffleArray(Object[] array, Random random) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            Object obj = array[index];
            array[index] = array[i];
            array[i] = obj;
        }
    }

    public static void shuffleArray(int[] array, Random random) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int obj = array[index];
            array[index] = array[i];
            array[i] = obj;
        }
    }

    public static void shuffleArray(Object[] array) {
        shuffleArray(array, new Random());
    }

    /**
     * Will parse a valid IPv4/IPv6 address and port, may return garbage for invalid address formats. If no port was parsed it will be -1.
     */
    public static IPInfo parseIPAddress(String str) throws UnknownHostException {
        String ip;
        int port = -1;
        boolean ipv6 = false;
        if (str.indexOf(':') != -1) {
            if (str.indexOf('[') != -1 && str.indexOf(']') != -1) {
                ip = str.substring(1, str.indexOf(']'));
                port = Integer.parseInt(str.substring(str.indexOf(']') + 2));
                ipv6 = true;
            } else if (str.indexOf(':') == str.lastIndexOf(':')) {
                ip = str.substring(0, str.indexOf(':'));
                port = Integer.parseInt(str.substring(str.indexOf(':') + 1));
            } else ip = str;
        } else ip = str;
        return new IPInfo(InetAddress.getByName(ip), port, ipv6);
    }

    public static String getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        fis.read(bytes);
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        fis.close();
        return sb.toString();
    }

    public static String getFileMD5(String file) throws IOException, NoSuchAlgorithmException {
        return getFileMD5(new File(file));
    }

    /**
     * Compresses the byte array using deflate algorithm.
     */
    public static byte[] compresssBytes(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(out);
        dos.write(bytes);
        dos.flush(); dos.close();
        return out.toByteArray();
    }

    /**
     * Decompresses the byte array using deflate algorithm.
     */
    public static byte[] decompresssBytes(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterOutputStream dos = new InflaterOutputStream(out);
        dos.write(bytes);
        dos.flush(); dos.close();
        return out.toByteArray();
    }

    public static float[] floatListToArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static short floatToShortBits(float fval) {
        int fbits = Float.floatToIntBits(fval);
        int sign = fbits >>> 16 & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        if(val >= 0x47800000) {
            if((fbits & 0x7fffffff) >= 0x47800000) {
                if(val < 0x7f800000) return (short)(sign | 0x7c00);
                return (short)(sign | 0x7c00 | (fbits & 0x007fffff) >>> 13);
            }
            return (short)(sign | 0x7bff);
        }
        if(val >= 0x38800000) return (short)(sign | val - 0x38000000 >>> 13);
        if(val < 0x33000000) return (short)(sign);
        val = (fbits & 0x7fffffff) >>> 23;
        return (short)(sign | ((fbits & 0x7fffff | 0x800000) + (0x800000 >>> val - 102) >>> 126 - val));
    }

    public static float shortBitsToFloat(short hbits) {
        int mant = hbits & 0x03ff;
        int exp =  hbits & 0x7c00;
        if(exp == 0x7c00) exp = 0x3fc00;
        else if(exp != 0) {
            exp += 0x1c000;
            if(mant == 0 && exp > 0x1c400) return Float.intBitsToFloat((hbits & 0x8000) << 16 | exp << 13 | 0x3ff);
        }
        else if(mant != 0) {
            exp = 0x1c400;
            do {
                mant <<= 1;
                exp -= 0x400;
            } while((mant & 0x400) == 0);
            mant &= 0x3ff;
        }
        return Float.intBitsToFloat((hbits & 0x8000) << 16 | (exp | mant) << 13);
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096]; int count;
        while ((count = in.read(bytes, 0, bytes.length)) != -1) {
            out.write(bytes, 0, count);
        }
        return out.toByteArray();
    }

    public static String readFile(File file) throws FileNotFoundException, IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = readFully(in);
        in.close();
        return new String(bytes, "UTF-8");
    }

    public static long microTime() {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }

    public static long bytesToMB(long bytes) {
        return bytes / 1048576;
    }

    public static String bytesToMBString(long bytes) {
        return bytesToMB(bytes) + " MB";
    }

    public static String colorToString(Color color, boolean alpha) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + (alpha ? "," + color.getAlpha() : "");
    }

    public static Color stringToColor(String str) {
        String[] split = str.split(",");
        if (split.length < 3) throw new IllegalArgumentException("Too few color components or wrong delimiter");
        Color color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        if (split.length >= 4) color.setAlpha(Integer.parseInt(split[3]));
        return color;
    }

    public static int getNextPowerOfTwo(int number) {
        int ret = Integer.highestOneBit(number);
        return ret < number ? ret << 1 : ret;
    }

    public static boolean isPowerOfTwo(int number) {
        return (number != 0) && (number & (number - 1)) == 0;
    }

    public static final class IPInfo {
        private InetAddress address;
        private int port;
        private boolean ipv6;

        private IPInfo(InetAddress address, int port, boolean ipv6) {
            this.address = address;
            this.port = port;
            this.ipv6 = ipv6;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public boolean isIPv6() {
            return ipv6;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IPInfo other = (IPInfo)obj;
            if (this.address != other.address && (this.address == null || !this.address.equals(other.address))) {
                return false;
            }
            if (this.port != other.port) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.address != null ? this.address.hashCode() : 0);
            hash = 67 * hash + this.port;
            return hash;
        }

        @Override
        public String toString() {
            return port < 0 ? address.getHostAddress() : ipv6 ? '[' + address.getHostAddress() + "]:" + port : address.getHostAddress() + ':' + port;
        }
    }
}
