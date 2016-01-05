//
// Created by Dilip Kumar Rai on 02/01/16.
//
#include "GrContext.h"
#include "SkCanvas.h"
#include "SkSurface.h"
#include "SkRect.h"
#include "SkPathOps.h"

#define LOGD(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG, "SkiaGLRenderer", fmt, __VA_ARGS__)

static SkAutoTUnref <GrContext> gGrContext;
static SkAutoTUnref <SkSurface> gSurface;
static SkAutoTUnref <SkSurface> gSurface2;

class SkiaGLRendererImpl {
private:
    SkMatrix modelToView;
public:
    void SetModelToViewMatrix(const SkScalar &width, const SkScalar &height) {
        SkScalar mat[] = {1, 0, width / 2, 0, -1, height / 2, 0, 0, 1};
        modelToView.set9(mat);
    }

    const SkMatrix &GetModelToViewMatrix() const {
        return modelToView;
    }

    void mapRect(SkRect *rect) {
        modelToView.mapRect(rect);
    }
};

static SkiaGLRendererImpl gRendererImpl;


/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaGLRenderer
 * Method:    init
 * Signature: ()V
 */
extern "C" JNIEXPORT void JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaGLRenderer_init
        (JNIEnv *, jclass) {
    gGrContext.reset(GrContext::Create(kOpenGL_GrBackend, 0));
}

/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaGLRenderer
 * Method:    destroy
 * Signature: ()V
 */
extern "C" JNIEXPORT void JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaGLRenderer_destroy
        (JNIEnv *, jclass) {
    if (gSurface) {
        gSurface.reset();
    }

    if (gGrContext) {
        gGrContext.reset();
    }
}

/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaGLRenderer
 * Method:    setSize
 * Signature: (II)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaGLRenderer_setSize
        (JNIEnv *, jclass, jint width, jint height) {
    // Render to the default framebuffer render target.
    GrBackendRenderTargetDesc desc;
    desc.fWidth = width;
    desc.fHeight = height;
    desc.fConfig = kSkia8888_GrPixelConfig;
    desc.fOrigin = kBottomLeft_GrSurfaceOrigin;
    SkSurfaceProps surfaceProps(SkSurfaceProps::kUseDeviceIndependentFonts_Flag,
                                kUnknown_SkPixelGeometry);
    gSurface.reset(SkSurface::NewFromBackendRenderTarget(gGrContext, desc, &surfaceProps));

    //gSurface2.reset(SkSurface::NewRasterN32Premul(width, height));
    gSurface2.reset(
            SkSurface::NewRenderTarget(gGrContext, SkSurface::kNo_Budgeted, SkImageInfo::MakeN32Premul(width, height)));
    SkCanvas *canvas = gSurface2->getCanvas();
    canvas->clear(SK_ColorWHITE);
    canvas->flush();

    gRendererImpl.SetModelToViewMatrix(width, height);
}

/*
 * Class:     com_artwork_kalakruti_skiadraw_SkiaGLRenderer
 * Method:    draw
 * Signature: (Lcom/artwork/kalakruti/skiadraw/SkiaPath;FFFF)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_artwork_kalakruti_skiadraw_SkiaGLRenderer_draw
        (JNIEnv *env, jclass, jobject skiapath, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkRect rect;
    SkCanvas *canvas = gSurface2->getCanvas();
    SkPaint paint;
    // Adapt the SkPaint for drawing blue lines
    paint.setAntiAlias(true); // Turning off anti-aliasing speeds up the line drawing
    paint.setColor(0xFF0000FF); // This is a solid blue color for our lines
    paint.setStrokeWidth(SkIntToScalar(5)); // This makes the lines have a thickness of 2 pixels
    paint.setStyle(SkPaint::kStroke_Style);

    rect.set((SkScalar) left, (SkScalar) top, (SkScalar) right, (SkScalar) bottom);
    SkPath *path = getSkPath(env, skiapath);
    canvas->save();
    canvas->concat(gRendererImpl.GetModelToViewMatrix());
    canvas->clipRect(rect);
    canvas->clear(SK_ColorWHITE);
    canvas->drawPath(*path, paint);
    canvas->restore();

    // Copy Hardware image to BackBuffer
    canvas = gSurface->getCanvas();
    gSurface2->draw(canvas, 0, 0, NULL);
    canvas->flush();
}
