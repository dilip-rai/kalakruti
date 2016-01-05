package com.artwork.kalakruti.skiadraw;

/**
 * Java wrapper for SkiaPath class.
 */
class SkiaPath {
    private long handle;

    SkiaPath() {
        this.handle = SkiaPath.init();
    }

    private static native long init();

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    native void destroy();

    native void moveTo(final float x, final float y);

    native void lineTo(final float x, final float y);
}
