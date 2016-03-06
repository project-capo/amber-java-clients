package pl.edu.agh.amber.hokuyo;

import pl.edu.agh.amber.common.FutureObject;

import java.util.Iterator;
import java.util.List;

public class Scan extends FutureObject {

    private AngleRange range;

    private int[] distances;

    public Scan() {

    }

    public Scan(int[] distances, AngleRange range) {
        this.distances = distances;
        this.range = range;
        setAvailable();
    }

    public void fillScan(List<Integer> originalDistances, AngleRange range) {
        this.range = range;
        fillDistances(originalDistances);
    }


    private void fillDistances(List<Integer> originalDistances) {
        int from = range.getFromIndex();
        int to = range.getToIndex();

        Iterator<Integer> it = originalDistances.listIterator(from);

        for (int i = 0; i < to - from + 1; i++) {
            distances[i] = it.next();
        }
    }

    public int[] getDistances() throws Exception {
        if (!isAvailable()) {
            waitAvailable();
        }
        return distances;
    }

    public AngleRange getAngleRange() {
        return range;
    }


}
