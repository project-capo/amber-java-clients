package pl.edu.agh.amber.drivetopoint;

import pl.edu.agh.amber.common.FutureObject;

public class Result<T> extends FutureObject {
    private T result;
    private Location location;

    public void setResult(T result) {
        this.result = result;
    }

    public T getResult() throws Exception {
        if (!isAvailable()) {
            waitAvailable();
        }
        return this.result;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() throws Exception {
        if (!isAvailable()) {
            waitAvailable();
        }
        return this.location;
    }
}
