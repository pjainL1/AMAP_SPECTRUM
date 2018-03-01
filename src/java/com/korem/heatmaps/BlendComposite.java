package com.korem.heatmaps;

import com.spinn3r.log5j.Logger;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class BlendComposite implements Composite {

    private static final int PIXEL_IGNORE = 0x00FFFFFF;
    private static final int PIXEL_EMPTY = 0;

    private IBlendListener listener;
    private int weight;
    private int steps;

    BlendComposite(IBlendListener listener, int steps) {
        this.listener = listener;
        this.steps = steps;
    }

    void setWeight(int weight) {
        this.weight = (weight >= steps) ? steps : weight;
    }

    static boolean isValid(int intColor) {
        return intColor != PIXEL_EMPTY && intColor != (PIXEL_IGNORE | 0xFF000000);
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints) {
        return new BlendingContext(this, listener, weight);
    }

    private static final class BlendingContext implements CompositeContext {
        private Blender blender;
        private IBlendListener listener;

        private BlendingContext(final BlendComposite composite, IBlendListener listener,
                final int weight) {
            this.blender = new Blender() {
                @Override
                public int[] blend(int[] src, int[] dst) {
                    int col = ((src[0] - weight) * dst[0]) >> 8;
                    return new int[] {col, col, col, col};
                }
            };
            this.listener = listener;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            if (src.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
                throw new IllegalStateException(
                        "Source and destination must store pixels as INT.");
            }

            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());

            int[] srcPixel = new int[4];
            int[] dstPixel = new int[4];
            int[] srcPixels = new int[width];
            int[] dstPixels = new int[width];

            double buffer = .29;
            double maxY = height + height * buffer;
            double maxX = width + width * buffer;
            for (int y = (int)(-height * buffer); y < maxY; y++) {
                if (y >= 0 && y < height) {
                    src.getDataElements(0, y, width, 1, srcPixels);
                    dstIn.getDataElements(0, y, width, 1, dstPixels);
                }
                for (int x = (int)(-width * buffer); x < maxX; x++) {
                    if (x >= 0 && x < width) {
                        int pixel = srcPixels[x];

                        if (isValid(pixel)) {
                            int listenerSrcPixel = pixel;
                            toArray(pixel, srcPixel);
                            pixel = dstPixels[x];
                            toArray(pixel, dstPixel);

                            int[] result = blender.blend(srcPixel, dstPixel);
                            if (result[0] != result[1]) {
                                result[0] = result[1];
                            }

                            dstPixels[x] = toInt(result);
                            listener.blended(x, y, listenerSrcPixel, dstPixels[x]);
                        }
                    } else {
                        listener.value(x, y);
                    }
                }
                if (y >= 0 && y < height) {
                    dstOut.setDataElements(0, y, width, 1, dstPixels);
                }
            }
        }
    }

    public static void toArray(int pixel, int[] dest) {
        dest[0] = (pixel >> 16) & 0xFF;
        dest[1] = (pixel >>  8) & 0xFF;
        dest[2] = (pixel      ) & 0xFF;
        dest[3] = (pixel >> 24) & 0xFF;
    }

    public static int toInt(int[] pixel) {
        return (pixel[0] << 16) | (pixel[1] << 8) | pixel[2] | (pixel[3] << 24);
    }

    private static abstract class Blender {
        public abstract int[] blend(int[] src, int[] dst);
    }
}
