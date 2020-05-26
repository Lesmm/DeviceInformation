#include <jni.h>
#include <string>
#include <android/log.h>

#define DEBUG

#ifdef DEBUG
    #define LOG_TAG "DEBUG"
    #define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
    #define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
    #define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))
    #define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#else
    #define LOGD(fmt, ...) printf(fmt"\n", ##__VA_ARGS__)
    #define LOGI(fmt, ...) printf(fmt"\n", ##__VA_ARGS__)
    #define LOGW(fmt, ...) printf(fmt"\n", ##__VA_ARGS__)
    #define LOGE(fmt, ...) printf(fmt"\n", ##__VA_ARGS__)
#endif


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if(vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    LOGW("-------- JNI_OnLoad --------");
    return JNI_VERSION_1_4;
}
