#include <jni.h>
#include <string>

extern "C"
jstring
Java_anh_com_opencvtest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "This is message from JNI";
    return env->NewStringUTF(hello.c_str());
}
