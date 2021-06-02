package com.testapp.utils;

public interface PermissionCallback {
    void onPermissionDenied(String[] permissions);

    void onPermissionGranted(String[] toArray);

    void onPermissionBlocked(String[] toArray);
}
