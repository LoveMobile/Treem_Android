package com.treem.treem.activities.image;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.treem.treem.R;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.Utils;

import java.io.File;

/**
 * Image select activity *
 */
public class ImageSelectActivity extends AppCompatActivity {

    //Request code for camera image capture
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    //Request code for gallery image capture
    private static final int REQUEST_IMAGE_SELECT = 2;

    //Request code for write image request permission
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    //Buttons
    private ViewGroup buttonAlbum;
    private ViewGroup buttonCamera;

    //Root view to handle cancel clicks
    private View rootView;

    //Temp file for camera capture
    private File mTmpPath;

    //Capture actions
    private enum TakeAction {
        takeFromGallery,
        takeFromCamera
    }

    //Last selected capture action
    private TakeAction action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        handleViews();
        setWidgetActions();
    }

    /**
     * Set widgets actions
     */
    private void setWidgetActions() {
        buttonAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTakeFromGallery();
            }
        });
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTakeFromCamera();
            }
        });
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    /**
     * Check permissions and start take image from camera
     */
    private void startTakeFromCamera() {
        action = TakeAction.takeFromCamera;
        if (checkWritePermissions()) {
            captureCameraImage();
        }
    }

    /**
     * Check permissions and start take image from gallery
     */
    private void startTakeFromGallery() {
        action = TakeAction.takeFromGallery;
        if (checkWritePermissions()) {
            selectGalleryImage();
        }
    }

    /**
     * Handle layout views
     */
    private void handleViews() {
        buttonAlbum = (ViewGroup) findViewById(R.id.imageButtonAlbums);
        buttonCamera = (ViewGroup) findViewById(R.id.imageButtonCamera);
        rootView = findViewById(R.id.imageRoot);
    }

    /**
     * Check write external storage permissions. Start request permission if not
     * @return true if permission granted or false if not
     */
    private boolean checkWritePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showWritePermissionDescription();
            } else {
                requestWritePermissions();
            }
            return false;
        }
        return true;
    }

    /**
     * Show write external storage permission description dialog
     */
    private void showWritePermissionDescription() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle(R.string.title_write_describe_permissions);
        builder.setMessage(R.string.msg_write_describe_permissions);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestWritePermissions();
            }
        });
        builder.show();
    }

    /**
     * Request write external storage permission
     */
    private void requestWritePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * On request permission result handler
     * @param requestCode permission request code
     * @param permissions the array of permissions
     * @param grantResults result of action
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted
                    if (action == TakeAction.takeFromCamera)
                        captureCameraImage();
                    else if (action == TakeAction.takeFromGallery)
                        selectGalleryImage();
                } else {
                    Toast.makeText(ImageSelectActivity.this, R.string.error_cant_capture_image_without_permission, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    /**
     * Select image from gallery
     */
    private void selectGalleryImage() {
        Intent galleryIntent;
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

        }
        else{
            galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        galleryIntent.setType("image/*");
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECT);
        }
    }

    /**
     * Capture image from camera
     */
    private void captureCameraImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = Utils.createMediaFile(".jpg"); //create temp file for capture image
            if (photoFile == null) {
                Toast.makeText(this, R.string.error_failed_create_file, Toast.LENGTH_LONG).show();
                return;
            }
            mTmpPath = photoFile;
            // Continue only if the File was successfully created
            Uri fileUri = Uri.fromFile(photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_SELECT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    processCapturedFile(data.getData());
                }
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                processCapturedFile(Uri.fromFile(mTmpPath));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Process capture image. Check media type to be sure the file is supported
     * @param uri the image uri
     */
    private void processCapturedFile(Uri uri) {
        String mimeType = Utils.getMimeType(ImageSelectActivity.this,uri); //get uri media type
        if (mimeType!=null) {
            int idx = mimeType.indexOf('/');
            if (idx > 0) {
                String type = mimeType.substring(0, idx);
                String subtype = mimeType.substring(idx + 1);
                if (type.equals("image") && Utils.isImageSupported(subtype)) { //check is this an image and supported type
                    returnImageSuccess(uri);
                    return;
                }
            }
        }
        NotificationHelper.showError(ImageSelectActivity.this,getString(R.string.media_type_not_supported));
    }

    /**
     * Send uri to parent activity
     * @param uri the image uri
     */
    private void returnImageSuccess(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }
}
