package com.loe.scan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.king.zxing.CaptureHelper;
import com.king.zxing.Intents;
import com.king.zxing.ViewfinderView;
import com.king.zxing.util.CodeUtils;

public class ScanActivity extends FragmentActivity
{
    private SurfaceView surfaceView;

    private ViewfinderView viewfinderView;

    private CaptureHelper mCaptureHelper;

    private View viewTitle;

    private TextView textTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(0xFF000000);
        }
        setContentView(R.layout.scan_activity);

        String title = getIntent().getStringExtra("title");


        surfaceView = findViewById(R.id.surfaceView);
        viewfinderView = findViewById(R.id.viewfinderView);

        viewTitle = findViewById(R.id.viewTitle);
        textTitle = findViewById(R.id.textTitle);

        if (title != null)
        {
            textTitle.setText(title);
        }

        mCaptureHelper = new CaptureHelper(this, surfaceView, viewfinderView);
        mCaptureHelper.onCreate();
        mCaptureHelper.vibrate(true).fullScreenScan(true).supportVerticalCode(true);

        viewTitle.setPadding(0, getStatusHeight(), 0, 0);

        if (!allPermissionsGranted())
        {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 0);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mCaptureHelper.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mCaptureHelper.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mCaptureHelper.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mCaptureHelper.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void finish()
    {
        super.finish();
        if (LoeScan.onDestroyAnimate != null)
        {
            LoeScan.onDestroyAnimate.onOut(this);
        }
    }

    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private boolean allPermissionsGranted()
    {
        for (String requiredPermission : REQUIRED_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(this, requiredPermission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (!allPermissionsGranted())
        {
            Toast.makeText(this, "缺少拍照相关权限", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private int getStatusHeight()
    {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && resourceId > 0)
        {
            int heightSize = getResources().getDimensionPixelSize(resourceId);
            return heightSize;
        }
        else
        {
            return 0;
        }
    }

    /**
     * 关闭闪光灯（手电筒）
     */
    private void offFlash()
    {
        Camera camera = mCaptureHelper.getCameraManager().getOpenCamera().getCamera();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    /**
     * 开启闪光灯（手电筒）
     */
    public void openFlash()
    {
        Camera camera = mCaptureHelper.getCameraManager().getOpenCamera().getCamera();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    public void back(View v)
    {
        finish();
    }

    public void album(View v)
    {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        ScanResultUtil.startResult(this, pickIntent, new ScanResultUtil.OnActivityResultListener()
        {
            @Override
            public void onActivityResult(int resultCode, Intent data)
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    parsePhoto(data);
                }
            }
        });
    }

    private void parsePhoto(Intent data)
    {
        final String path = ScanUriUtil.INSTANCE.getImagePath(this, data);
        if (TextUtils.isEmpty(path))
        {
            return;
        }
        //异步解析
        new Thread()
        {
            @Override
            public void run()
            {
                final String result = CodeUtils.parseCode(path);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(result == null || result.isEmpty())
                        {
                            Toast.makeText(ScanActivity.this, "未发现二维码或条形码", Toast.LENGTH_SHORT).show();
                        }else
                        {
                            setResult(RESULT_OK, new Intent().putExtra(Intents.Scan.RESULT, result));
                            finish();
                        }
                    }
                });
            }
        }.start();
    }

    public void clickLight(View v)
    {
        if (v.isSelected())
        {
            offFlash();
            v.setSelected(false);
        }
        else
        {
            openFlash();
            v.setSelected(true);
        }
    }
}