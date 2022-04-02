package com.topband.tbapi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.IGpioService;
import android.os.IMcuService;
import android.os.IWiegandService;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.ayst.androidx.IKeyInterceptService;
import com.ayst.androidx.ILog2fileService;
import com.ayst.androidx.IModemService;
import com.ayst.androidx.IOtgService;
import com.ayst.androidx.ITimeRTCService;
import com.ayst.romupgrade.IRomUpgradeService;
import com.topband.tbapi.utils.DeviceInfoUtils;
import com.topband.tbapi.utils.DnsUtils;
import com.topband.tbapi.utils.EthernetHelper;
import com.topband.tbapi.utils.EthernetHelperR;
import com.topband.tbapi.utils.IEthernetHelper;
import com.topband.tbapi.utils.InstallUtils;
import com.topband.tbapi.utils.ShellUtils;
import com.topband.tbapi.utils.SystemUtils;
import com.topband.tbapi.utils.UsbnetHelperR;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

public class TBManager {
    // 屏幕旋转角度
    public static final int SCREEN_ANGLE_0 = 0;
    public static final int SCREEN_ANGLE_90 = 90;
    public static final int SCREEN_ANGLE_180 = 180;
    public static final int SCREEN_ANGLE_270 = 270;
    // 网卡前缀
    public static final String IFACE_PREFIX_ETH = "eth";
    public static final String IFACE_PREFIX_USB = "usb";
    private static final String TAG = "TBManager";
    // API版本
    private static final String VERSION = "1.0.15";
    private Context mContext;
    private IMcuService mMcuService;
    private IGpioService mGpioService;
    private IWiegandService mWiegandService;
    private IRomUpgradeService mRomUpgradeService;
    private ILog2fileService mLog2fileService;
    private IModemService mModemService;
    private IKeyInterceptService mKeyInterceptService;
    private ITimeRTCService mTimingSwitchService;
    private IOtgService mOtgService;
    private IEthernetHelper mEthernetHelper;
    private IEthernetHelper mUsbnetHelper;
    private PowerManager mPowerManager;
    private DevicePolicyManager mDevicePolicyManager;
    private AudioManager mAudioManager;

    /**
     * 升级Service Connection
     */
    private ServiceConnection mRomUpgradeServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IRomUpgradeService, onServiceConnected...");
            mRomUpgradeService = IRomUpgradeService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IRomUpgradeService, onServiceDisconnected...");
            mRomUpgradeService = null;
        }
    };

    /**
     * Log2file Service Connection
     */
    private ServiceConnection mLog2fileServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "ILog2fileService, onServiceConnected...");
            mLog2fileService = ILog2fileService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "ILog2fileService, onServiceDisconnected...");
            mLog2fileService = null;
        }
    };

    /**
     * 4G保活Service Connection
     */
    private ServiceConnection mModemServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IModemService, onServiceConnected...");
            mModemService = IModemService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IModemService, onServiceDisconnected...");
            mModemService = null;
        }
    };

    /**
     * 事件拦截Service Connection
     */
    private ServiceConnection mKeyInterceptServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IKeyInterceptService, onServiceConnected...");
            mKeyInterceptService = IKeyInterceptService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IKeyInterceptService, onServiceDisconnected...");
            mKeyInterceptService = null;
        }
    };

    /**
     * 定时开关机Service Connection
     */
    private ServiceConnection mTimingSwitchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "ITimeRTCService, onServiceConnected...");
            mTimingSwitchService = ITimeRTCService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "ITimeRTCService, onServiceDisconnected...");
            mTimingSwitchService = null;
        }
    };

    /**
     * Otg Service Connection
     */
    private ServiceConnection mOtgServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IOtgService, onServiceConnected...");
            mOtgService = IOtgService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IOtgService, onServiceDisconnected...");
            mOtgService = null;
        }
    };

    public TBManager(Context context) {
        mContext = context;
    }

    @SuppressLint("PrivateApi")
    private static void activeAdmin(Context context) {
        ComponentName adminReceiver = new ComponentName(context, AdminReceiver.class);
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            Method setActiveAdmin = dpm.getClass().getDeclaredMethod("setActiveAdmin", ComponentName.class, boolean.class);
            setActiveAdmin.setAccessible(true);
            setActiveAdmin.invoke(dpm, adminReceiver, true);
        } catch (Exception e) {
            Log.e(TAG, "activeAdmin, " + e.getMessage());
        }
    }

    /**
     * 初始化
     */
    @SuppressLint("PrivateApi")
    public void init() {
        Log.i(TAG, "init, API Version: " + VERSION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mEthernetHelper = new EthernetHelperR(mContext);
        } else {
            mEthernetHelper = new EthernetHelper(mContext);
        }
        mUsbnetHelper = new UsbnetHelperR(mContext);

        // 获取MCU Service
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"mcu"});
            mMcuService = IMcuService.Stub.asInterface(binder);
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取GPIO Service
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"gpio"});
            mGpioService = IGpioService.Stub.asInterface(binder);
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取Wiegand Service
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"wiegand"});
            mWiegandService = IWiegandService.Stub.asInterface(binder);
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 绑定升级Service
        Intent intent = new Intent();
        intent.setPackage("com.ayst.romupgrade");
        intent.setAction("com.ayst.romupgrade.UPGRADE_SERVICE");
        mContext.bindService(intent, mRomUpgradeServiceConnection, Context.BIND_AUTO_CREATE);

        // 绑定Log2file Service
        intent = new Intent();
        intent.setPackage("com.ayst.androidx");
        intent.setAction("com.ayst.androidx.LOG2FILE_SERVICE");
        mContext.bindService(intent, mLog2fileServiceConnection, Context.BIND_AUTO_CREATE);

        // 绑定4G保活Service
        intent = new Intent();
        intent.setPackage("com.ayst.androidx");
        intent.setAction("com.ayst.androidx.MODEM_SERVICE");
        mContext.bindService(intent, mModemServiceConnection, Context.BIND_AUTO_CREATE);

        // 绑定事件拦截Service
        intent = new Intent();
        intent.setPackage("com.ayst.androidx");
        intent.setAction("com.ayst.androidx.KEY_INTERCEPT_SERVICE");
        mContext.bindService(intent, mKeyInterceptServiceConnection, Context.BIND_AUTO_CREATE);

        // 绑定定时开关机Service
        intent = new Intent();
        intent.setPackage("com.ayst.androidx");
        intent.setAction("com.ayst.androidx.TIMERTC_SREVICE");
        mContext.bindService(intent, mTimingSwitchServiceConnection, Context.BIND_AUTO_CREATE);

        // 绑定Otg Service
        intent = new Intent();
        intent.setPackage("com.ayst.androidx");
        intent.setAction("com.ayst.androidx.OTG_SERVICE");
        mContext.bindService(intent, mOtgServiceConnection, Context.BIND_AUTO_CREATE);

        // 激活设备管理权限
        activeAdmin(mContext);
    }

    /**
     * 清理
     */
    public void deinit() {
        Log.i(TAG, "deinit");

        mContext.unbindService(mRomUpgradeServiceConnection);
        mContext.unbindService(mLog2fileServiceConnection);
        mContext.unbindService(mModemServiceConnection);
        mContext.unbindService(mKeyInterceptServiceConnection);
        mContext.unbindService(mTimingSwitchServiceConnection);
        mContext.unbindService(mOtgServiceConnection);
    }

    /**
     * 获取API版本号
     *
     * @return 当前API版本号
     * 例如：1.0.1
     */
    public String getAPIVersion() {
        return VERSION;
    }

    /**
     * 获取android系统版本号
     *
     * @return android版本号
     * 例如：8.1.0
     */
    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取固件版本号
     *
     * @return 固件版本号
     * 例如：1.0.1
     */
    public String getFirmwareVersion() {
        return SystemUtils.getProperty("ro.topband.sw.version", "");
    }

    /**
     * 获取固件版本和编译日期
     *
     * @return 格式化固件版本号与编译时间
     * 例如：ls328-default-1.0.1-1-20200101.032407
     */
    public String getAndroidDisplay() {
        return Build.DISPLAY;
    }

    /**
     * 获取内核版本号
     *
     * @return 格式化内核版本号
     * 例如：Linux version 4.4.132 (tomcat@dqrd03) (gcc version 6.3.1 20170404
     * (Linaro GCC 6.3-2017.05) ) #285 SMP PREEMPT Wed Dec 30 02:23:56 CST 2020
     */
    public String getFormattedKernelVersion() {
        return DeviceInfoUtils.getFormattedKernelVersion();
    }

    /**
     * 获取设备型号
     *
     * @return 设备型号
     * 例如：topband
     */
    public String getModel() {
        return Build.MODEL;
    }

    /**
     * 获取内存大小
     *
     * @return 内存大小（单位：byte）
     */
    public long getMemorySize() {
        return SystemUtils.getMemSize(mContext, "MemTotal");
    }

    /**
     * 获取内部存储容量
     *
     * @return 内部存储空间大小（单位：byte）
     */
    public long getInternalStorageSize() {
        return SystemUtils.getStorageSize();
    }

    /**
     * 关机
     */
    public void shutdown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else {
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    /**
     * 重启
     */
    public void reboot() {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        mContext.sendBroadcast(intent);
    }

    /**
     * 设置定时开关机
     * <p>
     * 描述:
     * 如果设置了 关机日期 和 开机日期，则设备会在指定日期的指定时间点关机和开机。
     * 例如    设置2019-07-01 11:30关机，2019-07-02 13:30开机，设备将会在
     * 2019-07-01 11:30关机，13:30开机。开机后，需要重新设置定时开关机时间。
     * <p>
     * 如果仅仅设置 关机时间 和 开机时间，而不设置日期，则设备每天重复这个时间点关机和开机。
     * 例如    设置日期为空，时间为11:30关机，12:00开机，设备将每天在11:30关机，
     * 12:00开机，除非调用接口主动关闭定时开关机。
     *
     * @param offDate 关机日期（格式：YYYY-MM-DD，例如：2019-07-03）（可为空）
     * @param offTime 关机时间（格式：HH:MM，例如：23:30）
     * @param onDate  开机日期（格式：YYYY-MM-DD，例如：2019-07-03）（可为空）
     * @param onTime  开机时间（格式：HH:MM，例如：8:30）
     * @param enable  true：打开定时开关机，false：关闭定时开关机
     * @return true：设置成功，false：设置失败
     */
    public boolean setTimingSwitch(String offDate, @NonNull String offTime,
                                   String onDate, @NonNull String onTime,
                                   boolean enable) {
        try {
            JSONObject param = new JSONObject();
            param.put("cmd", enable ? "1" : "0");
            param.put("off_date", offDate);
            param.put("off_time", offTime);
            param.put("on_date", onDate);
            param.put("on_time", onTime);
            if (mTimingSwitchService != null) {
                try {
                    int ret = mTimingSwitchService.updateTimeToRtc(param.toString());
                    if (ret < 0) {
                        Log.e(TAG, "setTimingSwitch, error: " + ret);
                    } else {
                        return true;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "setTimingSwitch, " + e.getMessage());
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "setTimingSwitch, " + e.getMessage());
        }
        return false;
    }

    /**
     * 打开看门狗
     *
     * @return true：成功，false：失败
     */
    public boolean openWatchdog() {
        if (mMcuService != null) {
            try {
                int ret = mMcuService.openWatchdog();
                if (ret < 0) {
                    Log.e(TAG, "openWatchdog, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "openWatchdog, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 关闭看门狗
     *
     * @return true：成功，false：失败
     */
    public boolean closeWatchdog() {
        if (mMcuService != null) {
            try {
                int ret = mMcuService.closeWatchdog();
                if (ret < 0) {
                    Log.e(TAG, "closeWatchdog, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "closeWatchdog, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 喂狗一次，对看门狗计数进行复位操作
     *
     * @return true：成功，false：失败
     */
    public boolean watchdogFeed() {
        if (mMcuService != null) {
            try {
                int ret = mMcuService.heartbeat();
                if (ret < 0) {
                    Log.e(TAG, "watchdogFeed, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "watchdogFeed, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 设置看门狗超时时长
     *
     * @param timeout 看门狗超时时长（单位：秒）
     * @return true：成功，false：失败
     */
    public boolean setWatchdogTimeout(int timeout) {
        if (mMcuService != null) {
            try {
                int ret = mMcuService.setWatchdogDuration(timeout);
                if (ret < 0) {
                    Log.e(TAG, "setWatchdogTimeout, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "setWatchdogTimeout, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 获取看门狗超时时长
     *
     * @return 看门狗超时时长（单位：秒）
     */
    public int getWatchdogTimeout() {
        if (mMcuService != null) {
            try {
                return mMcuService.getWatchdogDuration();
            } catch (RemoteException e) {
                Log.e(TAG, "getWatchdogTimeout, " + e.getMessage());
            }
        }
        return 0;
    }

    /**
     * 判断看门狗是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean watchdogIsOpen() {
        if (mMcuService != null) {
            try {
                return mMcuService.watchdogIsOpen();
            } catch (RemoteException e) {
                Log.e(TAG, "watchdogIsOpen, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 截屏并保存为png图片格式到指定位置
     *
     * @param path 截屏保存文件路径（例如：/sdcard/screenshot.png）
     */
    public void screenshot(@NonNull String path) {
        ShellUtils.execCmd("screencap -p " + path, false);
    }

    /**
     * 设置屏幕逆时针旋转角度
     *
     * @param angle  屏幕旋转角度（取值：0、90、180、270）
     * @param reboot true：重启，false：不重启
     */
    public void setRotation(int angle, boolean reboot) {
        if (angle == SCREEN_ANGLE_0
                || angle == SCREEN_ANGLE_90
                || angle == SCREEN_ANGLE_180
                || angle == SCREEN_ANGLE_270) {

            SystemUtils.setProperty("persist.sys.sf.hwrotation", String.valueOf(angle));

            if (reboot) {
                Log.w(TAG, "setRotation, reboot");
                reboot();
            }
        } else {
            Log.e(TAG, "setRotation, unsupported angle");
        }
    }

    /**
     * 获取屏幕当前角度
     *
     * @return 屏幕旋转角度（取值：0、90、180、270）
     */
    public int getRotation() {
        return Integer.parseInt(SystemUtils.getProperty(
                "persist.sys.sf.hwrotation", "0"));
    }

    /**
     * 获取屏幕宽 X 像素
     *
     * @param context Activity
     * @return 屏幕宽度（单位：像素）
     */
    public int getScreenWidth(@NonNull Activity context) {
        Point size = new Point();
        context.getWindowManager().getDefaultDisplay().getRealSize(size);
        return size.x;
    }

    /**
     * 获取屏幕高 Y 像素
     *
     * @param context Activity
     * @return 屏幕高度（单位：像素）
     */
    public int getScreenHeight(@NonNull Activity context) {
        Point size = new Point();
        context.getWindowManager().getDefaultDisplay().getRealSize(size);
        return size.y;
    }

    /**
     * 设置状态栏显示或隐藏
     *
     * @param show true：显示，false:隐藏
     */
    public void setStatusBar(boolean show) {
        Intent intent = new Intent();
        if (show) {
            intent.setAction("android.intent.action.SYSTEM_STATUS_BAR_SHOW");
        } else {
            intent.setAction("android.intent.action.SYSTEM_STATUS_BAR_HIDE");
        }
        mContext.sendBroadcast(intent);
    }

    /**
     * 判断状态栏是显示或隐藏状态
     *
     * @return true：显示，false:隐藏
     */
    public boolean isStatusBarShow() {
        return TextUtils.equals(SystemUtils.getProperty(
                "persist.sys.hidestatusbar", "0"), "0");
    }

    /**
     * 设置导航栏显示或隐藏
     *
     * @param show true：显示，false:隐藏
     */
    public void setNavBar(boolean show) {
        Intent intent = new Intent();
        if (show) {
            intent.setAction("android.intent.action.SYSTEM_NAVIGATION_BAR_SHOW");
        } else {
            intent.setAction("android.intent.action.SYSTEM_NAVIGATION_BAR_HIDE");
        }
        mContext.sendBroadcast(intent);
    }

    /**
     * 判断导航栏是显示或隐藏状态
     *
     * @return true：显示，false:隐藏
     */
    public boolean isNavBarShow() {
        return TextUtils.equals(SystemUtils.getProperty(
                "persist.sys.hidenavbar", "0"), "0");
    }

    /**
     * 屏幕背光开关使能，当关闭背光时，不进入休眠，软件继续运行
     *
     * @param enable true：亮屏，false：熄屏
     */
    public void setBackLight(boolean enable) {
        Log.i(TAG, "setBackLight, " + enable);

        String[] nodes = {"/sys/class/backlight/backlight/bl_power",
                "/sys/class/backlight/rk28_bl/bl_power"};
        for (String node : nodes) {
            ShellUtils.CommandResult result = ShellUtils.execCmd(
                    "echo " + (enable ? "0" : "1") + " > " + node,
                    false);
            if (!TextUtils.isEmpty(result.errorMsg)) {
                Log.e(TAG, "setBackLight, " + node + ", error: " + result.errorMsg);
            } else {
                Log.i(TAG, "setBackLight, " + node + ", success");
                return;
            }
        }
    }

    /**
     * 设置背光亮度（如果双屏独立背光，则设置主屏背光亮度）
     *
     * @param brightness 背光亮度（取值：0~255）
     */
    public void setBrightness(int brightness) {
        try {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
        } catch (Exception e) {
            Log.e(TAG, "setBrightness, " + e.getMessage());
        }
    }

    /**
     * 设置副背光亮度（如果双屏独立背光，则设置副屏背光亮度）
     *
     * @param brightness 背光亮度（取值：0~255）
     */
    public void setBrightnessExt(int brightness) {
        try {
            /**
             * Settings.System.SCREEN_BRIGHTNESS_EXT = "screen_brightness_ext"
             */
            Settings.System.putInt(mContext.getContentResolver(),
                    "screen_brightness_ext", brightness);
        } catch (Exception e) {
            Log.e(TAG, "setBrightness, " + e.getMessage());
        }
    }

    /**
     * 唤醒屏幕
     */
    public void screenOn() {
        if (null == mPowerManager) {
            mPowerManager = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE));
        }
        PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE,
                "tbapi:screenOn");

        wakeLock.acquire();
        wakeLock.release();

        sendKey(KeyEvent.KEYCODE_F12);
    }

    /**
     * 关闭屏幕
     */
    public void screenOff() {
        if (null == mDevicePolicyManager) {
            mDevicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        ComponentName adminReceiver = new ComponentName(mContext, AdminReceiver.class);
        boolean admin = mDevicePolicyManager.isAdminActive(adminReceiver);
        if (admin) {
            mDevicePolicyManager.lockNow();
        } else {
            Log.e(TAG, "screenOff, No device admin permissions");
        }
    }

    /**
     * 通知系统检查升级
     */
    public void checkUpdate() {
        if (mRomUpgradeService != null) {
            try {
                mRomUpgradeService.checkUpdate();
            } catch (RemoteException e) {
                Log.e(TAG, "checkUpdate, " + e.getMessage());
            }
        }
    }

    /**
     * 安装系统升级包
     *
     * @param packagePath 系统升级包路径
     * @return true：成功，false：失败
     */
    public boolean installPackage(@NonNull String packagePath) {
        if (null != mRomUpgradeService) {
            try {
                return mRomUpgradeService.installPackage(packagePath);
            } catch (RemoteException e) {
                Log.e(TAG, "installPackage, " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * 验证系统升级包
     *
     * @param packagePath 系统升级包路径
     * @return true：升级包有效，false：升级包无效
     */
    public boolean verifyPackage(@NonNull String packagePath) {
        if (null != mRomUpgradeService) {
            try {
                return mRomUpgradeService.verifyPackage(packagePath);
            } catch (RemoteException e) {
                Log.e(TAG, "verifyPackage, " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * 删除系统升级包
     *
     * @param packagePath 系统升级包路径
     */
    public void deletePackage(@NonNull String packagePath) {
        if (null != mRomUpgradeService) {
            try {
                mRomUpgradeService.deletePackage(packagePath);
            } catch (RemoteException e) {
                Log.e(TAG, "deletePackage, " + e.getMessage());
            }
        }
    }

    /**
     * 获取WiFi MAC地址
     *
     * @return WiFi MAC地址
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public String getWiFiMac() {
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    /**
     * 获取以太网MAC地址
     *
     * @return 以太网MAC地址
     */
    public String getEthMac() {
        try {
            int numRead = 0;
            char[] buf = new char[1024];
            StringBuilder sb = new StringBuilder(1000);
            BufferedReader reader = new BufferedReader(new FileReader(
                    "/sys/class/net/eth0/address"));
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                sb.append(readData);
            }
            reader.close();
            return sb.toString().replaceAll("\r|\n", "");
        } catch (IOException e) {
            Log.e(TAG, "getEthMac, " + e.getMessage());
        }

        return "";
    }

    /**
     * 获取IP地址
     *
     * @param iface 网卡名（eth0/eth1/usb0...）
     * @return IP地址
     */
    public String getIp(@NonNull String iface) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.getIp(iface);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.getIp(iface);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 获取子网掩码
     *
     * @param iface 网卡名（eth0/eth1/usb0...）
     * @return 子网掩码
     */
    public String getNetmask(String iface) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.getNetmask(iface);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.getNetmask(iface);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 获取网关
     *
     * @param iface 网卡名（eth0/eth1/usb0...）
     * @return 网关
     */
    public String getGateway(String iface) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.getGateway(iface);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.getGateway(iface);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 获取DNS1
     *
     * @param iface 网卡名（eth0/eth1/usb0...）
     * @return DNS1
     */
    public String getDns1(String iface) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.getDns1(iface);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.getDns1(iface);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 获取DNS2
     *
     * @param iface 网卡名（eth0/eth1/usb0...）
     * @return DNS2
     */
    public String getDns2(String iface) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.getDns2(iface);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.getDns2(iface);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 设置IP地址
     *
     * @param iface   网卡名（eth0/eth1/usb0...）
     * @param ip      IP地址
     * @param netmask 子网掩码
     * @param gateway 网关
     * @param dns1    DNS1
     * @param dns2    DNS2
     * @param mode    模式
     *                STATIC：静态IP（其它参数不能为空）。
     *                DHCP：动态IP（其它参数可为空）。
     */
    public boolean setIp(String iface,
                         String ip,
                         String netmask,
                         String gateway,
                         String dns1,
                         String dns2,
                         @NonNull String mode) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.setIp(iface, ip, netmask, gateway, dns1, dns2, mode);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.setIp(iface, ip, netmask, gateway, dns1, dns2, mode);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 开关网卡
     *
     * @param iface  网卡名（eth0/eth1/usb0...）
     * @param enable true：打开， false：关闭
     * @return true：成功， false：失败
     */
    public boolean setNetEnabled(String iface, boolean enable) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.setEnabled(iface, enable);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.setEnabled(iface, enable);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 网卡是否打开
     *
     * @param iface 网卡名（eth0/eth1/usb0...）
     * @return true：打开， false：关闭
     */
    public boolean isNetEnabled(String iface) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.isEnabled(iface);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.isEnabled(iface);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 获取IP分配方式
     *
     * @param iface 网卡名（eth0/eth1/usb0...）
     * @return DHCP：动太分配，STATIC：静态IP
     */
    public String getIpAssignment(String iface) {
        if (iface.startsWith(IFACE_PREFIX_ETH)) {
            return mEthernetHelper.getIpAssignment(iface);
        } else if (iface.startsWith(IFACE_PREFIX_USB)) {
            return mUsbnetHelper.getIpAssignment(iface);
        } else {
            throw new InvalidParameterException(iface);
        }
    }

    /**
     * 获取当前连网类型（WiFi、移动网络、有线）
     *
     * @return 0：WiFi，1：移动网络，2：有线
     */
    public int getNetworkType() {
        //TODO
        return 0;
    }

    /**
     * 设置DNS
     *
     * @param dns1 DNS
     * @param dns2 DNS
     * @return true：成功，false：失败
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean setDns(@NonNull String dns1, String dns2) {
        try {
            InetAddress address1 = null;
            InetAddress address2 = null;
            if (!TextUtils.isEmpty(dns1)) {
                address1 = InetAddress.getByName(dns1);
            }
            if (!TextUtils.isEmpty(dns2)) {
                address2 = InetAddress.getByName(dns2);
            }
            return DnsUtils.setDns(mContext, address1, address2);
        } catch (UnknownHostException e) {
            Log.e(TAG, "setDns, " + e.getMessage());
        }

        return false;
    }

    /**
     * 获取外部存储 SD卡路径
     *
     * @return 外部存储SD卡路径
     */
    public String getSdcardPath() {
        //TODO
        return null;
    }

    /**
     * 获取外部存储 U 盘路径
     *
     * @param num U盘ID
     * @return 外部存储U盘路径
     */
    public String getUsbPath(int num) {
        //TODO
        return null;
    }

    /**
     * 卸载外部存储
     *
     * @param path             挂载路径
     * @param force            true：强制卸载
     * @param removeEncryption --
     */
    public void unmountVolume(@NonNull String path, boolean force, boolean removeEncryption) {
        //TODO
    }

    /**
     * 设置GPIO输出电平
     *
     * @param gpio  GPIO序号（取值：0~N）
     * @param value 0：低电平，1：高电平
     * @return true：成功，false：失败
     */
    public boolean setGpio(int gpio, int value) {
        if (mGpioService != null) {
            try {
                int ret = mGpioService.gpioWrite(gpio, value);
                if (ret < 0) {
                    Log.e(TAG, "setGpio, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "setGpio, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 获取GPIO输入电平
     *
     * @param gpio GPIO序号（取值：0~N）
     * @return 0：低电平，1：高电平
     */
    public int getGpio(int gpio) {
        if (mGpioService != null) {
            try {
                return mGpioService.gpioRead(gpio);
            } catch (RemoteException e) {
                Log.e(TAG, "getGpio, " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 设置GPIO方向（输入或输出）
     *
     * @param gpio      GPIO序号（取值：0~N）
     * @param direction 0：输入，1：输出
     * @param value     0：低电平，1：高电平（仅设置输出模式时有效）
     * @return true：成功，false：失败
     */
    public boolean setGpioDirection(int gpio, int direction, int value) {
        if (mGpioService != null) {
            try {
                int ret = mGpioService.gpioDirection(gpio, direction, value);
                if (ret < 0) {
                    Log.e(TAG, "setGpioDirection, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "setGpioDirection, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 将GPIO注册为按键
     *
     * @param gpio GPIO序号（取值：0~N）
     * @return true：成功，false：失败
     */
    public boolean regGpioKeyEvent(int gpio) {
        if (mGpioService != null) {
            try {
                int ret = mGpioService.gpioRegKeyEvent(gpio);
                if (ret < 0) {
                    Log.e(TAG, "regGpioKeyEvent, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "regGpioKeyEvent, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 取消GPIO注册为按键
     *
     * @param gpio GPIO序号（取值：0~N）
     * @return true：成功，false：失败
     */
    public boolean unregGpioKeyEvent(int gpio) {
        if (mGpioService != null) {
            try {
                int ret = mGpioService.gpioUnregKeyEvent(gpio);
                if (ret < 0) {
                    Log.e(TAG, "unregGpioKeyEvent, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "unregGpioKeyEvent, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 获取GPIO数量
     *
     * @return GPIO数量
     */
    public int getGpioNum() {
        if (mGpioService != null) {
            try {
                return mGpioService.gpioGetNumber();
            } catch (RemoteException e) {
                Log.e(TAG, "getGpioNum, " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 获取USB摄像头对应Android Camera的ID
     *
     * @param vid USB摄像头VID（取值：16进制字符串，例如：1d6b）
     * @param pid USB摄像头PID（取值：16进制字符串，例如：0002）
     * @return Android Camera ID(0~N), <0：未找到匹配的摄像头
     */
    public int getUVCCameraIndex(@NonNull String vid, @NonNull String pid) {
        for (int i = 0; i < 10; i++) {
            if (TextUtils.equals(
                    SystemUtils.getProperty("topband.dev.video" + i, ""),
                    vid + ":" + pid)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 打开日志写入文件功能
     */
    public void openLog2file() {
        if (mLog2fileService != null) {
            try {
                mLog2fileService.openLog2file();
            } catch (RemoteException e) {
                Log.e(TAG, "openLog2file, " + e.getMessage());
            }
        }
    }

    /**
     * 关闭日志写入文件功能
     */
    public void closeLog2file() {
        if (mLog2fileService != null) {
            try {
                mLog2fileService.closeLog2file();
            } catch (RemoteException e) {
                Log.e(TAG, "closeLog2file, " + e.getMessage());
            }
        }
    }

    /**
     * 日志写入文件是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean isLog2fileOpen() {
        if (mLog2fileService != null) {
            try {
                return mLog2fileService.isOpen();
            } catch (RemoteException e) {
                Log.e(TAG, "isLog2fileOpen, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 获取最大日志文件数量
     *
     * @return 最大日志文件数量
     */
    public int getLogFileNum() {
        if (mLog2fileService != null) {
            try {
                return mLog2fileService.getLogFileNum();
            } catch (RemoteException e) {
                Log.e(TAG, "isLog2fileOpen, " + e.getMessage());
            }
        }
        return 0;
    }

    /**
     * 设置最大日志文件数量
     *
     * @param num 最大日志文件数量
     */
    public void setLogFileNum(int num) {
        if (mLog2fileService != null) {
            try {
                mLog2fileService.setLogFileNum(num);
            } catch (RemoteException e) {
                Log.e(TAG, "setLogFileNum, " + e.getMessage());
            }
        }
    }

    /**
     * 获取日志文件存储路径
     *
     * @return 日志文件存储路径
     */
    public String getLogFilePath() {
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + "lastlog";
    }

    /**
     * 执行shell命令
     *
     * @param cmd  shell命令字符串
     * @param root true：以root用户运行，false：以非root用户运行
     * @return ShellUtils.CommandResult
     */
    public ShellUtils.CommandResult execCmd(@NonNull String cmd, boolean root) {
        return ShellUtils.execCmd(cmd, root);
    }

    /**
     * 打开4G模块保活
     */
    public void open4gKeepLive() {
        if (mModemService != null) {
            try {
                mModemService.open4gKeepLive();
            } catch (RemoteException e) {
                Log.e(TAG, "open4GKeeplive, " + e.getMessage());
            }
        }
    }

    /**
     * 关闭4G模块保活
     */
    public void close4gKeepLive() {
        if (mModemService != null) {
            try {
                mModemService.close4gKeepLive();
            } catch (RemoteException e) {
                Log.e(TAG, "close4GKeeplive, " + e.getMessage());
            }
        }
    }

    /**
     * 4G保活是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean keepLiveIsOpen() {
        if (mModemService != null) {
            try {
                return mModemService.keepLiveIsOpen();
            } catch (RemoteException e) {
                Log.e(TAG, "keepLiveIsOpen, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 打开按键事件拦截
     */
    public void openKeyIntercept() {
        if (mKeyInterceptService != null) {
            try {
                mKeyInterceptService.openKeyIntercept();
            } catch (RemoteException e) {
                Log.e(TAG, "openKeyIntercept, " + e.getMessage());
            }
        }
    }

    /**
     * 关闭按键事件拦截
     */
    public void closeKeyIntercept() {
        if (mKeyInterceptService != null) {
            try {
                mKeyInterceptService.closeKeyIntercept();
            } catch (RemoteException e) {
                Log.e(TAG, "closeKeyIntercept, " + e.getMessage());
            }
        }
    }

    /**
     * 按键事件拦截是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean keyInterceptIsOpen() {
        if (mKeyInterceptService != null) {
            try {
                return mKeyInterceptService.isOpen();
            } catch (RemoteException e) {
                Log.e(TAG, "keyInterceptIsOpen, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 设置OTG口模式（默认保存）
     *
     * @param mode 0: auto, 1: host, 2: device
     * @return true：成功，false：失败
     */
    public boolean setOtgMode(String mode) {
        if (mOtgService != null) {
            try {
                return mOtgService.setOtgMode(mode);
            } catch (RemoteException e) {
                Log.e(TAG, "setOtgMode, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 设置OTG口模式
     *
     * @param mode 0: auto, 1: host, 2: device
     * @param save true：保存，false：不保存，单次有效
     * @return true：成功，false：失败
     */
    public boolean setOtgModeExt(String mode, boolean save) {
        if (mOtgService != null) {
            try {
                return mOtgService.setOtgModeExt(mode, save);
            } catch (RemoteException e) {
                Log.e(TAG, "setOtgModeExt, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 获取OTG口模式
     *
     * @return 0: auto, 1: host, 2: device
     */
    public String getOtgMode() {
        if (mOtgService != null) {
            try {
                return mOtgService.getOtgMode();
            } catch (RemoteException e) {
                Log.e(TAG, "getOtgMode, " + e.getMessage());
            }
        }
        return "0";
    }

    /**
     * 其它扩展设备控制
     *
     * @param type  扩展功能类型
     * @param value 值
     * @return true：成功，false：失败
     */
    public boolean setControl(int type, int value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * 静默安装APK应用
     *
     * @param path APK文件路径
     */
    public void silentInstall(@NonNull String path) {
        if (!InstallUtils.installSilent(path)) {
            InstallUtils.install(mContext, path);
        }
    }

    /**
     * 设置韦根读格式
     *
     * @param format 韦根格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_26：韦根26格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_24：韦根34格式
     * @return true：成功，false：失败
     */
    public boolean setWiegandReadFormat(WiegandFormat format) {
        if (mWiegandService != null) {
            try {
                int ret = -1;
                if (format == WiegandFormat.WIEGAND_FORMAT_26) {
                    ret = mWiegandService.setReadFormat(26);
                } else if (format == WiegandFormat.WIEGAND_FORMAT_34) {
                    ret = mWiegandService.setReadFormat(34);
                }
                if (ret < 0) {
                    Log.e(TAG, "setWiegandReadFormat, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "setWiegandReadFormat, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 设置韦根写格式
     *
     * @param format 韦根格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_26：韦根26格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_24：韦根34格式
     * @return true：成功，false：失败
     */
    public boolean setWiegandWriteFormat(WiegandFormat format) {
        if (mWiegandService != null) {
            try {
                int ret = -1;
                if (format == WiegandFormat.WIEGAND_FORMAT_26) {
                    ret = mWiegandService.setWriteFormat(26);
                } else if (format == WiegandFormat.WIEGAND_FORMAT_34) {
                    ret = mWiegandService.setWriteFormat(34);
                }
                if (ret < 0) {
                    Log.e(TAG, "setWiegandWriteFormat, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "setWiegandWriteFormat, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 读韦根数据
     *
     * @return 韦根数据（<0：失败）
     */
    public int wiegandRead() {
        if (mWiegandService != null) {
            try {
                return mWiegandService.read();
            } catch (RemoteException e) {
                Log.e(TAG, "wiegandRead, " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 写韦根数据
     *
     * @param data 韦根数据
     * @return true：成功，false：失败
     */
    public boolean wiegandWrite(int data) {
        if (mWiegandService != null) {
            try {
                int ret = mWiegandService.write(data);
                if (ret < 0) {
                    Log.e(TAG, "wiegandWrite, error: " + ret);
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "wiegandWrite, " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 静音
     */
    public void mute() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(
                    Context.AUDIO_SERVICE);
        }
        mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    /**
     * 取消静音
     */
    public void unmute() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(
                    Context.AUDIO_SERVICE);
        }
        mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    /**
     * 打开ADB
     */
    public void openAdb() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.ADB_ENABLED, 1);
    }

    /**
     * 关闭ADB
     */
    public void closeAdb() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0);
    }

    /**
     * ADB是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean isAdbOpen() {
        try {
            return Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.ADB_ENABLED) > 0;
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "isAdbEnabled, " + e.getMessage());
        }
        return false;
    }

    /**
     * 模拟发送键值
     *
     * @param keycode 键值
     */
    public void sendKey(final int keycode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keycode);
                } catch (Exception e) {
                    Log.e(TAG, "sendKey, " + e.getMessage());
                }
            }
        }).start();
    }

    public enum WiegandFormat {
        WIEGAND_FORMAT_26,
        WIEGAND_FORMAT_34
    }
}
