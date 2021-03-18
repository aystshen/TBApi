package com.topband.tbapidemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.topband.tbapi.TBManager;
import com.topband.tbapi.utils.ShellUtils;
import com.topband.tbapidemo.utils.AppUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "MainActivity";

    @BindView(R.id.tv_api_version)
    TextView mApiVersionTv;
    @BindView(R.id.tv_android_version)
    TextView mAndroidVersionTv;
    @BindView(R.id.tv_fw_version)
    TextView mFwVersionTv;
    @BindView(R.id.tv_display_version)
    TextView mDisplayTv;
    @BindView(R.id.tv_kernel_version)
    TextView mKernelVersionTv;
    @BindView(R.id.tv_model)
    TextView mModelTv;
    @BindView(R.id.tv_memory_size)
    TextView mMemorySizeTv;
    @BindView(R.id.tv_storage_size)
    TextView mStorageSizeTv;
    @BindView(R.id.btn_shutdown)
    Button mShutdownBtn;
    @BindView(R.id.btn_reboot)
    Button mRebootBtn;
    @BindView(R.id.btn_timing_switch)
    Button mTimingSwitchBtn;
    @BindView(R.id.btn_watchdog)
    ToggleButton mWatchdogBtn;
    @BindView(R.id.btn_watchdog_feed)
    Button mWatchdogFeedBtn;
    @BindView(R.id.btn_set_watchdog_timeout)
    Button mSetWatchdogTimeoutBtn;
    @BindView(R.id.btn_get_watchdog_timeout)
    Button mGetWatchdogTimeoutBtn;
    @BindView(R.id.btn_screenshot)
    Button mScreenshotBtn;
    @BindView(R.id.btn_rotation)
    Button mRotationBtn;
    @BindView(R.id.btn_statusbar)
    ToggleButton mStatusbarBtn;
    @BindView(R.id.btn_navbar)
    ToggleButton mNavbarBtn;
    @BindView(R.id.btn_backlight)
    ToggleButton mBacklightBtn;
    @BindView(R.id.tv_screen_width)
    TextView mScreenWidthTv;
    @BindView(R.id.tv_screen_height)
    TextView mScreenHeightTv;
    @BindView(R.id.seekbar_main_backlight)
    SeekBar mMainBacklightSeekbar;
    @BindView(R.id.seekbar_sub_backlight)
    SeekBar mSubBacklightSeekbar;
    @BindView(R.id.btn_check_update)
    Button mCheckUpdateBtn;
    @BindView(R.id.btn_install_update)
    Button mInstallUpdateBtn;
    @BindView(R.id.btn_verity_update)
    Button mVerityUpdateBtn;
    @BindView(R.id.btn_delete_update)
    Button mDeleteUpdateBtn;
    @BindView(R.id.rdo_wifi)
    RadioButton mWifiRdo;
    @BindView(R.id.rdo_4g)
    RadioButton m4GRdo;
    @BindView(R.id.rdo_eth)
    RadioButton mEthRdo;
    @BindView(R.id.tv_wifi_mac)
    TextView mWifiMacTv;
    @BindView(R.id.tv_eth_mac)
    TextView mEthMacTv;
    @BindView(R.id.edt_eth_ip)
    EditText mEthIpEdt;
    @BindView(R.id.edt_eth_mask)
    EditText mEthMaskEdt;
    @BindView(R.id.edt_eth_gateway)
    EditText mEthGatewayEdt;
    @BindView(R.id.edt_eth_dns1)
    EditText mEthDnsEdt1;
    @BindView(R.id.edt_eth_dns2)
    EditText mEthDnsEdt2;
    @BindView(R.id.btn_dhcp)
    ToggleButton mDhcpBtn;
    @BindView(R.id.tv_sdcard_path)
    TextView mSdcardPathTv;
    @BindView(R.id.tv_usb_path)
    TextView mUsbPathTv;
    @BindView(R.id.spn_gpio)
    Spinner mGpioSpn;
    @BindView(R.id.btn_gpio_direction)
    ToggleButton mGpioDirectionBtn;
    @BindView(R.id.btn_gpio)
    ToggleButton mGpioBtn;
    @BindView(R.id.btn_get_gpio)
    Button mGetGpioBtn;
    @BindView(R.id.tv_camera)
    TextView mCameraTv;
    @BindView(R.id.btn_refresh_camera)
    Button mRefreshCameraBtn;
    @BindView(R.id.btn_log2file)
    ToggleButton mLog2fileBtn;
    @BindView(R.id.btn_set_log2file_num)
    Button mSetLog2fileNumBtn;
    @BindView(R.id.btn_get_log2file_num)
    Button mGetLog2fileNumBtn;
    @BindView(R.id.tv_log_path)
    TextView mLogPathTv;
    @BindView(R.id.btn_shell_cmd)
    Button mShellCmdBtn;
    @BindView(R.id.tv_shell_cmd)
    TextView mShellCmdTv;
    @BindView(R.id.btn_4g_keeplive)
    ToggleButton m4gKeepliveBtn;
    @BindView(R.id.btn_key_intercept)
    ToggleButton mKeyInterceptBtn;
    @BindView(R.id.btn_silent_install)
    Button mSilentInstallBtn;
    @BindView(R.id.rdo_wiegand_read_26)
    RadioButton mWiegandRead26Rdo;
    @BindView(R.id.rdo_wiegand_read_34)
    RadioButton mWiegandRead34Rdo;
    @BindView(R.id.rdo_wiegand_write_26)
    RadioButton mWiegandWrite26Rdo;
    @BindView(R.id.rdo_wiegand_write_34)
    RadioButton mWiegandWrite34Rdo;
    @BindView(R.id.btn_wiegand_read)
    Button mWiegandReadBtn;
    @BindView(R.id.btn_wiegand_write)
    Button mWiegandWriteBtn;
    @BindView(R.id.btn_eth)
    ToggleButton mEthBtn;

    private TBManager mTBManager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mHandler = new Handler(getMainLooper());

        mTBManager = new TBManager(this);
        mTBManager.init();

        // 延时1秒，等特TBManager初始化完成
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        mTBManager.deinit();
        super.onDestroy();
    }

    @SuppressLint("DefaultLocale")
    private void init() {
        // 系统信息
        mApiVersionTv.setText(mTBManager.getAPIVersion());
        mAndroidVersionTv.setText(mTBManager.getAndroidVersion());
        mFwVersionTv.setText(mTBManager.getFirmwareVersion());
        mDisplayTv.setText(mTBManager.getAndroidDisplay());
        mKernelVersionTv.setText(mTBManager.getFormattedKernelVersion());
        mModelTv.setText(mTBManager.getModel());
        mMemorySizeTv.setText(mTBManager.getMemorySize() / 1024 / 1024 + "MB");
        mStorageSizeTv.setText(mTBManager.getInternalStorageSize() / 1024 / 1024 + "MB");

        // 看门狗
        mWatchdogBtn.setChecked(mTBManager.watchdogIsOpen());
        mWatchdogBtn.setOnCheckedChangeListener(this);
        mGetWatchdogTimeoutBtn.setText(String.format("获取看门狗超时(%d秒)",
                mTBManager.getWatchdogTimeout()));

        // 显示
        mRotationBtn.setText(String.format("旋转屏幕(%d)", mTBManager.getRotation()));
        mStatusbarBtn.setChecked(mTBManager.isStatusBarShow());
        mStatusbarBtn.setOnCheckedChangeListener(this);
        mNavbarBtn.setChecked(mTBManager.isNavBarShow());
        mNavbarBtn.setOnCheckedChangeListener(this);
        mScreenWidthTv.setText(mTBManager.getScreenWidth(this) + "");
        mScreenHeightTv.setText(mTBManager.getScreenHeight(this) + "");
        mMainBacklightSeekbar.setOnSeekBarChangeListener(this);
        mSubBacklightSeekbar.setOnSeekBarChangeListener(this);

        // 网络
        switch (mTBManager.getNetworkType()) {
            case 0:
                mWifiRdo.setChecked(true);
                break;
            case 1:
                m4GRdo.setChecked(true);
                break;
            case 2:
                mEthRdo.setChecked(true);
                break;
        }
        mDhcpBtn.setChecked(mTBManager.isDhcp());
        mDhcpBtn.setOnCheckedChangeListener(this);
        mWifiMacTv.setText(mTBManager.getWiFiMac());
        mEthMacTv.setText(mTBManager.getEthMac());
        mEthIpEdt.setText(mTBManager.getEthIp());
        mEthMaskEdt.setText(mTBManager.getEthNetmask());
        mEthGatewayEdt.setText(mTBManager.getEthGateway());
        mEthDnsEdt1.setText(mTBManager.getEthDns1());
        mEthDnsEdt2.setText(mTBManager.getEthDns2());
        mEthBtn.setChecked(mTBManager.isEthEnabled());
        mEthBtn.setOnCheckedChangeListener(this);

        // 存储
        mSdcardPathTv.setText(mTBManager.getSdcardPath());
        mUsbPathTv.setText(mTBManager.getUsbPath(0));

        // 硬件接口
        if (mTBManager.getGpioNum() > 0) {
            String[] spinnerItems = new String[mTBManager.getGpioNum()];
            for (int i = 0; i < mTBManager.getGpioNum(); i++) {
                spinnerItems[i] = "GPIO_" + i;
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                    R.layout.spinner_item, spinnerItems);
            mGpioSpn.setAdapter(spinnerAdapter);
            mGpioSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    mTBManager.setGpioDirection(position, 1, 0);
                    mGetGpioBtn.setEnabled(false);
                    mGpioBtn.setEnabled(true);
                    mGpioDirectionBtn.setChecked(true);
                    mGpioBtn.setChecked(false);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            mGpioDirectionBtn.setOnCheckedChangeListener(this);
            mGpioBtn.setOnCheckedChangeListener(this);
        } else {
            mGpioDirectionBtn.setEnabled(false);
            mGpioBtn.setEnabled(false);
        }

        // 日志
        mLog2fileBtn.setChecked(mTBManager.isLog2fileOpen());
        mLog2fileBtn.setOnCheckedChangeListener(this);
        mGetLog2fileNumBtn.setText(String.format("获取最大日志文件数(%d)",
                mTBManager.getLogFileNum()));
        mLogPathTv.setText(mTBManager.getLogFilePath());

        // 其它
        m4gKeepliveBtn.setChecked(mTBManager.keepLiveIsOpen());
        m4gKeepliveBtn.setOnCheckedChangeListener(this);
        mKeyInterceptBtn.setChecked(mTBManager.keyInterceptIsOpen());
        mKeyInterceptBtn.setOnCheckedChangeListener(this);

        // 韦根
        mTBManager.setWiegandReadFormat(TBManager.WiegandFormat.WIEGAND_FORMAT_26);
        mWiegandRead26Rdo.setChecked(true);
        mTBManager.setWiegandWriteFormat(TBManager.WiegandFormat.WIEGAND_FORMAT_26);
        mWiegandWrite26Rdo.setChecked(true);

        mWiegandRead26Rdo.setOnCheckedChangeListener(this);
        mWiegandRead34Rdo.setOnCheckedChangeListener(this);
        mWiegandWrite26Rdo.setOnCheckedChangeListener(this);
        mWiegandWrite34Rdo.setOnCheckedChangeListener(this);
    }

    @SuppressLint("DefaultLocale")
    @OnClick({R.id.btn_shutdown, R.id.btn_reboot, R.id.btn_timing_switch, R.id.btn_watchdog_feed,
            R.id.btn_set_watchdog_timeout, R.id.btn_get_watchdog_timeout, R.id.btn_screenshot,
            R.id.btn_rotation, R.id.btn_check_update, R.id.btn_install_update, R.id.btn_verity_update,
            R.id.btn_delete_update, R.id.btn_gpio, R.id.btn_get_gpio,
            R.id.btn_refresh_camera, R.id.btn_set_log2file_num, R.id.btn_get_log2file_num,
            R.id.btn_shell_cmd, R.id.btn_silent_install, R.id.btn_wiegand_read, R.id.btn_wiegand_write})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_shutdown:
                mTBManager.shutdown();
                break;
            case R.id.btn_reboot:
                mTBManager.reboot();
                break;
            case R.id.btn_timing_switch:
                long time = System.currentTimeMillis();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String offDate = dateFormat.format(new Date(time + 360 * 1000)); // 5分钟后关机
                String offTime = timeFormat.format(new Date(time + 360 * 1000)); // 5分钟后关机
                String onDate = dateFormat.format(new Date(time + 660 * 1000)); // 6分钟后开机
                String onTime = timeFormat.format(new Date(time + 660 * 1000)); // 6分钟后开机
                mTBManager.setTimingSwitch(offDate, offTime, onDate, onTime, true);
                break;
            case R.id.btn_watchdog_feed:
                mTBManager.watchdogFeed();
                break;
            case R.id.btn_set_watchdog_timeout:
                mTBManager.setWatchdogTimeout(mTBManager.getWatchdogTimeout() + 1);
                break;
            case R.id.btn_get_watchdog_timeout:
                mGetWatchdogTimeoutBtn.setText(String.format("获取看门狗超时(%d秒)",
                        mTBManager.getWatchdogTimeout()));
                break;
            case R.id.btn_screenshot:
                String screenshot = AppUtils.getRootDir(this)
                        + File.separator + "screenshot.png";
                mTBManager.screenshot(screenshot);
                Toast.makeText(this, "已截屏保存至：" + screenshot,
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_rotation:
                mTBManager.setRotation((mTBManager.getRotation() + 90) % 360, true);
                break;
            case R.id.btn_check_update:
                mTBManager.checkUpdate();
                break;
            case R.id.btn_install_update: {
                File pkgFile = new File(AppUtils.getRootDir(this)
                        + File.separator + "update.zip");
                if (pkgFile.exists()) {
                    mTBManager.installPackage(pkgFile.getAbsolutePath());
                } else {
                    Toast.makeText(this, pkgFile.getAbsolutePath() + "文件不存在",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.btn_verity_update: {
                File pkgFile = new File(AppUtils.getRootDir(this)
                        + File.separator + "update.zip");
                if (pkgFile.exists()) {
                    mTBManager.verifyPackage(pkgFile.getAbsolutePath());
                } else {
                    Toast.makeText(this, pkgFile.getAbsolutePath() + "文件不存在",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.btn_delete_update: {
                File pkgFile = new File(AppUtils.getRootDir(this)
                        + File.separator + "update.zip");
                if (pkgFile.exists()) {
                    mTBManager.deletePackage(pkgFile.getAbsolutePath());
                } else {
                    Toast.makeText(this, pkgFile.getAbsolutePath() + "文件不存在",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.btn_gpio:
                if (mGpioDirectionBtn.isChecked()) {
                    mTBManager.setGpio(mGpioSpn.getSelectedItemPosition(),
                            mGpioBtn.isChecked() ? 1 : 0);
                }
                break;
            case R.id.btn_get_gpio:
                mGpioBtn.setChecked(mTBManager.getGpio(mGpioSpn.getSelectedItemPosition()) > 0);
                break;
            case R.id.btn_refresh_camera:
                mCameraTv.setText("");
                for (int i = 0; i < 10; i++) {
                    String value = AppUtils.getProperty("topband.dev.video" + i, "");
                    if (TextUtils.isEmpty(value)) {
                        break;
                    }
                    String[] ids = value.split(":");
                    int id = mTBManager.getUVCCameraIndex(ids[0], ids[1]);
                    mCameraTv.append(id + " -> " + value + "\n");
                }
                break;
            case R.id.btn_set_log2file_num:
                mTBManager.setLogFileNum(mTBManager.getLogFileNum() + 1);
                break;
            case R.id.btn_get_log2file_num:
                mGetLog2fileNumBtn.setText(String.format("获取最大日志文件数(%d)",
                        mTBManager.getLogFileNum()));
                break;
            case R.id.btn_shell_cmd:
                ShellUtils.CommandResult result = mTBManager.execCmd("uname -a", false);
                mShellCmdTv.setText("$ uname -a\n");
                mShellCmdTv.append(result.toString());
                break;
            case R.id.btn_silent_install:
                File apkFile = new File(AppUtils.getRootDir(this)
                        + File.separator + "test.apk");
                if (apkFile.exists()) {
                    mTBManager.silentInstall(apkFile.getAbsolutePath());
                } else {
                    Toast.makeText(this, apkFile.getAbsolutePath() + "文件不存在",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_wiegand_read:
                mWiegandReadBtn.setText(String.format("韦根读(等待数据...)"));
                mWiegandReadBtn.setEnabled(false);
                Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                        // wiegandRead 为阻塞式接口，需要在子线程中调用，当有数据才返回。
                        emitter.onNext(mTBManager.wiegandRead());
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer data) throws Exception {
                                mWiegandReadBtn.setText(String.format("韦根读(0x%08x)", data));
                                mWiegandReadBtn.setEnabled(true);
                            }
                        });
                break;
            case R.id.btn_wiegand_write:
                mTBManager.wiegandWrite(0x00776677); // 0x00776677 为固定的测试数据
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.btn_watchdog:
                if (b) {
                    mTBManager.openWatchdog();
                } else {
                    mTBManager.closeWatchdog();
                }
                break;
            case R.id.btn_statusbar:
                if (b) {
                    mTBManager.setStatusBar(true);
                } else {
                    mTBManager.setStatusBar(false);
                }
                break;
            case R.id.btn_navbar:
                if (b) {
                    mTBManager.setNavBar(true);
                } else {
                    mTBManager.setNavBar(false);
                }
                break;
            case R.id.btn_backlight:
                if (b) {
                    mTBManager.setBackLight(true);
                } else {
                    mTBManager.setBackLight(false);
                }
                break;
            case R.id.btn_dhcp:
                if (b) {
                    mTBManager.setEthIp(null, null, null, null, null, "DHCP");
                } else {
                    String ip = mEthIpEdt.getText().toString();
                    String mask = mEthMaskEdt.getText().toString();
                    String gateway = mEthGatewayEdt.getText().toString();
                    String dns1 = mEthDnsEdt1.getText().toString();
                    String dns2 = mEthDnsEdt2.getText().toString();
                    if (!TextUtils.isEmpty(ip)
                            && !TextUtils.isEmpty(mask)
                            && !TextUtils.isEmpty(gateway)
                            && !TextUtils.isEmpty(dns1)) {
                        mTBManager.setEthIp(ip, mask, gateway, dns1, dns2, "STATIC");
                    } else {
                        Toast.makeText(this, "请输入IP地址、子网掩码、网关、DNS！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btn_eth:
                mTBManager.setEthEnabled(b);
                break;
            case R.id.btn_gpio_direction:
                if (b) {
                    mTBManager.setGpioDirection(mGpioSpn.getSelectedItemPosition(), 1, 0);
                    mGetGpioBtn.setEnabled(false);
                    mGpioBtn.setEnabled(true);
                } else {
                    mTBManager.setGpioDirection(mGpioSpn.getSelectedItemPosition(), 0, 0);
                    mGetGpioBtn.setEnabled(true);
                    mGpioBtn.setEnabled(false);
                }
                break;
            case R.id.btn_log2file:
                if (b) {
                    mTBManager.openLog2file();
                } else {
                    mTBManager.closeLog2file();
                }
                break;
            case R.id.btn_4g_keeplive:
                if (b) {
                    mTBManager.open4gKeepLive();
                } else {
                    mTBManager.close4gKeepLive();
                }
                break;
            case R.id.btn_key_intercept:
                if (b) {
                    mTBManager.openKeyIntercept();
                } else {
                    mTBManager.closeKeyIntercept();
                }
                break;
            case R.id.rdo_wiegand_read_26:
                if (b) {
                    mTBManager.setWiegandReadFormat(TBManager.WiegandFormat.WIEGAND_FORMAT_26);
                }
                break;
            case R.id.rdo_wiegand_read_34:
                if (b) {
                    mTBManager.setWiegandReadFormat(TBManager.WiegandFormat.WIEGAND_FORMAT_34);
                }
                break;
            case R.id.rdo_wiegand_write_26:
                if (b) {
                    mTBManager.setWiegandWriteFormat(TBManager.WiegandFormat.WIEGAND_FORMAT_26);
                }
                break;
            case R.id.rdo_wiegand_write_34:
                if (b) {
                    mTBManager.setWiegandWriteFormat(TBManager.WiegandFormat.WIEGAND_FORMAT_34);
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.seekbar_main_backlight:
                mTBManager.setBrightness(seekBar.getProgress());
                break;
            case R.id.seekbar_sub_backlight:
                mTBManager.setBrightnessExt(seekBar.getProgress());
                break;
        }
    }
}