import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class HybridClusteringBinarizer {

    //private Bitmap image;
    private BufferedImage image;
    private int[] pixels;

    public HybridClusteringBinarizer(BufferedImage image) throws NotFoundException {
        //set up pixels
        this.image = image;
        pixels = new int[image.getWidth()*image.getHeight()];
        image.getRGB(0,0,image.getWidth(),image.getHeight(),pixels,0,image.getWidth());
        BitMatrix bitMatrix = getBitMatrix();
        int index = 0;
        int positiveClusterCount = 0;
        int whitePixel = KMeansBinarizer.makePixel(0,255,255,255);
        for (int h = 0; h< bitMatrix.getHeight(); h++){
            for (int w = 0; w< bitMatrix.getWidth(); w++){
                if (!bitMatrix.get(w,h)){
                    pixels[index] = whitePixel;
                }else {
                    positiveClusterCount++;
                }
                index++;
            }
        }
        KMeansBinarizer kMeansBinarizer = new KMeansBinarizer(pixels, positiveClusterCount);
        pixels = kMeansBinarizer.getPixels();
    }

    private BitMatrix getBitMatrix() throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        Binarizer binarizer = new HybridBinarizer(source);
        return binarizer.getBlackMatrix();
    }

    public BufferedImage getBufferedImage(){
        BufferedImage resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        resultImage.setRGB(0,0,image.getWidth(),image.getHeight(),pixels,0,image.getWidth());
        return resultImage;
    }

}
