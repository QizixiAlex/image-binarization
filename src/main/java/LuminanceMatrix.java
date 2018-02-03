import com.google.zxing.LuminanceSource;
import com.google.zxing.common.BitMatrix;


public class LuminanceMatrix {

    private final int height;
    private final int width;
    private int[][] matrix;
    private final int WHITE = 0;
    private final int BLACK = 255;

    public LuminanceMatrix(LuminanceSource source){
        this.height = source.getHeight();
        this.width = source.getWidth();
        initMatrix(source);
    }

    public LuminanceMatrix(BitMatrix bitMatrix){
        this.height = bitMatrix.getHeight();
        this.width = bitMatrix.getWidth();
        initMatrix(bitMatrix);
    }

    public LuminanceMatrix(int[][] matrix){
        this.height = matrix[0].length;
        this.width = matrix.length;
        initMatrix(matrix);
    }

    private void initMatrix(LuminanceSource source){
        matrix = new int[width][height];
        byte[] bytes = source.getMatrix();
        int index = 0;
        for (int j=0;j<height;j++){
            for (int i=0;i<width;i++){
                matrix[i][j] = bytes[index++] & 0xff;
            }
        }
    }

    private void initMatrix(BitMatrix bitMatrix){
        matrix = new int[width][height];
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                matrix[i][j] = bitMatrix.get(i,j)?WHITE:BLACK;
            }
        }
    }

    private void initMatrix(int[][] matrix){
        this.matrix = new int[width][height];
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                this.matrix[i][j] = matrix[i][j];
            }
        }
    }

    private int[][] getMatrixSum(){
        int[][] matrixSum = new int[width][height];
        for (int i=width-1;i>=0;i--){
            for (int j = height-1;j>=0;j--){
                int sum = matrix[i][j];
                int downSum = i < width-1?matrixSum[i+1][j]:0;
                int rightSum = j < height-1?matrixSum[i][j+1]:0;
                int rightDownSum = i < width-1 && j < height-1?matrixSum[i+1][j+1]:0;
                matrixSum[i][j] = sum + rightSum + downSum - rightDownSum;
            }
        }
        return matrixSum;
    }


    public void set(int i,int j,int value){
        if (0 <= value && value <= 255){
            matrix[i][j] = value;
        }
    }

    public int get(int i, int j){
        if (i < 0 || i >= width){
            if (j < 0 || j >= height){
                return -1;
            }
        }
        return matrix[i][j];
    }

    private static int cap(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    private int blockAvg(int i, int j, int stride, int[][] matrixSum){
        if (i < 0 || i >= width){
            if (j < 0 || j >= height){
                return -1;
            }
        }
        //calculate surrounding avg
        int leftUpH = cap(j - stride,0,height-1);
        int leftUpW = cap(i - stride,0,width-1);
        int rightDownH = cap(j+stride,0,height-1);
        int rightDownW = cap(i+stride,0,width-1);
        int count = (leftUpH-rightDownH)*(leftUpW-rightDownW) - 1;
        int sum = matrixSum[leftUpW][leftUpH] + matrixSum[rightDownW][rightDownH] - matrix[i][j]
                - matrixSum[leftUpW][rightDownH] - matrixSum[rightDownW][leftUpH];
        return sum/count;
    }

    public void interpolation(int stride, BitMatrix bitMatrix){
        int[][] matrixSum = getMatrixSum();
        int[][] interpolationMatrix = new int[width][height];
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                if (bitMatrix.get(i,j)){
                    interpolationMatrix[i][j] = blockAvg(i,j,stride,matrixSum);
                }
            }
        }
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                if (interpolationMatrix[i][j] != 0){
                    matrix[i][j] = interpolationMatrix[i][j];
                }
            }
        }
    }

    public int[][] getMatrix(){
        return this.matrix;
    }

    public int getHeight(){
        return this.height;
    }

    public int getWidth(){
        return this.width;
    }

    public int getSum(){
        if (matrix.length>0&&matrix[0].length>0){
            int sum = 0;
            for (int i=0;i<width;i++){
                for (int j=0;j<height;j++){
                    sum += matrix[i][j];
                }
            }
            return sum;
        }
        return 0;
    }

    public LuminanceMatrix multiply(LuminanceMatrix luminanceMatrix){
        assert luminanceMatrix.getHeight() == getHeight();
        assert luminanceMatrix.getWidth() == getWidth();
        LuminanceMatrix newLuminMatrix = new LuminanceMatrix(this.matrix);
        int[][] newMatrix = newLuminMatrix.getMatrix();
        int[][] multiplyMatrix = luminanceMatrix.getMatrix();
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                newMatrix[i][j] *= multiplyMatrix[i][j];
            }
        }
        return newLuminMatrix;
    }

    public LuminanceMatrix minus(LuminanceMatrix luminanceMatrix){
        assert luminanceMatrix.getHeight() == getHeight();
        assert luminanceMatrix.getWidth() == getWidth();
        LuminanceMatrix newLuminMatrix = new LuminanceMatrix(this.matrix);
        int[][] newMatrix = newLuminMatrix.getMatrix();
        int[][] multiplyMatrix = luminanceMatrix.getMatrix();
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                newMatrix[i][j] -= multiplyMatrix[i][j];
            }
        }
        return newLuminMatrix;
    }

    public BitMatrix getBitMatrix(){
        BitMatrix result = new BitMatrix(width,height);
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                if (matrix[i][j] == BLACK){
                    result.set(i,j);
                }
            }
        }
        return result;
    }
}
