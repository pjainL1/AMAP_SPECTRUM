package com.korem.invocationThrottling;

import com.spinn3r.log5j.Logger;
import java.util.LinkedList;

/**
 *
 * @author jduchesne
 */
public class Throttler {

    private final LinkedList<AbstractMethod> queue;

    public Throttler() {
        queue = new LinkedList<AbstractMethod>();
    }

    public void invoke(AbstractMethod method) {
        if (register(method) == 1) {
            method.invoke();
            invokeLast();
        } else {
            synchronized (method) {
                wait(method);
            }
        }
    }

    private int register(AbstractMethod method) {
        synchronized (queue) {
            queue.offer(method);
            return queue.size();
        }
    }

    private void invokeLast() {
        synchronized (queue) {
            queue.poll();
            while (queue.size() > 1) {
                AbstractMethod method = queue.poll();
                synchronized (method) {
                    method.notify();
                }
            }
            AbstractMethod method = queue.poll();
            if (method != null) {
                synchronized (method) {
                    method.invoke();
                    method.notify();
                }
            }
        }
    }

    private void wait(Object obj) {
        try {
            obj.wait();
        } catch (InterruptedException ex) {
            Logger.getLogger().error(null, ex);
        }
    }
}
