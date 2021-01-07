package com.topband.tbapi;

import android.os.Bundle;
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

import com.topband.tbapi.utils.AppUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

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
    @BindView(R.id.tv_watchdog_timeout)
    TextView mWatchdogTimeoutTv;
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
    @BindView(R.id.edt_eth_dns)
    EditText mEthDnsEdt;
    @BindView(R.id.btn_dhcp)
    ToggleButton mDhcpBtn;
    @BindView(R.id.btn_set_ip)
    Button mSetIpBtn;
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

    private TBManager mTBManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTBManager = new TBManager(this);
        mTBManager.init();

        init();
    }

    private void init() {
        mApiVersionTv.setText(mTBManager.getAPIVersion());
        mAndroidVersionTv.setText(mTBManager.getAndroidVersion());
        mFwVersionTv.setText(mTBManager.getFirmwareVersion());
        mDisplayTv.setText(mTBManager.getAndroidDisplay());
        mKernelVersionTv.setText(mTBManager.getFormattedKernelVersion());
        mModelTv.setText(mTBManager.getModel());
        mMemorySizeTv.setText(mTBManager.getMemorySize() / 1024 / 1024 + "MB");
        mStorageSizeTv.setText(mTBManager.getInternalStorageSize() / 1024 / 1024 + "MB");

        mWatchdogBtn.setChecked(mTBManager.watchdogIsOpen());
        mWatchdogBtn.setOnCheckedChangeListener(this);

        mRotationBtn.setText(String.format("旋转屏幕(%d)", mTBManager.getRotation()));
        mStatusbarBtn.setChecked(mTBManager.isStatusBarShow());
        mStatusbarBtn.setOnCheckedChangeListener(this);
        mNavbarBtn.setChecked(mTBManager.isNavBarShow());
        mNavbarBtn.setOnCheckedChangeListener(this);

        mScreenWidthTv.setText(mTBManager.getScreenWidth(this) + "");
        mScreenHeightTv.setText(mTBManager.getScreenHeight(this) + "");

        mMainBacklightSeekbar.setOnSeekBarChangeListener(this);
        mSubBacklightSeekbar.setOnSeekBarChangeListener(this);

        // 初始化网络类型
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

        // 初始化DHCP
        mDhcpBtn.setChecked(mTBManager.isDhcp());
        if (mTBManager.isDhcp()) {
            mSetIpBtn.setEnabled(false);
            mEthIpEdt.setEnabled(false);
            mEthMaskEdt.setEnabled(false);
            mEthGatewayEdt.setEnabled(false);
            mEthDnsEdt.setEnabled(false);
        }
        mDhcpBtn.setOnCheckedChangeListener(this);

        // 初始化MAC地址
        mWifiMacTv.setText(mTBManager.getWiFiMac());
        mEthMacTv.setText(mTBManager.getEthMac());

        // 初始化以太网IP
        mEthIpEdt.setText(mTBManager.getEthIp());
        mEthMaskEdt.setText(mTBManager.getEthMask());
        mEthGatewayEdt.setText(mTBManager.getEthGateway());
        mEthDnsEdt.setText(mTBManager.getEthDns());

        // 初始化存储路径
        mSdcardPathTv.setText(mTBManager.getSdcardPath());
        mUsbPathTv.setText(mTBManager.getUsbPath(0));

        // 初始化GPIO
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
    }

    @OnClick({R.id.btn_shutdown, R.id.btn_reboot, R.id.btn_timing_switch,
            R.id.btn_watchdog_feed, R.id.btn_set_watchdog_timeout, R.id.btn_get_watchdog_timeout,
            R.id.btn_screenshot, R.id.btn_rotation,
            R.id.btn_check_update, R.id.btn_install_update, R.id.btn_verity_update, R.id.btn_delete_update,
            R.id.btn_set_ip, R.id.btn_gpio, R.id.btn_get_gpio})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_shutdown:
                mTBManager.shutdown();
                break;
            case R.id.btn_reboot:
                mTBManager.reboot();
                break;
            case R.id.btn_timing_switch:
                mTBManager.setTimingSwitch("", "", "", "", false);
                break;
            case R.id.btn_watchdog_feed:
                mTBManager.watchdogFeed();
                break;
            case R.id.btn_set_watchdog_timeout:
                mTBManager.setWatchdogTimeout(mTBManager.getWatchdogTimeout() + 1);
                break;
            case R.id.btn_get_watchdog_timeout:
                mWatchdogTimeoutTv.setText(mTBManager.getWatchdogTimeout() + "秒");
                break;
            case R.id.btn_screenshot:
                mTBManager.screenshot("");
                break;
            case R.id.btn_rotation:
                mTBManager.setRotation((mTBManager.getRotation() + 90) % 360, true);
                break;
            case R.id.btn_check_update:
                mTBManager.checkUpdate();
                break;
            case R.id.btn_install_update:
                mTBManager.installPackage(AppUtils.getRootDir(this)
                        + File.separator + "update.zip");
                break;
            case R.id.btn_verity_update:
                mTBManager.verifyPackage(AppUtils.getRootDir(this)
                        + File.separator + "update.zip");
                break;
            case R.id.btn_delete_update:
                mTBManager.deletePackage(AppUtils.getRootDir(this)
                        + File.separator + "update.zip");
                break;
            case R.id.btn_set_ip:
                String ip = mEthIpEdt.getText().toString();
                String mask = mEthMaskEdt.getText().toString();
                String gateway = mEthGatewayEdt.getText().toString();
                String dns = mEthDnsEdt.getText().toString();
                if (!TextUtils.isEmpty(ip)
                        && !TextUtils.isEmpty(mask)
                        && !TextUtils.isEmpty(gateway)
                        && !TextUtils.isEmpty(dns)) {
                    mTBManager.setEthIp(ip, mask, gateway, dns);
                } else {
                    Toast.makeText(this, "请输入IP地址、子网掩码、网关、DNS！",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_gpio:
                if (mGpioDirectionBtn.isChecked()) {
                    mTBManager.setGpio(mGpioSpn.getSelectedItemPosition(),
                            mGpioBtn.isChecked() ? 1 : 0);
                }
                break;
            case R.id.btn_get_gpio:
                mGpioBtn.setChecked(mTBManager.getGpio(mGpioSpn.getSelectedItemPosition()) > 0);
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
                    mTBManager.setDhcp(true);
                    mSetIpBtn.setEnabled(false);
                    mEthIpEdt.setEnabled(false);
                    mEthMaskEdt.setEnabled(false);
                    mEthGatewayEdt.setEnabled(false);
                    mEthDnsEdt.setEnabled(false);
                } else {
                    mTBManager.setDhcp(false);
                    mSetIpBtn.setEnabled(true);
                    mEthIpEdt.setEnabled(true);
                    mEthMaskEdt.setEnabled(true);
                    mEthGatewayEdt.setEnabled(true);
                    mEthDnsEdt.setEnabled(true);
                }
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