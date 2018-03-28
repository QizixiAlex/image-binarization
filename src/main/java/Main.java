import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Qizixi on 2017/12/23.
 */
public class Main {

    public static void main(String[] args) {
        String inputDirectory = "src\\main\\resources\\input_images\\";
        String outputDirectory = "src\\main\\resources\\output_images\\";
        String outputDirectoryZxing = "src\\main\\resources\\output_images_zxing\\";
        //List<String> filenames = new ArrayList<>(Arrays.asList("1.png","2.png","3.png","4.png","sample6.jpg","sample7.jpg","sample1.jpg","sample2.jpg","sample3.jpg"));
        //List<String> filenames = new ArrayList<>(Arrays.asList("sample6.jpg"));
        List<String> filenames = new ArrayList<>();
        for (int i=1;i<=26;i++){
            filenames.add(String.format("%d.jpg",i));
        }
        for (int i=0;i<filenames.size();i++){
            String filename = filenames.get(i);
            String inputFilePath = inputDirectory + filename;
            String outputFilePath = outputDirectory + filename;
            String outputFilePathZxing = outputDirectoryZxing + filename;
            File imageFile = new File(inputFilePath);
            try {
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                BitMatrix bitMatrix = binarizeImageZxing(bufferedImage);
                String imageFormat = getExtension(filename);
                Path path = Paths.get(outputFilePathZxing);
                MatrixToImageWriter.writeToPath(bitMatrix,imageFormat,path);
            } catch (IOException | NotFoundException e) {
                e.printStackTrace();
            }
            try {
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                System.out.println(filename);
                BufferedImage outputImage = binarizeImage(bufferedImage);
                String imageFormat = getExtension(filename);
                File outputFile = new File(outputFilePath);
                ImageIO.write(outputImage,imageFormat,outputFile);
            } catch (IOException | NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getExtension(String filename){
         String extension = null;
         int index = filename.lastIndexOf(".");
         if (index>0){
             extension = filename.substring(index+1);
         }
         return extension;
    }

    private static BitMatrix binarizeImageZxing(BufferedImage image) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        //Binarizer binarizer = new AdaptiveBinarizer(source);
        //Binarizer binarizer = new GlobalHistogramBinarizer(source);
        Binarizer binarizer = new HybridBinarizer(source);
        return binarizer.getBlackMatrix();
    }
    private static BufferedImage binarizeImage(BufferedImage image) throws NotFoundException {
        HybridClusteringBinarizer binarizer = new HybridClusteringBinarizer(image);
        return binarizer.getBufferedImage();
    }
}
