package com.lo.hosting.watchdog;

import com.korem.SessionlessLanguageManager;
import com.lo.config.Confs;
import com.lo.hosting.Mail;
import com.lo.hosting.om.Extract;
import com.lo.hosting.watchdog.LoadingResult.DataRow;
import com.lo.hosting.watchdog.LoadingResult.ExceptionItem;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author maitounejjar
 */
class LoadingNotification {
    private static final int EXCEPTION_DESCRIPTION_MAX_LENGTH = 255;
    public static final String LANGUAGE = "en";
    public static final Locale LOCALE = Locale.CANADA;
    private static final String PERCENTAGE = "%";
    private static final String NA = "N/A";
    
    public SessionlessLanguageManager lm;

    public static class FormattedDate {

        public String time;
        public String month;
        public String day;
        public String year;

        public FormattedDate() {
            Calendar cal = Calendar.getInstance();
            String yy = Integer.toString(cal.get(Calendar.YEAR));
            String dd = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String mm = new SimpleDateFormat("MMMM", LOCALE).format(cal.getTime());
            String tt = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());

            this.time = tt;
            this.month = mm;
            this.day = dd;
            this.year = yy;
        }

    };

    public LoadingNotification() {
        this.lm = new SessionlessLanguageManager(LANGUAGE);
    }

    public boolean sendStartEmail(List<Extract> sequenced, Collection<Extract> parallel) {
        /*
         This method will not send an email in case all expected files were missing
         */
        
        List<Extract> allExtracts = new ArrayList<>(sequenced);
        allExtracts.addAll(parallel);

        // holds Types of the missing and empty files, respectively
        List<String> missingFiles = getMissingFiles(allExtracts);
        List<String> emptyFiles = getEmptyFiles(allExtracts);

        // Only proceed if no expected file is missing
        boolean isEmailSent = false;
        if (missingFiles.size() != Extract.Type.values().length) {
            isEmailSent = true;
            String subject = lm.get("emailLoadingAlert.subjectLoadingStarted");
            String body = createStartEmailBody(missingFiles, emptyFiles);

            String from = Confs.CONFIG.loadingAlertFromEmail();
            String to = Confs.CONFIG.loadingAlertToEmail();
            Mail mail = new Mail(from, to, subject, body);
            mail.send();
        }

        return isEmailSent;
    }

    public void sendFinishEmail(LoadingResult loadingResult) {

        StringBuilder sb = new StringBuilder();

        String subject = loadingResult.isSuccess() ?
                lm.get("emailLoadingAlert.subjectLoadingTerminatedSuccess") :
                lm.get("emailLoadingAlert.subjectLoadingTerminatedError");
        
        Date startDate = loadingResult.getStart();
        Date finishDate = loadingResult.getFinish();
        String dateDiff = getDateDiff(startDate, finishDate);

        
        FormattedDate date = new FormattedDate();
        sb.append("<p>");
        sb.append(String.format(lm.get("emailLoadingAlert.bodyLoadingTerminatedSuccessPart1"), dateDiff, date.time, date.month, date.day, date.year));
        sb.append("</p>");
            
        if (!loadingResult.getDataRows().isEmpty()) {
            sb.append(createDataTable(loadingResult.getDataRows()));
        }
        
        sb.append(loadingResult.isSuccess() ? lm.get("emailLoadingAlert.noErrorsEncountered") : lm.get("emailLoadingAlert.errorsEncountered"));
            
        if (!loadingResult.getExceptions().isEmpty()) {
            sb.append(createErrorTable(loadingResult.getExceptions()));
        }

        String body = sb.toString();

        // Send it!
        String from = Confs.CONFIG.loadingAlertFromEmail();
        String to = Confs.CONFIG.loadingAlertToEmail();
        Mail mail = new Mail(from, to, subject, body);
        mail.send();
    }

    public List<String> getMissingFiles(List<Extract> sequenced) {
        List<String> missingFiles = new ArrayList<>();

        // loop through all possible file types
        for (Extract.Type extractType : Extract.Type.values()) {
            boolean fileFound = false;
            // loop through files that are currently in the sequence
            for (Extract file : sequenced) {
                if (file.getType() == extractType) {
                    // file type is found
                    fileFound = true;
                    break;
                }
            }

            if (!fileFound) {
                missingFiles.add(extractType.toString());
            }

        }
        return missingFiles;
    }

    public List<String> getEmptyFiles(List<Extract> sequenced) {

        boolean fileEmpty = false;
        List<String> emptyFiles = new ArrayList<>();

        // loop through all possible file types
        for (Extract.Type extractType : Extract.Type.values()) {

            // loop through files that are currently in the sequence
            for (Extract file : sequenced) {
                if (file.getType() == extractType) {
                    if (file.getFile().length() == 0) {
                        // file is empty
                        fileEmpty = true;
                    }
                    break;
                }
            }
            if (fileEmpty) {
                emptyFiles.add(extractType.toString());
            }
        }

        return emptyFiles;

    }

    public String createStartEmailBody(List<String> missingFiles, List<String> emptyFiles) {
        /*
         Generates the body for the LOADING STARTED notification
         */

        FormattedDate date = new FormattedDate();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(lm.get("emailLoadingAlert.bodyLoadingStarted"), date.time, date.month, date.day, date.year));

        sb.append("<p>");
        // add missing files, if any
        if (!missingFiles.isEmpty()) {
            String missingFilesTitle = lm.get("emailLoadingAlert.filesMissing");
            sb.append(missingFilesTitle).append(": ").append(missingFiles.get(0));
        }
        for (int i = 1; i < missingFiles.size(); i++) {
            sb.append(", ").append(missingFiles.get(i));
        }
        sb.append("</p><p>");
        // add empty files, if any
        if (!emptyFiles.isEmpty()) {
            String emptyFilesTitle = lm.get("emailLoadingAlert.filesEmpty");
            sb.append(emptyFilesTitle).append(": ").append(emptyFiles.get(0));
        }

        for (int i = 1; i < emptyFiles.size(); i++) {
            sb.append(", ").append(emptyFiles.get(i));
        }
        sb.append("</p>");

        String body = sb.toString();

        return body;

    }

    public String createErrorTable(List<ExceptionItem> list) {

        StringBuilder sb = new StringBuilder();

        sb.append("<p><table border='1'>");
        for (int i = 0; i < list.size(); i++) {
            ExceptionItem ex = list.get(i);
            sb.append("<tr valign='top'>");
            sb.append("<td>").append("[").append(ex.getDate()).append("]").append("</td>");
            sb.append("<td>").append(ex.getText()).append("</td>");
            sb.append("<td>").append(getExceptionMessage(ex.getException()))
                    .append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table></p>");

        String err = sb.toString();
        return err;
    }
    private String getExceptionMessage(Exception ex) {
        if (ex != null) {
            return StringUtils.abbreviate(ex.toString(), EXCEPTION_DESCRIPTION_MAX_LENGTH);
        }
        
        return "";
    }

    public String createDataTable(List<DataRow> dataRows) {

        SessionlessLanguageManager lm = new SessionlessLanguageManager(LANGUAGE);

        String information = lm.get("emailLoadingAlert.bodyTableInformation");
        String previously = lm.get("emailLoadingAlert.bodyTablePreviously");
        String newValue = lm.get("emailLoadingAlert.bodyTableNewValue");
        String change = lm.get("emailLoadingAlert.bodyTableChange");

        String[] headers = {information, previously, newValue, change, PERCENTAGE};

        StringBuilder sb = new StringBuilder();

        sb.append("<p><table border='1'>");
        sb.append("<tr>");
        for (String header : headers) {
            sb.append("<th>");
            sb.append(header);
            sb.append("</th>");
        }
        sb.append("</tr>");
        for (DataRow row : dataRows) {
            boolean isNA = false;

            sb.append("<tr valign='top'>");
            sb.append("<td>").append(row.getExtract().getFile().getName()).append("</td>");
            if (row.getOldCount() == -1) {
                sb.append("<td>").append(NA).append("</td>");
                isNA = true;
            } else {
                sb.append("<td>").append(row.getOldCount()).append("</td>");
            }

            if (row.getNewCount() == -1) {
                sb.append("<td>").append(NA).append("</td>");
                isNA = true;
            } else {
                sb.append("<td>").append(row.getNewCount()).append("</td>");
            }

            if (!isNA) {
                sb.append("<td>").append(getChange(row)).append("</td>");
                sb.append("<td>").append(getChangePercentage(row)).append("</td>");
            } else {
                sb.append("<td>").append(NA).append("</td>");
                sb.append("<td>").append(NA).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></p></p>");
        String body = sb.toString();
        return body;

    }
    
    private String getChange(DataRow row) {
        
        long change = row.getNewCount() - row.getOldCount();
        
        String changeDiff;
        if (row.getOldCount() < row.getNewCount()) {
            changeDiff = "+" + change;
        } else {
            changeDiff = "" + change;
        }
        return changeDiff;
    }
    
    private String getChangePercentage(DataRow row) {
        if (row.getOldCount() == 0) {
            return NA;
        }
        
        long change = row.getNewCount() - row.getOldCount();
        double percentage = ((float)change / row.getOldCount()) * 100;
        percentage = (float)Math.round(percentage * 100) / 100;
        DecimalFormat decimalFormat = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(LOCALE));
        return decimalFormat.format(percentage) + PERCENTAGE;
    }

    public String getDateDiff(Date dateOne, Date dateTwo) {
        /*
         IN  : 2 dates
         OUT : String representing the time difference in hours-minutes-seconds between the 2 dates
         */
        
        long timeDiff = Math.abs(dateOne.getTime() - dateTwo.getTime()) / 1000;
        
        int days = (int)TimeUnit.SECONDS.toDays(timeDiff);        
        long hours = TimeUnit.SECONDS.toHours(timeDiff) - (days *24);
        long minutes = TimeUnit.SECONDS.toMinutes(timeDiff) - (TimeUnit.SECONDS.toHours(timeDiff)* 60);
        long seconds = TimeUnit.SECONDS.toSeconds(timeDiff) - (TimeUnit.SECONDS.toMinutes(timeDiff) *60);
        
        String diff = "";
        if (days > 0) {
            diff += String.format("%d day(s) ", days);
        }
        diff += String.format("%d hour(s) %d min(s) %d second(s)",
                hours,
                minutes,
                seconds
        );
        return diff;
    }

}
