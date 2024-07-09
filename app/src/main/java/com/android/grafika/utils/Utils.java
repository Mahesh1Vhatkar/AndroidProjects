package com.android.grafika.utils;

import android.content.Context;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.Deflater;


public class Utils {

    public static String convertToDisplayFormat(long date) {
        String dateStr = "";
        try {
            if (date > 0) {
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date d = new Date(date);
                System.out.println(sdf.format(d));
                dateStr = sdf.format(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dateStr;
    }

    public static long getTimestamp(String dateTime, String format) {
        try {
            // Create a SimpleDateFormat object with the given format
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);

            // Parse the date and time string to a Date object
            Date date = dateFormat.parse(dateTime);

            // Return the timestamp (milliseconds since epoch)
            return date.getTime();
        } catch (ParseException e) {
            // Handle the exception if the date and time string is not in the expected format
            System.err.println("Invalid date and time format: " + e.getMessage());
            return -1;
        }
    }
    public static String convertByteStreamToHex(InputStream inputStream) throws IOException {
        StringBuilder hexStringBuilder = new StringBuilder();
        int byteRead;

        // Read each byte from the InputStream
        while ((byteRead = inputStream.read()) != -1) {
            // Convert each byte to a 2-digit hex string and append it to the StringBuilder
            hexStringBuilder.append(String.format("%02X", byteRead));
        }

        return hexStringBuilder.toString();
    }

    public static String getCurrentDateTime() {
        String indianTime = "";

        java.text.SimpleDateFormat utcFormatter = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = utcFormatter.format(new Date());

        Date utcTimeInstance = null;
        try {
            utcTimeInstance = utcFormatter.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        java.text.SimpleDateFormat indianFormatter = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        indianFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        indianTime = indianFormatter.format(utcTimeInstance);
        return indianTime;
    }

    public static String getDateCurrentTimeZone(long timestamp) {
        try {
            Calendar calendar = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                calendar = Calendar.getInstance();
                TimeZone tz = TimeZone.getTimeZone("IST");
                calendar.setTimeInMillis(timestamp * 1000);
                calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currenTimeZone = (Date) calendar.getTime();
                return sdf.format(currenTimeZone);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getDateForFile() {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
        return date;
    }

    public static String getPreviousDate() {
        final Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DATE, -1);
        return dateFormat.format(cal.getTime());
    }

    public static String encodeString(String s) {
        String base64Encoded = "";
        byte[] data = new byte[0];
        try {
            data = s.getBytes(StandardCharsets.UTF_8);
        } finally {
            base64Encoded = Base64.encodeToString(data, Base64.NO_WRAP);
        }
        return base64Encoded;
    }


    public static String decodeString(String s) {
        String base64Encoded = "";

        try {
            base64Encoded = new String(Base64.decode(s, Base64.NO_WRAP));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64Encoded;
    }

    public static byte[] decodeByteArray(String s) {
        byte[] base64Encoded = null;

        try {
            base64Encoded = Base64.decode(s, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64Encoded;
    }
    public static String decodeBase64String(String s ){
        byte[] decodedBytes = Base64.decode(s,Base64.NO_WRAP);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }
    public static String compressAndEncode(String str) throws UnsupportedEncodingException {
        Deflater def = new Deflater();
        String finalStr = "";
        for (int i = 0; i < 3; i++)
            finalStr += str;
        def.setInput(finalStr.getBytes("UTF-8"));
        def.finish();
        byte compString[] = new byte[1024];
        int compSize = def.deflate(compString, 3, 13, Deflater.FULL_FLUSH);
        return new String(compString);
    }

    public static String convertToString(byte[] array, boolean isHex) {
        StringBuilder stringBuilder = new StringBuilder(array.length);
        for (byte byteChar : array) {
            // Log.w("Utils", "convertToString "+ (isHex ? String.format("%02X ", byteChar) : "" + byteChar));
            stringBuilder.append((isHex ? String.format("%02X ", byteChar) : "" + byteChar));
        }

        return stringBuilder.toString();
    }

    public static float byteToFlot(byte[] configByteArray) {
        long data = 0;
        return ByteBuffer.wrap(configByteArray).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    public static int byteToInt(byte[] configByteArray) {
        return ByteBuffer.wrap(configByteArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }


    public static long byteToLong(byte[] configByteArray) {
        long data = 0;
        return ByteBuffer.wrap(configByteArray).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public static short byteToShort(byte[] configByteArray) {
        return ByteBuffer.wrap(configByteArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }


    public static byte[] longToByteArray(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(value);
        return buffer.array();
    }

    public static byte[] shortToByteArray(short value) {
        byte[] bytes = new byte[2];
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(value);
        return buffer.array();
    }

    public static byte[] convertToByteArray(float value) {
        byte[] bytes = new byte[4];
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(value);
        return buffer.array();

    }

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This was deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

}
