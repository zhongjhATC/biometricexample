package com.example.biometricexample;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;

/**
 * 综合控制所有界面跳转到指纹界面的
 * https://blog.csdn.net/zhuiying865/article/details/79546186
 * https://www.cnblogs.com/android-deli/p/10369526.html
 */
public class FingerprintSettings {

    private final String SONY = "sony";
    private final String OPPO = "oppo";
    private final String HUAWEI = "huawei";
    private final String HONOR = "honor";
    private final String KNT = "knt";

    private static FingerprintSettings instance = null;
    private Context mContext;

    /**
     * 构造
     *
     * @param context 上下文
     */
    public FingerprintSettings(Context context) {
        mContext = context;
    }

    public static synchronized FingerprintSettings getInstance(Context context) {
        if (null == instance)
            instance = new FingerprintSettings(context);
        return instance;
    }

    /**
     * 跳转到指纹页面 或 通知用户去指纹录入
     */
    public void startFingerprintActivity() {
        String pcgName = null;
        String clsName = null;

        if (compareTextSame(SONY)) {
            pcgName = "com.android.settings";
            clsName = "com.android.settings.Settings$FingerprintEnrollSuggestionActivity";
        } else if (compareTextSame(OPPO)) {
            pcgName = "com.coloros.fingerprint";
            clsName = "com.coloros.fingerprint.FingerLockActivity";
        } else if (compareTextSame(HUAWEI)) {
            pcgName = "com.android.settings";
            clsName = "com.android.settings.fingerprint.FingerprintSettingsActivity";
        } else if (compareTextSame(HONOR)) {
            pcgName = "com.android.settings";
            clsName = "com.android.settings.fingerprint.FingerprintSettingsActivity";
        } else {
            // 如果以上判断没有符合该机型，那就跳转到设置界面，让用户自己设置吧
            showSettingDialog();
        }

        // 一加
//        Intent intent;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            intent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
//        } else {
//            intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
//        }
//        startActivity(intent);

        // 小米
//        Intent intent = new Intent();
//        String pcgName = "com.android.settings";
//        String clsName = "com.android.settings.NewFingerprintActivity";
//        ComponentName componentName = new ComponentName(pcgName, clsName);
//        intent.setComponent(componentName);
//        intent.setAction("android.settings.FINGERPRINT_SETUP");
//        startActivity(intent);
        if (isExistActivity(pcgName, clsName)) {
            if (!TextUtils.isEmpty(pcgName) && !TextUtils.isEmpty(clsName)) {
                showActivity(pcgName, clsName);
            }
        } else {
            showSettingDialog();
        }
    }

    /**
     * 对比两个字符串，并且比较字符串是否包含在其中的，并且忽略大小写
     *
     * @param brand 品牌
     * @return 是否包含
     */
    private boolean compareTextSame(String brand) {
        return brand.toUpperCase().contains(getBrand().toUpperCase());
    }

    /**
     * 获得当前手机品牌
     *
     * @return 例如：HONOR
     */
    private String getBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 打开窗口
     *
     * @param packageName 包名
     * @param className   类名
     */
    private void showActivity(String packageName, String className) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(packageName, className);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(componentName);
        mContext.startActivity(intent);
    }

    /**
     * 判断系统是否有该activity
     *
     * @param packageName 包名
     * @param className   类名
     * @return 是否存在
     */
    private boolean isExistActivity(String packageName, String className) {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        if (mContext.getPackageManager().resolveActivity(intent, 0) == null) {
            // 说明系统中不存在这个activity
            return false;
        } else {
            return true;
        }
    }

    /**
     * 显示是否跳转到设置的dialog
     */
    private void showSettingDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle("指纹录入")
                .setMessage("请到设置中，找到指纹录入，进行指纹录入操作")
                .setPositiveButton("好的，我去录入指纹", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到Settings页面的Intent
                        String pcgName = "com.android.settings";
                        String clsName = "com.android.settings.Settings";
                        showActivity(pcgName, clsName);
                    }
                })
                .show();
    }

}
