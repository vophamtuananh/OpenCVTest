package anh.com.opencvtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION_CAMERA_REQUEST_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 104;
    private static final String TAG = "OCVSample::Activity";
    CascadeClassifier face_cascade = null;

    static {
        System.loadLibrary("face-detection-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    openCamera();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    protected Uri fileUri;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.img_Test);

        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            openCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (fileUri != null && fileUri.getPath() != null) {
                    Mat m = Imgcodecs.imread(fileUri.getPath(), CvType.CV_8UC4);
                    Mat mat = new Mat();
                    FaceDetection.removeBG(m.getNativeObjAddr(), mat.getNativeObjAddr());
                    if (face_cascade.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        face_cascade = null;
                        return;
                    } else{
                        Log.e(TAG, "Loaded cascade classifier from ");
                    }


                    MatOfRect faceDetections = new MatOfRect();

                    //face_cascade.detectMultiScale(mat, faceDetections);
                    Log.e(TAG, "Co " + faceDetections.size() + " khop.");

                    for (Rect rect : faceDetections.toArray()){
                        Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                    }
                    //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
                    Boolean bool = false;
                    Bitmap bm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mat, bm);
                    if (bool == true)
                        Log.d(TAG, "SUCCESS writing image to external storage");
                    else
                        Log.d(TAG, "Fail writing image to external storage");
                    imageView.setImageBitmap(bm);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                camera();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void openCamera() {
        if (face_cascade == null)
            load_cascade();
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CAMERA_REQUEST_CODE);
            } else {
                camera();
            }
        } else {
            camera();
        }
    }

    private void camera() {
        fileUri = Uri.fromFile(FileUtil.getOutputMediaFile(getApplicationContext()));
        Intent intent = getCaptureImageIntent(fileUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public static Intent getCaptureImageIntent(Uri file) {
        Intent chooserIntent = null;
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        chooserIntent = Intent.createChooser(takePhotoIntent, "Selection Photo");
        return chooserIntent;
    }

    public void load_cascade() {
        File cascadeDir = FileUtil.getDiskCacheDir(getApplicationContext(), "cascades");
        if (!cascadeDir.exists())
            cascadeDir.mkdir();
        File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalcatface.xml");
        if (!mCascadeFile.exists()) {
            try {
                InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalcatface);
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

                face_cascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());

                Log.e("MyActivity", "Cascades loaded");
            } catch (IOException e) {
                Log.e("MyActivity", "Failed to load cascade. Exception thrown: " + e);
            }
        } else {
            face_cascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        }
        if (face_cascade.empty()) {
            Log.e(TAG, "Failed to load cascade classifier");
            face_cascade = null;
            return;
        } else{
            Log.e(TAG, "Loaded cascade classifier from ");
        }
    }
}
