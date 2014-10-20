package pl.edu.agh.amber.dummy;

import pl.edu.agh.amber.common.FutureObject;

public class Status extends FutureObject {
    private String message;
    private boolean enable;

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        try {
            if (!isAvailable()) {
                waitAvailable();
            }
            return "Message: " + this.message + ", enable: " + this.enable;
        } catch (Exception e) {
            return super.toString();
        }
    }
}
