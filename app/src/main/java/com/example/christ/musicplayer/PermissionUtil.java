package com.example.christ.musicplayer;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by christ on 2018/5/22.
 */

public class PermissionUtil {
    public static boolean hasPermission = false;

    public static boolean verifyStoragePermissions(Activity activity, String[] PERMISSIONS){
        try {
            boolean result = checkPermissionAllGranted(activity, PERMISSIONS);
            if(result){
                hasPermission = true;
            } else {
                ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return hasPermission;
    }
    private static boolean checkPermissionAllGranted(Activity activity, String[] PERMISSIONS) {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }
}
