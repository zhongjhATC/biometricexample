package com.example.biometricexample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Android 6 使用 FingerprintManager 指纹授权
 * Android 10 使用 BiometricManager 生物认证
 * 谷歌和三星我测试能调出来人脸，小米魅族就不行，甚至只能调出指纹解锁，没有回退到密码解锁的dialog
 */
public class MainActivity extends AppCompatActivity {

    ViewHolder mViewHolder;
    private static CancellationSignal cancellationSignal = null; // 用于取消指纹解锁

    private Handler handler = new Handler(); // 用于生物特征解锁的
    // 用于生物特征解锁的
    private Executor executor = new Executor() {
        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewHolder = new ViewHolder(MainActivity.this);
        mViewHolder.btnBiometric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBiometric();
            }
        });
        mViewHolder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancellationSignal != null)
                    cancellationSignal.cancel();
            }
        });
        // 跳转到指纹设置界面
        mViewHolder.btnToFingerprintSettingsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerprintSettings.getInstance(MainActivity.this).startFingerprintActivity();
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) ==
                    PackageManager.PERMISSION_GRANTED) {
                startFingerprint();
            }
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startFingerprint() {
        FingerprintManager fingerprintManager = (FingerprintManager) MainActivity.this.getSystemService(Context.FINGERPRINT_SERVICE);
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
            if (cancellationSignal == null) {
                cancellationSignal = new CancellationSignal();
            }

            fingerprintAuthenticate(fingerprintManager, null, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    // 多次指纹密码验证错误后，进入此方法；并且，不可再验（短时间） errorCode是失败的次数
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(MainActivity.this, "多次错误，不可再验", Toast.LENGTH_SHORT).show();
                    mViewHolder.imgFingerprint.setVisibility(View.GONE);
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    // 指纹验证失败，可再验，可能手指过脏，或者移动过快等原因。
                    super.onAuthenticationHelp(helpCode, helpString);
                    Toast.makeText(MainActivity.this, "指纹验证失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    // 指纹密码验证成功
                    super.onAuthenticationSucceeded(result);
                    mViewHolder.imgFingerprint.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "指纹密码验证成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    // 指纹验证失败，指纹识别失败，可再验，错误原因为：该指纹不是系统录入的指纹。
                    super.onAuthenticationFailed();
                    Toast.makeText(MainActivity.this, "指纹验证失败", Toast.LENGTH_SHORT).show();
                }
            }, null);

            // 显示指纹view
            mViewHolder.imgFingerprint.setVisibility(View.VISIBLE);
        }
    }

    /**
     * @param crypto   用于通过指纹验证取出AndroidKeyStore中的key的对象，用于加密
     * @param cancel   用来取消指纹验证，如果想手动关闭验证，可以调用该参数的cancel方法
     * @param flags    没什么意义，就是传0就好了
     * @param callback 最重要，由于指纹信息是存在系统硬件中的，app是不可以访问指纹信息的，所以每次验证的时候，系统会通过这个callback告诉你是否验证通过、验证失败等
     * @param handler  FingerPrint中的消息都通过这个Handler来传递消息，如果你传空，则默认创建一个在主线程上的Handler来传递消息，没什么用，传null好了
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void fingerprintAuthenticate(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject crypto, CancellationSignal cancel,
                                         int flags, FingerprintManager.AuthenticationCallback callback, Handler handler) {
        fingerprintManager.authenticate(crypto, cancel, flags, callback, handler);
    }

//    private void

    public static class ViewHolder {

        public TextView tvBiometric;
        public Button btnBiometric;
        public Button btnCancel;
        public Button btnToFingerprintSettingsActivity;
        public ImageView imgFingerprint;

        public ViewHolder(MainActivity rootView) {
            this.tvBiometric = rootView.findViewById(R.id.tvBiometric);
            this.btnBiometric = rootView.findViewById(R.id.btnBiometric);
            this.btnCancel = rootView.findViewById(R.id.btnCancel);
            this.btnToFingerprintSettingsActivity = rootView.findViewById(R.id.btnToFingerprintSettingsActivity);
            this.imgFingerprint = rootView.findViewById(R.id.imgFingerprint);
        }

    }
}