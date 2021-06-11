package com.app.maps_vijaylingamneni_c0800126;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class LocationPermissionHelper {
    static PermissionListener permissionListener;
    static Activity context;
    static int  requestCode = 1001;


    public static String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    static String locationPermErrorMsg = "Location permission denied. You can enable permission from settings";


    public static void askPermission(final Activity context, final String permission, final PermissionListener permissionListener) {
        LocationPermissionHelper.context = context;
        LocationPermissionHelper.permissionListener = permissionListener;

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode);
            return;
        } else {
            permissionListener.onPermissionResult(true);
            permissionListener.onPermissionResult(true);
        }
    }

    public static void onRequestPermissionsResult(final int requestCode, String[] permissions, int[] grantResults) {
        if(permissions.length>0){
        final String permission = permissions[0];
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionListener.onPermissionResult(true);
        }
        else  if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            permissionListener.onPermissionResult(true);

        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission required");

                 if (permission.equals(LOCATION))
                    alertBuilder.setMessage("Location permission");


                alertBuilder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode);
                    }
                });
                alertBuilder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        permissionListener.onPermissionResult(false);
                    }
                });
                final AlertDialog alert = alertBuilder.create();
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        // alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.bgColor));
                        // alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.bgColor));

                    }
                });
                alert.show();
            } else {
                 if (permission.equals(LOCATION))
                    Toast.makeText(context, locationPermErrorMsg, Toast.LENGTH_LONG).show();


                permissionListener.onPermissionResult(false);
            }
        }
        }
        return;
    }

    public interface PermissionListener {
        void onPermissionResult(boolean isGranted);
    }
}
