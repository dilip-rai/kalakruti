//
// Created by Dilip Kumar Rai on 03/01/16.
//

#ifndef KALAKRUTI_SKIAPATH_H
#define KALAKRUTI_SKIAPATH_H

#include "SkPath.h"
#include <jni.h>

SkPath *getSkPath(JNIEnv *env, jobject skiapath);

#endif //KALAKRUTI_SKIAPATH_H
