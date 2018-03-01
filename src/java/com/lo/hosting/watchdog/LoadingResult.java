package com.lo.hosting.watchdog;

import com.lo.hosting.om.Extract;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jphoude
 */
public class LoadingResult {
    
    public static class ExceptionItem {

        private Exception exception;

        private String text;
        private Date date;

        public ExceptionItem(Exception exception, String message) {
            this.exception = exception;
            this.text = message;
            this.date = new Date();
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }
    };

    public static class DataRow {

        public DataRow(Extract extract, long oldCount, long newCount) {
            this.extract = extract;
            this.oldCount = oldCount;
            this.newCount = newCount;
        }
        
        private final Extract extract;
        private long oldCount;
        private long newCount;

        public Extract.Type getType() {
            return extract.getType();
        }

        public Extract getExtract() {
            return extract;
        }

        public long getOldCount() {
            return oldCount;
        }

        public long getNewCount() {
            return newCount;
        }

        public void setOldCount(long oldCount) {
            this.oldCount = oldCount;
        }

        public void setNewCount(long newCount) {
            this.newCount = newCount;
        }

    };

    public LoadingResult() {
        this.exceptions = Collections.synchronizedList(new ArrayList<ExceptionItem>());
        this.dataRows = Collections.synchronizedList(new ArrayList<DataRow>());
        this.start = new Date();
        this.success = true;
    }

    public LoadingResult(boolean success, List<ExceptionItem> exception, List<DataRow> dataRows, Date start, Date finish) {
        this.success = success;
        this.exceptions = exception;
        this.dataRows = dataRows;
        this.start = start;
        this.finish = finish;
    }

    private boolean success;
    private final List<ExceptionItem> exceptions;
    private final List<DataRow> dataRows;
    private Date start;
    private Date finish;

    public boolean isSuccess() {
        return success;
    }

    public List<ExceptionItem> getExceptions() {
        return exceptions;
    }

    public List<DataRow> getDataRows() {
        return dataRows;
    }

    public Date getStart() {
        return start;
    }

    public Date getFinish() {
        return finish;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setFinish(Date finish) {
        this.finish = finish;
    }
    
    public void addException(ExceptionItem ei) {
        this.exceptions.add(ei);
        this.setSuccess(false);
    }
    
    public void addRow(DataRow row) {
        this.dataRows.add(row);
    }
}
