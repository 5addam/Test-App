package com.testapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.testapp.adapters.FileAdapter;
import com.testapp.models.StorageFile;
import com.testapp.utils.PermissionCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FileAdapter.OnItemClickListener {
    private Map<Integer, PermissionCallback> permissionCallbackMap = new HashMap<>();
    private Button btnImages, btnAudio, btnVideo, btnDocuments;
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private List<StorageFile> storageFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initialize UI components
        initUI();
    }

    private void initUI() {

        storageFileList = new ArrayList<>();
        btnImages = findViewById(R.id.btn_images);
        btnAudio = findViewById(R.id.btn_audio);
        btnVideo = findViewById(R.id.btn_video);
        btnDocuments = findViewById(R.id.btn_docs);

        btnImages.setOnClickListener(this);
        btnAudio.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnDocuments.setOnClickListener(this);

        // initialize recyclerview
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        requestStoragePermission();
        switch (id) {
            case R.id.btn_images:
                storageFileList = getData("Images");
                if (!storageFileList.isEmpty())
                    adapter.updateData(storageFileList);
                else
                    Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_audio:
                storageFileList = getData("Audios");
                if (!storageFileList.isEmpty())
                    adapter.updateData(storageFileList);
                else
                    Toast.makeText(this, "No audio files found", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_video:
                storageFileList = getData("Videos");
                if (!storageFileList.isEmpty())
                    adapter.updateData(storageFileList);
                else
                    Toast.makeText(this, "No video files found", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_docs:
                storageFileList = getData("Documents");
                if (!storageFileList.isEmpty())
                    adapter.updateData(storageFileList);
                else
                    Toast.makeText(this, "No documents found", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

    }

    private List<StorageFile> getData(String type) {
        List<StorageFile> storageFiles = new ArrayList<>();


        Uri contentURI = null;
        String[] projection = null;
        String whereClause = "";
        String DisplayNameColumn = "";
        String DataColumn = "";
        String MimeTypeColumn = "";
        String DisplayBucketName = "";
        String FileSizeColumn = "";


        if (type.equals("Documents")) { //moduleTypes[3] = "Documents"
            contentURI = MediaStore.Files.getContentUri("external");
            DisplayNameColumn = MediaStore.Files.FileColumns.DISPLAY_NAME;
            DataColumn = MediaStore.Files.FileColumns.DATA;
            MimeTypeColumn = MediaStore.Files.FileColumns.MIME_TYPE;
            DisplayBucketName = MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME;
            FileSizeColumn = MediaStore.Files.FileColumns.SIZE;

            whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN (" +
                    "'application/pdf' , " +
                    "'text/plain'," +
                    "'text/html' , " +
                    "'application/msword' , " +
                    "'application/vnd.ms-excel' , " +
                    "'application/mspowerpoint' ," +
                    "'application/zip') AND " + MediaStore.Files.FileColumns.DATA + " like ? ";

        } else if (type.equals("Images")) {  //moduleTypes[0] = "Images"
            contentURI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            DisplayNameColumn = MediaStore.Images.ImageColumns.DISPLAY_NAME;
            DataColumn = MediaStore.Images.ImageColumns.DATA;
            MimeTypeColumn = MediaStore.Images.ImageColumns.MIME_TYPE;
            DisplayBucketName = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
            FileSizeColumn = MediaStore.Images.ImageColumns.SIZE;
            whereClause = MediaStore.Images.ImageColumns.DATA + " like ? ";

        } else if (type.equals("Audios")) {  //moduleTypes[1] = "Audios"
            contentURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            DisplayNameColumn = MediaStore.Audio.AudioColumns.DISPLAY_NAME;
            DataColumn = MediaStore.Audio.AudioColumns.DATA;
            MimeTypeColumn = MediaStore.Audio.AudioColumns.MIME_TYPE;
            DisplayBucketName = MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME;
            FileSizeColumn = MediaStore.Audio.AudioColumns.SIZE;
            whereClause = MediaStore.Audio.AudioColumns.DATA + " like ? ";
        } else if (type.equals("Videos")) {  //moduleTypes[1] = "Audios"
            contentURI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            DisplayNameColumn = MediaStore.Video.VideoColumns.DISPLAY_NAME;
            DataColumn = MediaStore.Video.VideoColumns.DATA;
            MimeTypeColumn = MediaStore.Video.VideoColumns.MIME_TYPE;
            DisplayBucketName = MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME;
            FileSizeColumn = MediaStore.Video.VideoColumns.SIZE;
            whereClause = MediaStore.Video.VideoColumns.DATA + " like ? ";
        }

        projection = new String[]{DataColumn, DisplayNameColumn, FileSizeColumn};

        String orderBy = MediaStore.Files.FileColumns.SIZE + " DESC";

        Cursor cursor = this.getContentResolver().query(
                contentURI,
                projection,
                whereClause,
                new String[]{"%" + Environment.getExternalStorageDirectory() + "%"},
                orderBy
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                cursor.moveToFirst();
                do {
                    StorageFile storageFile = new StorageFile();

                    if (type.equals("Documents")) {
                        String filePath = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        MediaStore.Images.Media.DATA
                                ));
                        String[] pathArr = filePath.split("/");
                        storageFile.setName(pathArr[pathArr.length - 1]); // file name
                    } else {
                        storageFile.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)));
                    }
                    storageFile.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))); //file path
                    storageFile.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))); //file size

                    storageFiles.add(storageFile);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("Exception", e.getMessage());
            e.printStackTrace();
        }
        return storageFiles;
    }


    private void requestStoragePermission() {
        if (hasPermission(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE})) {
//            startActivity(intent);
            return;
        }
        requestPermission(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                new PermissionCallback() {
                    @Override
                    public void onPermissionDenied(String[] permissions) {
                        Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.storage_read_write_permissions), Snackbar.LENGTH_LONG)
                                .setAction(getResources().getString(R.string.ok),
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                requestStoragePermission();
                                            }
                                        }).setActionTextColor(getResources().getColor(R.color.white)).show();
                    }

                    @Override
                    public void onPermissionGranted(String[] toArray) {

                    }

                    @Override
                    public void onPermissionBlocked(String[] toArray) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {

                } else {
                    Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.storage_read_write_permissions), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.ok),
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).setActionTextColor(getResources().getColor(R.color.white)).show();
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionCallback callback = permissionCallbackMap.get(requestCode);

        if (callback == null) return;

        // Check whether the permission request was rejected.
        if (grantResults.length < 0 && permissions.length >= 0) {
            callback.onPermissionDenied(permissions);
            return;
        }

        List<String> grantedPermissions = new ArrayList<>();
        List<String> blockedPermissions = new ArrayList<>();
        List<String> deniedPermissions = new ArrayList<>();
        int index = 0;

        for (String permission : permissions) {
            List<String> permissionList = grantResults[index] == PackageManager.PERMISSION_GRANTED
                    ? grantedPermissions
                    : !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                    ? blockedPermissions
                    : deniedPermissions;
            permissionList.add(permission);
            index++;
        }

        if (grantedPermissions.size() > 0) {
            callback.onPermissionGranted(
                    grantedPermissions.toArray(new String[grantedPermissions.size()]));
        }

        if (deniedPermissions.size() > 0) {
            callback.onPermissionDenied(
                    deniedPermissions.toArray(new String[deniedPermissions.size()]));
        }

        if (blockedPermissions.size() > 0) {
            callback.onPermissionBlocked(
                    blockedPermissions.toArray(new String[blockedPermissions.size()]));
        }

        permissionCallbackMap.remove(requestCode);
    }

    /**
     * Check whether a permission is granted or not.
     *
     * @param permission
     * @return
     */
    public boolean hasPermission(String permission) {
        if (SDK_INT >= Build.VERSION_CODES.R)
            return Environment.isExternalStorageManager();
        else
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermission(String[] permissions) {
        if (SDK_INT >= Build.VERSION_CODES.R)
            return Environment.isExternalStorageManager();
        else {
            for (String p : permissions) {
                if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Request permissions and get the result on callback.
     *
     * @param permissions
     * @param callback
     */
    public void requestPermission(Activity activity, String[] permissions, @NonNull PermissionCallback callback) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            int requestCode = permissionCallbackMap.size() + 1;
            permissionCallbackMap.put(requestCode, callback);
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }


    /**
     * Request permission and get the result on callback.
     *
     * @param permission
     * @param callback
     */
    public void requestPermission(Activity activity, String permission, @NonNull PermissionCallback callback) {
        int requestCode = permissionCallbackMap.size() + 1;
        permissionCallbackMap.put(requestCode, callback);
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    @Override
    public void onItemClick(StorageFile storageFile) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        File file = new File(storageFile.getPath());
                        if (file.delete()) {
                            //ACTION_MEDIA_SCANNER_SCAN_FILE not work for shredding because it not delete thumbnail of file
                            try {
                                deleteFileFromMediaStore(MainActivity.this.getContentResolver(), file);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //do nothing
                        break;
                    default:
                        break;

                }
            }
        };
    }


    private void deleteFileFromMediaStore(final ContentResolver contentResolver,
                                          final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            contentResolver.delete(uri,
                    MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});

        }
    }
}