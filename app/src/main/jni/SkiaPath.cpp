//
// Created by Dilip Kumar Rai on 03/01/16.
//
#include "SkPath.h"
#include "SkPathOps.h"

#define LOGD(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG, "SkiaGLRenderer", fmt, __VA_ARGS__)

struct SkiaPathImpl {
    SkPath path;
    //SkPath simplePath;
};

static jfieldID getFieldID(JNIEnv *env, jobject obj, const char *name, const char *signature) {
    jclass cls = env->GetObjectClass(obj);
    return env->GetFieldID(cls, "handle", "J");
}

static void setSkPath(JNIEnv *env, jobject skiapath, SkiaPathImpl *skpath) {
    jlong handle = reinterpret_cast<jlong>(skpath);
    env->SetLongField(skiapath, getFieldID(env, skiapath, "handle", "J"), handle);
}

SkiaPathImpl *getSkiaPath(JNIEnv *env, jobject skiapath) {
    jlong handle = env->GetLongField(skiapath, getFieldID(env, skiapath, "handle", "J"));
    return reinterpret_cast<SkiaPathImpl *>(handle);
}


SkPath *getSkPath(JNIEnv *env, jobject skiapath) {
    SkiaPathImpl *impl = getSkiaPath(env, skiapath);
    if (impl) {
        //return &impl->simplePath;
        return &impl->path;
    }
    return NULL;
}

/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaPath
 * Method:    init
 * Signature: ()J
 */
extern "C" JNIEXPORT jlong JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaPath_init
        (JNIEnv *, jclass) {
    SkiaPathImpl *impl = new SkiaPathImpl();
    //path->setFillType(SkPath::kWinding_FillType);
    return reinterpret_cast<jlong>(impl);
}

/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaPath
 * Method:    destroy
 * Signature: ()V
 */
extern "C" JNIEXPORT void JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaPath_destroy
        (JNIEnv *env, jobject skiapath) {
    SkiaPathImpl *impl = getSkiaPath(env, skiapath);
    if (impl) {
        delete impl;
        setSkPath(env, skiapath, NULL);
    }
}

/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaPath
 * Method:    moveTo
 * Signature: (FF)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaPath_moveTo
        (JNIEnv *env, jobject skiapath, jfloat x, jfloat y) {
    SkiaPathImpl *impl = getSkiaPath(env, skiapath);
    impl->path.moveTo(x, y);
    //impl->simplePath.moveTo(x, y);
}

/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaPath
 * Method:    lineTo
 * Signature: (FF)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaPath_lineTo
        (JNIEnv *env, jobject skiapath, jfloat x, jfloat y) {
    SkiaPathImpl *impl = getSkiaPath(env, skiapath);
    impl->path.lineTo(x, y);
    int count = impl->path.countPoints();
    if (count % 1000 == 0) {
        LOGD("Path point count: %d", count);
    }
    /*if (count % 1000 == 0) {
        int n1, n2;
        n1 = impl->path.countPoints();
        if (Simplify(impl->path, &impl->simplePath)) {
            impl->path = impl->simplePath;
        }
        n2 = impl->path.countPoints();
        LOGD("Simplified path: Initial = %d final = %d", n1, n2);

    } else {
        impl->simplePath.lineTo(x, y);
    }*/
}
