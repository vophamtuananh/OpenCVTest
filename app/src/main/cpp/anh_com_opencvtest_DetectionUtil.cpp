/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <string>
#include <anh_com_opencvtest_DetectionUtil.h>

extern "C"

jstring JNICALL Java_anh_com_opencvtest_DetectionUtil_getMessageFromJNI
  (JNIEnv *env, jclass obj) {
    std::string hello = "This is message from JNI";
    return env->NewStringUTF(hello.c_str());
}
