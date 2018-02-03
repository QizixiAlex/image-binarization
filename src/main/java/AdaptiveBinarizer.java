/*
 * Copyright 2018 Zixi Qi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.google.zxing.LuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.HybridBinarizer;

public class AdaptiveBinarizer extends GlobalHistogramBinarizer {

    private final int WHITE = 0;
    private final int BLACK = 255;
    //constant parameters for calculating threshold
    private static final double q = 0.8;
    private static final double p1 = 0.5;
    private static final double p2 = 0.8;
    private static final int stride = 1;

    public AdaptiveBinarizer(LuminanceSource source) {
        super(source);
    }

    @Override
    public BitMatrix getBlackMatrix() throws NotFoundException {
        //rough estimation of foreground by using the average approach by HybridBinarizer
        BitMatrix foregroundBitMatrix = estimateForeground();
        LuminanceSource source = getLuminanceSource();
        //interpolation
        LuminanceMatrix backgroundLuminMatrix = new LuminanceMatrix(source);
        backgroundLuminMatrix.interpolation(stride,foregroundBitMatrix);
        //calculate threshold
        //background - foreground difference
        LuminanceMatrix originLuminMatrix = new LuminanceMatrix(source);
        int foregroundBlackCount = countBits(foregroundBitMatrix);
        int foregroundWhiteCount = foregroundBitMatrix.getHeight()*foregroundBitMatrix.getWidth() - foregroundBlackCount;
        if (foregroundBlackCount == 0 || foregroundWhiteCount == 0){
            return foregroundBitMatrix;
        }
        //distance factor between foreground and background
        int distance = (backgroundLuminMatrix.getSum()-originLuminMatrix.getSum())/foregroundBlackCount;
        //average background luminance factor
        BitMatrix flippedMatrix = flipMatrix(foregroundBitMatrix);
        int avgBackground = backgroundLuminMatrix.multiply(new LuminanceMatrix(toIntArray(flippedMatrix))).getSum()/(new LuminanceMatrix(toIntArray(flippedMatrix))).getSum();
        //calculate and apply threshold
        LuminanceMatrix resultMatrix = backgroundLuminMatrix.minus(originLuminMatrix);
        for (int i=0;i<resultMatrix.getWidth();i++){
            for (int j=0;j<resultMatrix.getHeight();j++){
                if (!foregroundBitMatrix.get(i,j)){
                    resultMatrix.set(i,j,WHITE);
                    continue;
                }
                int value = resultMatrix.get(i,j);
                int threshold = (int) getThreshold(distance,avgBackground,value);
                if (value>=threshold){
                    resultMatrix.set(i,j,BLACK);
                }else {
                    resultMatrix.set(i,j,WHITE);
                }
            }
        }
        return resultMatrix.getBitMatrix();
    }

    private double getThreshold(int distance,int avgBackground, int value){
        return q*distance*(p2 + (1-p2)/(1+Math.exp(((-4)*value)/(1-p1)*avgBackground + 2*(1+p1)/(1-p1))));
    }

    private BitMatrix estimateForeground() throws NotFoundException {
        LuminanceSource source = getLuminanceSource();
        //average threshold by using zxing HybridBinarizer
        HybridBinarizer hybridBinarizer = new HybridBinarizer(source);
        return hybridBinarizer.getBlackMatrix();
    }

    //count the number of valid bits in a BitMatrix
    private int countBits(BitMatrix bitMatrix){
        int sum = 0;
        for (int i=0;i<bitMatrix.getWidth();i++){
            for (int j=0;j<bitMatrix.getHeight();j++){
                if (bitMatrix.get(i,j)){
                    sum++;
                }
            }
        }
        return sum;
    }

    //return copy of a flipped BitMatrix
    private BitMatrix flipMatrix(BitMatrix bitMatrix){
        BitMatrix resultMatrix = bitMatrix.clone();
        for (int i=0;i<resultMatrix.getWidth();i++){
            for (int j=0;j<resultMatrix.getHeight();j++){
                resultMatrix.flip(i,j);
            }
        }
        return resultMatrix;
    }

    private int[][] toIntArray(BitMatrix bitMatrix){
        int[][] result = new int[bitMatrix.getWidth()][bitMatrix.getHeight()];
        for (int i=0;i<bitMatrix.getWidth();i++){
            for (int j=0;j<bitMatrix.getHeight();j++){
                result[i][j] = bitMatrix.get(i,j)?1:0;
            }
        }
        return result;
    }

}
