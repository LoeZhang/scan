package com.loe.scan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class ScanResultUtil
{
    private static final String TAG = "ScanResultUtil";

    public static void startResult(FragmentActivity activity, Intent intent, OnActivityResultListener listener)
    {
        FragmentManager manager = activity.getSupportFragmentManager();
        ActivityResultFragment fragment = (ActivityResultFragment) manager.findFragmentByTag(TAG);
        if (fragment == null)
        {
            fragment = new ActivityResultFragment();
            manager.beginTransaction()
                    .add(fragment, TAG)
                    .commitNowAllowingStateLoss();
        }
        fragment.listener = listener;
        fragment.startActivityForResult(intent, 41);
    }

    public static class ActivityResultFragment extends Fragment
    {
        public OnActivityResultListener listener;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode != 41) return;

            if (listener != null)
            {
                listener.onActivityResult(resultCode, data);
            }
        }
    }

    public interface OnActivityResultListener
    {
        void onActivityResult(int resultCode, Intent data);
    }
}