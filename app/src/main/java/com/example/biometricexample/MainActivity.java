package com.example.biometricexample;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

/**
 * Android 6 使用 FingerprintManager 指纹授权
 * Android 10 使用 BiometricManager 生物认证
 * 谷歌和三星我测试能调出来人脸，小米魅族就不行，甚至只能调出指纹解锁，没有回退到密码解锁的dialog
 */
public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    private Executor executor = new Executor() {
        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    };

    ViewHolder mViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewHolder = new ViewHolder(MainActivity.this);
        mViewHolder.btnBiometric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBiometricPrompt();
            }
        });
        init();
    }

    /**
     * 初始化相关信息
     */
    private void init() {
        mViewHolder.btnBiometric.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BiometricManager biometricManager = BiometricManager.from(this);
            switch (biometricManager.canAuthenticate()) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    mViewHolder.tvBiometric.setText("应用可以进行生物识别技术进行身份验证。");
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    mViewHolder.tvBiometric.setText("该设备上没有搭载可用的生物特征功能。");
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    mViewHolder.tvBiometric.setText("生物识别功能当前不可用。");
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    mViewHolder.tvBiometric.setText("用户没有录入生物识别数据。");
                    break;
            }
            mViewHolder.btnBiometric.setEnabled(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) getApplication().getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                mViewHolder.tvBiometric.setText("没有指纹识别模块。");
                return;
            }
            //判断是否有指纹录入
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(getApplication(), "没有指纹录入", Toast.LENGTH_SHORT).show();
                return;
            }
            mViewHolder.btnBiometric.setEnabled(true);
        }
    }

    /**
     * 根据sdk版本显示不同的
     */
    private void showBiometric() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startBiometricPrompt();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startFingerprint();
        }
    }

    /**
     * 生物认证的setting
     */
    private void startBiometricPrompt() {
        // 这种版本是可以取消的
        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("人脸识别") //设置大标题
                        .setSubtitle("扫描人脸登录") // 设置标题下的提示
                        .setNegativeButtonText("Cancel") //设置取消按钮
                        .setDeviceCredentialAllowed(false)
                        .build();
//        // 这种版本是没有取消，可以同时密码和人脸一起
//        BiometricPrompt.PromptInfo promptInfo =
//                new BiometricPrompt.PromptInfo.Builder()
//                        .setTitle("人脸识别") //设置大标题
//                        .setSubtitle("扫描人脸登录") // 设置标题下的提示
//                        .setDeviceCredentialAllowed(true)
//                        .build();

        // 需要提供的参数callback
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            // 各种异常的回调
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            // 认证成功的回调
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                BiometricPrompt.CryptoObject authenticatedCryptoObject =
                        result.getCryptoObject();
                // User has verified the signature, cipher, or message
                // authentication code (MAC) associated with the crypto object,
                // so you can use it in your app's crypto-driven workflows.
            }

            // 认证失败的回调
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // 显示认证对话框
        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * 6.0的指纹认证
     */
    private void startFingerprint() {
        FingerprintManager fingerprintManager = (FingerprintManager) MainActivity.this.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    public static class ViewHolder {

        public TextView tvBiometric;
        public Button btnBiometric;

        public ViewHolder(MainActivity rootView) {
            this.tvBiometric = rootView.findViewById(R.id.tvBiometric);
            this.btnBiometric = rootView.findViewById(R.id.btnBiometric);
        }

    }
}