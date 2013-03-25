package pl.edu.agh.amber.common;

public class FutureObject {

	protected boolean available = false;
	
	protected Exception exception = null;
	
	public boolean isAvailable() throws Exception {
		if (exception != null) {
			throw exception;
		}
		return available;
	}
	
	public synchronized void waitAvailable() throws Exception {
		try {
			while (!isAvailable()) {
				wait();
			}
		} catch (Exception e) {
			throw e;
		}
		
		return;
    }
	
	public synchronized void setAvailable() {
		available = true;
		notifyAll();
	}

	public synchronized void setException(Exception e) {
		available = true;
		exception = e;
		notifyAll();
	}
	
}
