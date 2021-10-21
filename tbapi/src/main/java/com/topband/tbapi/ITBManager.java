package com.topband.tbapi;

import android.app.Activity;

import com.topband.tbapi.utils.ShellUtils;

interface ITBManager {

    /**
     * 获取API版本号
     *
     * @return 当前API版本号
     * 例如：1.0.1
     */
    public String getAPIVersion();

    /**
     * 获取android系统版本号
     *
     * @return android版本号
     * 例如：8.1.0
     */
    public String getAndroidVersion();

    /**
     * 获取固件版本号
     *
     * @return 固件版本号
     * 例如：1.0.1
     */
    public String getFirmwareVersion();

    /**
     * 获取固件版本和编译日期
     *
     * @return 格式化固件版本号与编译时间
     * 例如：ls328-default-1.0.1-1-20200101.032407
     */
    public String getAndroidDisplay();

    /**
     * 获取内核版本号
     *
     * @return 格式化内核版本号
     * 例如：Linux version 4.4.132 (tomcat@dqrd03) (gcc version 6.3.1 20170404
     * (Linaro GCC 6.3-2017.05) ) #285 SMP PREEMPT Wed Dec 30 02:23:56 CST 2020
     */
    public String getFormattedKernelVersion();

    /**
     * 获取设备型号
     *
     * @return 设备型号
     * 例如：topband
     */
    public String getModel();

    /**
     * 获取内存大小
     *
     * @return 内存大小（单位：byte）
     */
    public long getMemorySize();

    /**
     * 获取内部存储容量
     *
     * @return 内部存储空间大小（单位：byte）
     */
    public long getInternalStorageSize();

    /**
     * 关机
     */
    public void shutdown();

    /**
     * 重启
     */
    public void reboot();

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
    public boolean setTimingSwitch(String offDate, String offTime,
                                   String onDate, String onTime,
                                   boolean enable);

    /**
     * 打开看门狗
     *
     * @return true：成功，false：失败
     */
    public boolean openWatchdog();

    /**
     * 关闭看门狗
     *
     * @return true：成功，false：失败
     */
    public boolean closeWatchdog();

    /**
     * 喂狗一次，对看门狗计数进行复位操作
     *
     * @return true：成功，false：失败
     */
    public boolean watchdogFeed();

    /**
     * 设置看门狗超时时长
     *
     * @param timeout 看门狗超时时长（单位：秒）
     * @return true：成功，false：失败
     */
    public boolean setWatchdogTimeout(int timeout);

    /**
     * 获取看门狗超时时长
     *
     * @return 看门狗超时时长（单位：秒）
     */
    public int getWatchdogTimeout();

    /**
     * 判断看门狗是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean watchdogIsOpen();

    /**
     * 截屏并保存为png图片格式到指定位置
     *
     * @param path 截屏保存文件路径（例如：/sdcard/screenshot.png）
     */
    public void screenshot(String path);

    /**
     * 设置屏幕逆时针旋转角度
     *
     * @param angle  屏幕旋转角度（取值：0、90、180、270）
     * @param reboot true：重启，false：不重启
     */
    public void setRotation(int angle, boolean reboot);

    /**
     * 获取屏幕当前角度
     *
     * @return 屏幕旋转角度（取值：0、90、180、270）
     */
    public int getRotation();

    /**
     * 获取屏幕宽 X 像素
     *
     * @param context Activity
     * @return 屏幕宽度（单位：像素）
     */
    public int getScreenWidth(Activity context);

    /**
     * 获取屏幕高 Y 像素
     *
     * @param context Activity
     * @return 屏幕高度（单位：像素）
     */
    public int getScreenHeight(Activity context);

    /**
     * 设置状态栏显示或隐藏
     *
     * @param show true：显示，false:隐藏
     */
    public void setStatusBar(boolean show);

    /**
     * 判断状态栏是显示或隐藏状态
     *
     * @return true：显示，false:隐藏
     */
    public boolean isStatusBarShow();

    /**
     * 设置导航栏显示或隐藏
     *
     * @param show true：显示，false:隐藏
     */
    public void setNavBar(boolean show);

    /**
     * 判断导航栏是显示或隐藏状态
     *
     * @return true：显示，false:隐藏
     */
    public boolean isNavBarShow();

    /**
     * 屏幕背光开关使能，当关闭背光时，不进入休眠，软件继续运行
     *
     * @param enable true：亮屏，false：熄屏
     */
    public void setBackLight(boolean enable);

    /**
     * 设置背光亮度（如果双屏独立背光，则设置主屏背光亮度）
     *
     * @param brightness 背光亮度（取值：0~255）
     */
    public void setBrightness(int brightness);

    /**
     * 设置副背光亮度（如果双屏独立背光，则设置副屏背光亮度）
     *
     * @param brightness 背光亮度（取值：0~255）
     */
    public void setBrightnessExt(int brightness);

    /**
     * 通知系统检查升级
     */
    public void checkUpdate();

    /**
     * 安装系统升级包
     *
     * @param packagePath 系统升级包路径
     * @return true：成功，false：失败
     */
    public boolean installPackage(String packagePath);

    /**
     * 验证系统升级包
     *
     * @param packagePath 系统升级包路径
     * @return true：升级包有效，false：升级包无效
     */
    public boolean verifyPackage(String packagePath);

    /**
     * 删除系统升级包
     *
     * @param packagePath 系统升级包路径
     */
    public void deletePackage(String packagePath);

    /**
     * 获取WiFi MAC地址
     *
     * @return WiFi MAC地址
     */
    public String getWiFiMac();

    /**
     * 获取以太网MAC地址
     *
     * @return 以太网MAC地址
     */
    public String getEthMac();

    /**
     * 获取以太网IP地址
     *
     * @return 以太网IP地址
     */
    public String getEthIp();

    /**
     * 获取以太网子网掩码
     *
     * @return 以太网子网掩码
     */
    public String getEthNetmask();

    /**
     * 获取以太网网关
     *
     * @return 以太网网关
     */
    public String getEthGateway();

    /**
     * 获取以太网DNS1
     *
     * @return 以太网DNS1
     */
    public String getEthDns1();

    /**
     * 获取以太网DNS2
     *
     * @return 以太网DNS2
     */
    public String getEthDns2();

    /**
     * 设置以太网IP地址
     *
     * @param ip      IP地址
     * @param netmask 子网掩码
     * @param gateway 网关
     * @param dns1    DNS1
     * @param dns2    DNS2
     * @param mode    模式
     *                STATIC：静态IP（其它参数不能为空）。
     *                DHCP：动态IP（其它参数可为空）。
     */
    public boolean setEthIp(String ip, String netmask, String gateway, String dns1, String dns2, String mode);

    /**
     * 开关以太网
     *
     * @param enable true：打开， false：关闭
     * @return true：成功， false：失败
     */
    public boolean setEthEnabled(boolean enable);

    /**
     * 以太网是否打开
     *
     * @return true：打开， false：关闭
     */
    public boolean isEthEnabled();

    /**
     * 判断当前网络类型是DHCP或静态IP
     *
     * @return true：DHCP，false：静态IP
     */
    public boolean isDhcp();

    /**
     * 获取当前连网类型（WiFi、移动网络、有线）
     *
     * @return 0：WiFi，1：移动网络，2：有线
     */
    public int getNetworkType();

    /**
     * 获取外部存储 SD卡路径
     *
     * @return 外部存储SD卡路径
     */
    public String getSdcardPath();

    /**
     * 获取外部存储 U 盘路径
     *
     * @param num U盘ID
     * @return 外部存储U盘路径
     */
    public String getUsbPath(int num);

    /**
     * 卸载外部存储
     *
     * @param path             挂载路径
     * @param force            true：强制卸载
     * @param removeEncryption --
     */
    public void unmountVolume(String path, boolean force, boolean removeEncryption);

    /**
     * 设置GPIO输出电平
     *
     * @param gpio  GPIO序号（取值：0~N）
     * @param value 0：低电平，1：高电平
     * @return true：成功，false：失败
     */
    public boolean setGpio(int gpio, int value);

    /**
     * 获取GPIO输入电平
     *
     * @param gpio GPIO序号（取值：0~N）
     * @return 0：低电平，1：高电平
     */
    public int getGpio(int gpio);

    /**
     * 设置GPIO方向（输入或输出）
     *
     * @param gpio      GPIO序号（取值：0~N）
     * @param direction 0：输入，1：输出
     * @param value     0：低电平，1：高电平（仅设置输出模式时有效）
     * @return true：成功，false：失败
     */
    public boolean setGpioDirection(int gpio, int direction, int value);

    /**
     * 将GPIO注册为按键
     *
     * @param gpio GPIO序号（取值：0~N）
     * @return true：成功，false：失败
     */
    public boolean regGpioKeyEvent(int gpio);

    /**
     * 取消GPIO注册为按键
     *
     * @param gpio GPIO序号（取值：0~N）
     * @return true：成功，false：失败
     */
    public boolean unregGpioKeyEvent(int gpio);

    /**
     * 获取GPIO数量
     *
     * @return GPIO数量
     */
    public int getGpioNum();

    /**
     * 获取USB摄像头对应Android Camera的ID
     *
     * @param vid USB摄像头VID（取值：16进制字符串，例如：1d6b）
     * @param pid USB摄像头PID（取值：16进制字符串，例如：0002）
     * @return Android Camera ID(0~N), <0：未找到匹配的摄像头
     */
    public int getUVCCameraIndex(String vid, String pid);

    /**
     * 打开日志写入文件功能
     */
    public void openLog2file();

    /**
     * 关闭日志写入文件功能
     */
    public void closeLog2file();

    /**
     * 日志写入文件是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean isLog2fileOpen();

    /**
     * 获取最大日志文件数量
     *
     * @return 最大日志文件数量
     */
    public int getLogFileNum();

    /**
     * 设置最大日志文件数量
     *
     * @param num 最大日志文件数量
     */
    public void setLogFileNum(int num);

    /**
     * 获取日志文件存储路径
     *
     * @return 日志文件存储路径
     */
    public String getLogFilePath();

    /**
     * 执行shell命令
     *
     * @param cmd  shell命令字符串
     * @param root true：以root用户运行，false：以非root用户运行
     * @return ShellUtils.CommandResult
     */
    public ShellUtils.CommandResult execCmd(String cmd, boolean root);

    /**
     * 打开4G模块保活
     */
    public void open4gKeepLive();

    /**
     * 关闭4G模块保活
     */
    public void close4gKeepLive();

    /**
     * 4G保活是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean keepLiveIsOpen();

    /**
     * 打开按键事件拦截
     */
    public void openKeyIntercept();

    /**
     * 关闭按键事件拦截
     */
    public void closeKeyIntercept();

    /**
     * 按键事件拦截是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean keyInterceptIsOpen();

    /**
     * 设置OTG口模式（默认保存）
     *
     * @param mode 0: auto, 1: host, 2: device
     * @return true：成功，false：失败
     */
    public boolean setOtgMode(String mode);

    /**
     * 设置OTG口模式
     *
     * @param mode 0: auto, 1: host, 2: device
     * @param save true：保存，false：不保存，单次有效
     * @return true：成功，false：失败
     */
    public boolean setOtgModeExt(String mode, boolean save);

    /**
     * 获取OTG口模式
     *
     * @return 0: auto, 1: host, 2: device
     */
    public String getOtgMode();

    /**
     * 其它扩展设备控制
     *
     * @param type  扩展功能类型
     * @param value 值
     * @return true：成功，false：失败
     */
    public boolean setControl(int type, int value);

    /**
     * 静默安装APK应用
     *
     * @param path APK文件路径
     */
    public void silentInstall(String path);

    /**
     * 设置韦根读格式
     *
     * @param format 韦根格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_26：韦根26格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_24：韦根34格式
     * @return true：成功，false：失败
     */
    public boolean setWiegandReadFormat(TBManager.WiegandFormat format);

    /**
     * 设置韦根写格式
     *
     * @param format 韦根格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_26：韦根26格式
     *               TBManager.WiegandFormat.WIEGAND_FORMAT_24：韦根34格式
     * @return true：成功，false：失败
     */
    public boolean setWiegandWriteFormat(TBManager.WiegandFormat format);

    /**
     * 读韦根数据
     *
     * @return 韦根数据（<0：失败）
     */
    public int wiegandRead();

    /**
     * 写韦根数据
     *
     * @param data 韦根数据
     * @return true：成功，false：失败
     */
    public boolean wiegandWrite(int data);

    /**
     * 静音
     */
    public void mute();

    /**
     * 取消静音
     */
    public void unmute();

    /**
     * 打开ADB
     */
    public void openAdb();

    /**
     * 关闭ADB
     */
    public void closeAdb();

    /**
     * ADB是否打开
     *
     * @return true：打开，false：关闭
     */
    public boolean isAdbOpen();
}