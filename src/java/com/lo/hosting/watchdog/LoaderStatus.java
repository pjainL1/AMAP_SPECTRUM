
package com.lo.hosting.watchdog;

/**
 *
 * @author jphoude
 */
public class LoaderStatus {
    private boolean success;
    private LoadingResult loadingResult; // loadingResult is the global loading state, whereas LoadingStatus is linked to a particular Loader/Extract instance.
        
    public LoaderStatus(LoadingResult lr) {
        this.loadingResult = lr;
        success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public void addException(LoadingResult.ExceptionItem ei) {
        loadingResult.addException(ei);
        this.setSuccess(false);
    }
}
