import java.util.Comparator;

public class PixelComparator implements Comparator<Integer>{
    @Override
    public int compare(Integer pixel1, Integer pixel2) {
        int p1 = pixel1;
        int p2 = pixel2;
        int zeros = KMeansBinarizer.makePixel(0,0,0,0);
        if (KMeansBinarizer.pixelDistance(p1,zeros) < KMeansBinarizer.pixelDistance(p2,zeros)){
            return 1;
        }else {
            return 0;
        }
    }
}
