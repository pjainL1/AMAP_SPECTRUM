package com.korem.invocationThrottling;

/**
 *
 * @author jduchesne
 */
public abstract class AbstractMethod {

    private boolean wasInvoked;
    private Object result;

    protected AbstractMethod() {
        wasInvoked = false;
    }

    void invoke() {
        result = doInvoke();
        wasInvoked = true;
    }

    protected abstract Object doInvoke();

    public Object getResult() {
        return result;
    }

    public boolean wasInvoked() {
        return wasInvoked;
    }
}
