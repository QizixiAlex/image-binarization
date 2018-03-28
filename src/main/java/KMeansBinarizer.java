import javafx.scene.control.RadioMenuItem;

import java.util.Random;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qizixi on 2018/3/17.
 */

public class KMeansBinarizer {

    //parameters
    private final static int CLUSTER_COUNT = 5;
    private final static int CLUSTER_CHANNELS = 4;
    private final static int CLUSTER_DISTANCE_THRESHOLD = 100;
    private final static int CENTROID_POOL = 500;
    private final static int CENTROID_INDEX = 0;
    private final static int COUNT_INDEX = 1;
    private final static int POSITIVE_INDEX = 2;
    private final static int EMPTY_INDEX = 3;
    private final static int MAX_ROUNDS = 10;
    private final static double SHIFT_THRESHOLD = 0.5;
    private final static double POSITIVE_THRESHOLD = 0.11;
    private final static double RANGE_WEIGHT = 1.5;

    private int whitePixel;
    private int blackPixel;
    private int positivePixelCount;
    private int[] pixels;
    private int[][] clusters;
    private int[] pixelClusterIndex;
    /* android */
//    private Bitmap bitmap;

//    public KMeansBinarizer(Bitmap bitmap){
//        this.bitmap = bitmap;
//        pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
//        bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
//        pixelClusterIndex = new int[pixels.length];
//        initClusters();
//    }

//    public Bitmap getBitmap(){
//        iterate();
//        binarizeImage();
//        Bitmap resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
//        resultBitmap.setPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
//        return resultBitmap;
//    }
    /* PC */
//    public KMeansBinarizer(BufferedImage image){
//        this.image = image;
//        pixels = new int[image.getWidth()*image.getHeight()];
//        image.getRGB(0,0,image.getWidth(),image.getHeight(),pixels,0,image.getWidth());
//        pixelClusterIndex = new int[pixels.length];
//        initClusters();
//    }
//
//    public BufferedImage getBufferedImage(){
//        iterate();
//        binarizeImage();
//        BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
//        bufferedImage.setRGB(0,0,image.getWidth(),image.getHeight(),pixels,0,image.getWidth());
//        //debug
//        System.out.println("-------------final----------------");
//        printClusterInfo();
//        System.out.println("-------------final----------------");
//        return bufferedImage;
//    }

    public KMeansBinarizer(int[] pixels, int positivePixelCount){
        this.positivePixelCount = positivePixelCount;
        blackPixel = makePixel(0,0,0,0);
        whitePixel = makePixel(0,255,255,255);
        this.pixels = pixels;
        pixelClusterIndex = new int[pixels.length];
        for (int i=0;i<pixels.length;i++){
            if (pixels[i]==whitePixel){
                pixelClusterIndex[i] = CLUSTER_COUNT;
            }
        }
        initClusters();
    }

    private void pickCentroidsPlus(){
        Random random = new Random();
        int[] candidates = new int[CENTROID_POOL];
        for (int i=0;i<CENTROID_POOL;i++){
            int candidate = pixels[random.nextInt(pixels.length-1)];
            while (candidate == whitePixel){
                candidate = pixels[random.nextInt(pixels.length-1)];
            }
            candidates[i] = candidate;
        }
        clusters[0][CENTROID_INDEX] = candidates[random.nextInt(CENTROID_POOL-1)];
        double[] distances = new double[CENTROID_POOL];
        for (int i=1;i<CLUSTER_COUNT;i++){
            int nextIndex = 0;
            double maxDistance = 0;
            for (int j=0;j<CENTROID_POOL;j++){
                if (i==1){
                    distances[j] = pixelDistance(clusters[i-1][CENTROID_INDEX], candidates[j]);
                }else{
                    distances[j] = Math.min(pixelDistance(clusters[i-1][CENTROID_INDEX], candidates[j]), distances[j]);
                }
                if (distances[j] > maxDistance){
                    maxDistance = distances[j];
                    nextIndex = j;
                }
            }
            clusters[i][CENTROID_INDEX] = candidates[nextIndex];
        }
    }

    public int[] getPixels() {
        iterate();
        binarizePixels();
        //debug
        System.out.println("-------------results----------------");
        printClusterInfo();
        System.out.println("-------------results----------------");
        return pixels;
    }

    public static int makePixel(int A, int R, int G, int B){
        //alpha channel does not matter in this case
        return  (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    }

    public static double pixelDistance(int pixelX, int pixelY){
        int RX = (pixelX >> 16) & 0xff;
        int GX = (pixelX >>  8) & 0xff;
        int BX = (pixelX      ) & 0xff;
        int RY = (pixelY >> 16) & 0xff;
        int GY = (pixelY >>  8) & 0xff;
        int BY = (pixelY      ) & 0xff;
        double distanceFactor = Math.sqrt((RX-RY)*(RX-RY)+(GX-GY)*(GX-GY)+(BX-BY)*(BX-BY));
        //distance of range between two pixels
        double rangeFactor = Math.abs((Math.max(RX,Math.max(GX,BX))-Math.min(RX,Math.min(GX,BX)))-(Math.max(RY,Math.max(GY,BY))-Math.min(RY,Math.min(GY,BY))));
        return distanceFactor+RANGE_WEIGHT*rangeFactor;
    }

    private int closetClusterIndex(int pixel){
        int result = 0;
        double minDistance = pixelDistance(pixel,clusters[0][CENTROID_INDEX]);
        for (int i=0;i<CLUSTER_COUNT;i++){
            if (clusters[i][EMPTY_INDEX]==0){
                continue;
            }else {
                result = i;
                minDistance = pixelDistance(pixel,clusters[i][CENTROID_INDEX]);
                break;
            }
        }
        for (int i=1;i<CLUSTER_COUNT;i++){
            //empty cluster
            if (clusters[i][EMPTY_INDEX]==0){
                continue;
            }
            double distance = pixelDistance(pixel,clusters[i][CENTROID_INDEX]);
            if (distance < minDistance) {
                minDistance = distance;
                result = i;
            }
        }
        return result;
    }

    private void initClusters(){
        clusters = new int[CLUSTER_COUNT][CLUSTER_CHANNELS];
        pickCentroidsPlus();
        for (int i=0;i<CLUSTER_COUNT;i++){
            //pixel count
            clusters[i][COUNT_INDEX] = 1;
            //if the cluster is positive
            clusters[i][POSITIVE_INDEX] = 0;
            //if the cluster is empty
            clusters[i][EMPTY_INDEX] = 1;
        }
        //debug
        System.out.println("-------------init----------------");
        printClusterInfo();
        System.out.println("-------------init----------------");
    }

    private double updateClusters(){
        double maxShift = 0;
        merge();
        for (int i=0;i<CLUSTER_COUNT;i++){
            //empty cluster
            if (clusters[i][EMPTY_INDEX]==0){
                continue;
            }
            double nextR=0,nextG=0,nextB=0;
            int pixelCount = clusters[i][COUNT_INDEX];
            for (int j=0;j<pixels.length;j++){
                if (pixelClusterIndex[j] == i){
                    nextR += ((pixels[j] >> 16) & 0xff) / (double)pixelCount;
                    nextG += ((pixels[j] >>  8) & 0xff) / (double)pixelCount;
                    nextB += ((pixels[j]      ) & 0xff) / (double)pixelCount;
                }
            }
            int nextCentroid = makePixel(0,(int)nextR,(int)nextG,(int)nextB);
            maxShift = Math.max(maxShift, pixelDistance(nextCentroid, clusters[i][CENTROID_INDEX]));
            clusters[i][CENTROID_INDEX] = nextCentroid;
        }
        return maxShift;
    }

    private void iterate(){
        for (int i=0;i<MAX_ROUNDS;i++){
            for (int j=0;j<CLUSTER_COUNT;j++){
                clusters[j][COUNT_INDEX] = 0;
            }
            for (int j=0;j<pixels.length;j++){
                if (pixels[j]==whitePixel){
                    continue;
                }
                pixelClusterIndex[j] = closetClusterIndex(pixels[j]);
                clusters[pixelClusterIndex[j]][COUNT_INDEX]++;
            }
            double maxShift = updateClusters();
            if (maxShift < SHIFT_THRESHOLD){
                break;
            }
            for (int j=0;j<CLUSTER_COUNT;j++){
                if (clusters[j][COUNT_INDEX] == 0){
                    clusters[j][EMPTY_INDEX] = 0;
                }
            }
            //debug
            System.out.println(String.format("Iteration %d",i));
            printClusterInfo();
        }
    }

    private double darkScore(int pixel){
        return pixelDistance(blackPixel, pixel)+pixelDistance(makePixel(0,200,200,50),pixel);
    }

    private void findDarkestClusters(){
        double totalPixelCount = positivePixelCount;
        double positivePercentage = 0.0;
        while (positivePercentage < POSITIVE_THRESHOLD){
            double darkestScore = darkScore(clusters[0][CENTROID_INDEX]);
            int darkestIndex = 0;
            for (int i=0;i<CLUSTER_COUNT;i++){
                //empty cluster
                if (clusters[i][EMPTY_INDEX]==0 || clusters[i][POSITIVE_INDEX] == 1){
                    continue;
                }else {
                    darkestScore = darkScore(clusters[i][CENTROID_INDEX]);
                    darkestIndex = i;
                    break;
                }
            }
            for (int i=1;i<CLUSTER_COUNT;i++){
                //empty cluster
                if (clusters[i][EMPTY_INDEX]==0){
                    continue;
                }
                //positive cluster
                if (clusters[i][POSITIVE_INDEX] == 1){
                    continue;
                }
                double darkScore = darkScore(clusters[i][CENTROID_INDEX]);
                if (darkScore <= darkestScore){
                    darkestScore = darkScore;
                    darkestIndex = i;
                }
            }
            clusters[darkestIndex][POSITIVE_INDEX] = 1;
            positivePercentage += clusters[darkestIndex][COUNT_INDEX] / totalPixelCount;
        }
    }

    private void binarizePixels(){
        findDarkestClusters();
        for (int i=0;i<pixels.length;i++){
            if (pixels[i] == whitePixel){
                continue;
            }
            if (clusters[pixelClusterIndex[i]][POSITIVE_INDEX] == 1){
                pixels[i] = blackPixel;
            }else {
                pixels[i] = whitePixel;
            }
        }
    }

    private void printClusterInfo(){
        for (int i=0;i<clusters.length;i++){
            //empty cluster
            if (clusters[i][EMPTY_INDEX]==0){
                continue;
            }
            System.out.println(String.format("Cluster %d",i));
            List<Object> args = new ArrayList<>();
            args.add((clusters[i][CENTROID_INDEX]>>16)&0xff);
            args.add((clusters[i][CENTROID_INDEX]>>8)&0xff);
            args.add((clusters[i][CENTROID_INDEX])&0xff);
            System.out.println(String.format("R:%d  G:%d  B:%d",args.toArray()));
            System.out.println(String.format("pixel count:%d",clusters[i][COUNT_INDEX]));
            if (clusters[i][POSITIVE_INDEX]==1){
                System.out.println("positive cluster");
            }
        }
    }

    private void mergeClusters(int index1, int index2){
        //put pixels in clusters[index2] into clusters[index1]
        clusters[index1][COUNT_INDEX] += clusters[index2][COUNT_INDEX];
        clusters[index2][COUNT_INDEX] = 0;
        clusters[index2][EMPTY_INDEX] = 0;
        for (int i=0;i<pixelClusterIndex.length;i++){
            pixelClusterIndex[i] = (pixelClusterIndex[i] == index2?index1:pixelClusterIndex[i]);
        }
    }

    private void merge(){
        for (int i=0;i<CLUSTER_COUNT;i++){
            for (int j=1;i+j<CLUSTER_COUNT;j++){
                if (clusters[i][EMPTY_INDEX] == 0 || clusters[i+j][EMPTY_INDEX] == 0){
                    continue;
                }
                double distance = pixelDistance(clusters[i][CENTROID_INDEX], clusters[i+j][CENTROID_INDEX]);
                if (distance < CLUSTER_DISTANCE_THRESHOLD){
                    mergeClusters(i+j, i);
                }
            }
        }
    }
}
