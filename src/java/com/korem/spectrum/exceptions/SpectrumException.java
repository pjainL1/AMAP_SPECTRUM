package com.korem.spectrum.exceptions;

/**
 * Generic exception for Spectrum related errors.
 *
 * @author Korem
 */
public class SpectrumException extends Exception {

    public SpectrumException() {
    }

    public SpectrumException(String message) {
        super(message);
    }

    public SpectrumException(Throwable throwable) {
        super(throwable);
    }

    public SpectrumException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
