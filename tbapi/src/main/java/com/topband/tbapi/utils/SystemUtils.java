package com.topband.tbapi.utils;

import android.annotation.SuppressLint;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class SystemUtils {

    // su 别名
    private static String sSuAlias = "";

    /**
     * 获取属性
     *
     * @param key          key
     * @param defaultValue default value
     * @return 属性值
     */
    @SuppressLint("PrivateApi")
    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * 设置属性
     *
     * @param key   key
     * @param value value
     */
    @SuppressLint("PrivateApi")
    public static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取内存大小
     *
     * @param context Context
     * @param type    类型(MemTotal\MemFree\MemAvailable等)
     * @return 内存大小（单位：Byte）
     */
    public static long getMemSize(Context context, String type) {
        try {
            FileReader fr = new FileReader("/proc/meminfo");
            BufferedReader br = new BufferedReader(fr, 4 * 1024);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(type)) {
                    break;
                }
            }
            br.close();

            //“\\s”表示：空格，回车，换行等空白符。 ”+“号表示一个或多个的意思
            String[] array = line.split("\\s+");

            return Long.parseLong(array[1])*1024;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 获取存储空间大小（>= Android O）
     *
     * @param context     Context
     * @param storageUuid UUID
     * @return 存储空间大小
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getStorageSize(Context context, UUID storageUuid) {
        try {
            StorageStatsManager stats = context.getSystemService(StorageStatsManager.class);
            return stats.getTotalBytes(storageUuid != null ? storageUuid : StorageManager.UUID_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取除系统外存储空间大小
     *
     * @return 存储空间大小（单位：Byte）
     */
    public static long getStorageSize() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());

        return statFs.getTotalBytes();
    }

    /**
     * 获取su命令别名（个别项目会重命名su）
     *
     * @return su别名
     */
    public static String getSuAlias() {
        if (TextUtils.isEmpty(sSuAlias)) {
            sSuAlias = getProperty("ro.su_alias", "su");
        }
        return sSuAlias;
    }
}
