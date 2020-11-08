package com.loe.scan;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.king.zxing.Intents;

public class LoeScan
{
    public static void scan(FragmentActivity activity, final OnStringCallback callback)
    {
        Intent intent = new Intent(activity, ScanActivity.class);
        ScanResultUtil.startResult(activity, intent, new ScanResultUtil.OnActivityResultListener()
        {
            @Override
            public void onActivityResult(int resultCode, Intent data)
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    String s = data.getStringExtra(Intents.Scan.RESULT);
                    callback.onString(s);
                }
            }
        });
        if (onDestroyAnimate != null)
        {
            onDestroyAnimate.onEnter(activity);
        }
    }

    public interface OnStringCallback
    {
        void onString(String s);
    }

    static OnDestroyAnimate onDestroyAnimate = new OnDestroyAnimate()
    {
        @Override
        public void onEnter(Activity activity)
        {
            activity.overridePendingTransition(R.anim.scan_scale_in, R.anim.scan_on);
        }

        @Override
        public void onOut(Activity activity)
        {
            activity.overridePendingTransition(R.anim.scan_on, R.anim.scan_scale_out);
        }
    };

    public static void setOnDestroyAnimate(OnDestroyAnimate onDestroyAnimate)
    {
        LoeScan.onDestroyAnimate = onDestroyAnimate;
    }

    public interface OnDestroyAnimate
    {
        void onEnter(Activity activity);

        void onOut(Activity activity);
    }
}
