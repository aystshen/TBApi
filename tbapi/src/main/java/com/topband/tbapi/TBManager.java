package com.topband.tbapi;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ayst.androidx.IKeyInterceptService;
import com.ayst.androidx.ILog2fileService;
import com.ayst.androidx.IModemService;
import com.ayst.androidx.ITimeRTCService;
import com.ayst.romupgrade.IRomUpgradeService;
import com.topband.tbapi.utils.DeviceInfoUtils;
import com.topband.tbapi.utils.EthernetHelper;
import com.topband.tbapi.utils.EthernetHelperR;
import com.topband.tbapi.utils.IEthernetHelper;
import com.topband.tbapi.utils.InstallUtils;
import com.topband.tbapi.utils.ShellUtils;
import com.topband.tbapi.utils.SystemUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

public class TBManager implements ITBManager {
    private static final String TAG = "TBManager";

    // API版本
    private static final String VERSION = "1.0.2";

    // 屏幕旋转角度
    public static final int SCREEN_ANGLE_0 = 0;
    public static final int SCREEN_ANGLE_90 = 90;
    public static final int SCREEN_ANGLE_180 = 180;
    public static final int SCREEN_ANGLE_270 = 270;

    public enum WiegandFormat {
        WIEGAND_FORMAT_26,
        WIEGAND_FORMAT_34
    };

    private Context mContext;
    private IMcuService mMcuService;
    private IGpioService mGpioService;
    private IWiegandService mWiegandService;
    private IRomUpgradeService mRomUpgradeService;
    private ILog2fileService mLog2fileService;
    private IModemService mModemService;
    private IKeyInterceptService mKeyInterceptService;
    private ITimeRTCService mTimingSwitchService;
    private IEthernetHelper mEthernetHelper;

    private static AudioManager sAudioManager;

    public TBManager(Context context) {
        mContext = context;
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

        // 获取MCU Service
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"mcu"});
            mMcuService = IMcuService.Stub.asInterface(binder);
        } catch (Exception e) {
            Log.e(TAG, "TBManager, get mcu service fail");
        }

        // 获取GPIO Service
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"gpio"});
            mGpioService = IGpioService.Stub.asInterface(binder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取Wiegand Service
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"wiegand"});
            mWiegandService = IWiegandService.Stub.asInterface(binder);
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

        intent = new Intent();
        intent.setPackage("com.ayst.androidx");
        intent.setAction("com.ayst.androidx.TIMERTC_SREVICE");
        mContext.bindService(intent, mTimingSwitchServiceConnection, Context.BIND_AUTO_CREATE);
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
    }

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

    @Override
    public String getAPIVersion() {
        return VERSION;
    }

    @Override
    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    @Override
    public String getFirmwareVersion() {
        return SystemUtils.getProperty("ro.topband.sw.version", "");
    }

    @Override
    public String getAndroidDisplay() {
        return Build.DISPLAY;
    }

    @Override
    public String getFormattedKernelVersion() {
        return DeviceInfoUtils.getFormattedKernelVersion();
    }

    @Override
    public String getModel() {
        return Build.MODEL;
    }

    @Override
    public long getMemorySize() {
        return SystemUtils.getMemSize(mContext, "MemTotal");
    }

    @Override
    public long getInternalStorageSize() {
        return SystemUtils.getStorageSize();
    }

    @Override
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

    @Override
    public void reboot() {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        mContext.sendBroadcast(intent);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void screenshot(@NonNull String path) {
        ShellUtils.execCmd("screencap -p " + path, false);
    }

    @Override
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

    @Override
    public int getRotation() {
        return Integer.parseInt(SystemUtils.getProperty(
                "persist.sys.sf.hwrotation", "0"));
    }

    @Override
    public int getScreenWidth(@NonNull Activity context) {
        Point size = new Point();
        context.getWindowManager().getDefaultDisplay().getRealSize(size);
        return size.x;
    }

    @Override
    public int getScreenHeight(@NonNull Activity context) {
        Point size = new Point();
        context.getWindowManager().getDefaultDisplay().getRealSize(size);
        return size.y;
    }

    @Override
    public void setStatusBar(boolean show) {
        Intent intent = new Intent();
        if (show) {
            intent.setAction("android.intent.action.SYSTEM_STATUS_BAR_SHOW");
        } else {
            intent.setAction("android.intent.action.SYSTEM_STATUS_BAR_HIDE");
        }
        mContext.sendBroadcast(intent);
    }

    @Override
    public boolean isStatusBarShow() {
        return TextUtils.equals(SystemUtils.getProperty(
                "persist.sys.hidestatusbar", "0"), "0");
    }

    @Override
    public void setNavBar(boolean show) {
        Intent intent = new Intent();
        if (show) {
            intent.setAction("android.intent.action.SYSTEM_NAVIGATION_BAR_SHOW");
        } else {
            intent.setAction("android.intent.action.SYSTEM_NAVIGATION_BAR_HIDE");
        }
        mContext.sendBroadcast(intent);
    }

    @Override
    public boolean isNavBarShow() {
        return TextUtils.equals(SystemUtils.getProperty(
                "persist.sys.hidenavbar", "0"), "0");
    }

    @Override
    public void setBackLight(boolean enable) {
        Log.i(TAG, "setBackLight, " + enable);

        String[] nodes = {"/sys/class/backlight/backlight/bl_power",
                        "/sys/class/backlight/rk28_bl/bl_power"};
        for (String node : nodes) {
            ShellUtils.CommandResult result = ShellUtils.execCmd(
                    "echo " + (enable ? "0" : "1") + " > " + node,
                    true);
            if (!TextUtils.isEmpty(result.errorMsg)) {
                Log.e(TAG, "setBackLight, " + node + ", error: " + result.errorMsg);
            } else {
                Log.i(TAG, "setBackLight, " + node + ", success");
                return;
            }
        }
    }

    @Override
    public void setBrightness(int brightness) {
        try {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
        } catch (Exception e) {
            Log.e(TAG, "setBrightness, " + e.getMessage());
        }
    }

    @Override
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

    @Override
    public void checkUpdate() {
        if (mRomUpgradeService != null) {
            try {
                mRomUpgradeService.checkUpdate();
            } catch (RemoteException e) {
                Log.e(TAG, "checkUpdate, " + e.getMessage());
            }
        }
    }

    @Override
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

    @Override
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

    @Override
    public void deletePackage(@NonNull String packagePath) {
        if (null != mRomUpgradeService) {
            try {
                mRomUpgradeService.deletePackage(packagePath);
            } catch (RemoteException e) {
                Log.e(TAG, "deletePackage, " + e.getMessage());
            }
        }
    }

    @SuppressLint("HardwareIds")
    @Override
    public String getWiFiMac() {
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    @Override
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

    @Override
    public String getEthIp() {
        return mEthernetHelper.getIp();
    }

    @Override
    public String getEthNetmask() {
        return mEthernetHelper.getNetmask();
    }

    @Override
    public String getEthGateway() {
        return mEthernetHelper.getGateway();
    }

    @Override
    public String getEthDns1() {
        return mEthernetHelper.getDns1();
    }

    @Override
    public String getEthDns2() {
        return mEthernetHelper.getDns2();
    }

    @Override
    public boolean setEthIp(String ip,
                            String netmask,
                            String gateway,
                            String dns1,
                            String dns2,
                            @NonNull String mode) {
        return mEthernetHelper.setIp(ip, netmask, gateway, dns1, dns2, mode);
    }

    @Override
    public boolean setEthEnabled(boolean enable) {
        return mEthernetHelper.setEthEnabled(enable);
    }

    @Override
    public boolean isEthEnabled() {
        return mEthernetHelper.isEthEnabled();
    }

    @Override
    public boolean isDhcp() {
        return TextUtils.equals("DHCP", mEthernetHelper.getIpAssignment());
    }

    @Override
    public int getNetworkType() {
        //TODO
        return 0;
    }

    @Override
    public String getSdcardPath() {
        //TODO
        return null;
    }

    @Override
    public String getUsbPath(int num) {
        //TODO
        return null;
    }

    @Override
    public void unmountVolume(@NonNull String path, boolean force, boolean removeEncryption) {
        //TODO
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void openLog2file() {
        if (mLog2fileService != null) {
            try {
                mLog2fileService.openLog2file();
            } catch (RemoteException e) {
                Log.e(TAG, "openLog2file, " + e.getMessage());
            }
        }
    }

    @Override
    public void closeLog2file() {
        if (mLog2fileService != null) {
            try {
                mLog2fileService.closeLog2file();
            } catch (RemoteException e) {
                Log.e(TAG, "closeLog2file, " + e.getMessage());
            }
        }
    }

    @Override
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

    @Override
    public void setLogFileNum(int num) {
        if (mLog2fileService != null) {
            try {
                mLog2fileService.setLogFileNum(num);
            } catch (RemoteException e) {
                Log.e(TAG, "setLogFileNum, " + e.getMessage());
            }
        }
    }

    @Override
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

    @Override
    public String getLogFilePath() {
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + "lastlog";
    }

    @Override
    public ShellUtils.CommandResult execCmd(@NonNull String cmd, boolean root) {
        return ShellUtils.execCmd(cmd, root);
    }

    @Override
    public void open4gKeepLive() {
        if (mModemService != null) {
            try {
                mModemService.open4gKeepLive();
            } catch (RemoteException e) {
                Log.e(TAG, "open4GKeeplive, " + e.getMessage());
            }
        }
    }

    @Override
    public void close4gKeepLive() {
        if (mModemService != null) {
            try {
                mModemService.close4gKeepLive();
            } catch (RemoteException e) {
                Log.e(TAG, "close4GKeeplive, " + e.getMessage());
            }
        }
    }

    @Override
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

    @Override
    public void openKeyIntercept() {
        if (mKeyInterceptService != null) {
            try {
                mKeyInterceptService.openKeyIntercept();
            } catch (RemoteException e) {
                Log.e(TAG, "openKeyIntercept, " + e.getMessage());
            }
        }
    }

    @Override
    public void closeKeyIntercept() {
        if (mKeyInterceptService != null) {
            try {
                mKeyInterceptService.closeKeyIntercept();
            } catch (RemoteException e) {
                Log.e(TAG, "closeKeyIntercept, " + e.getMessage());
            }
        }
    }

    @Override
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

    @Override
    public boolean setControl(int type, int value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void silentInstall(@NonNull String path) {
        if (!InstallUtils.installSilent(path)) {
            InstallUtils.install(mContext, path);
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void mute() {
        if (sAudioManager == null) {
            sAudioManager = (AudioManager) mContext.getSystemService(
                    Context.AUDIO_SERVICE);
        }
        sAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        sAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
        sAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        sAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
        sAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    @Override
    public void unmute() {
        if (sAudioManager == null) {
            sAudioManager = (AudioManager) mContext.getSystemService(
                    Context.AUDIO_SERVICE);
        }
        sAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        sAudioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        sAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        sAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
        sAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }
}
