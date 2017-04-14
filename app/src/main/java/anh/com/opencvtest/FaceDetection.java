package anh.com.opencvtest;

/**
 * Created by vophamtuananh on 4/13/17.
 */

public class FaceDetection {
    public native static void faceDetection(long addrRgba);

    public native static void removeBG(long addrRgbaOrigin, long addrRgba);
}
