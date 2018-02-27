/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.db.dao;

import com.lo.ContextParams;
import java.sql.SQLException;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import com.lo.db.om.PDFReport;
import com.lo.db.om.User;
import com.lo.pdf.PDFBean;
import com.lo.pdf.PDFProcessProgressListenerKey;
import com.lo.pdf.PDFReportHandler;
//import com.spinn3r.log5j.Logger;

/**
 *
 * @author mdube
 */
public class PDFReportDAO {

//    private static final Logger log = Logger.getLogger(PDFReportDAO.class);
    private static final Logger log = ESAPI.getLogger(PDFReportDAO.class);
    private static final String QUERY_UPSERT = "MERGE INTO PDF_REPORTS pr "
            + "USING (SELECT ? user_name, ? pdf_bean, ? process_key FROM dual) a "
            + "ON (pr.user_name = a.user_name) "
            + "WHEN MATCHED THEN "
            + "UPDATE SET pr.pdf_bean = a.pdf_bean, pr.process_key = a.process_key "
            + "WHEN NOT MATCHED THEN "
            + "INSERT (user_name, pr.pdf_bean, pr.process_key) "
            + "VALUES (a.user_name, a.pdf_bean, a.process_key)";
    private static final String QUERY_SELECT =
            "select user_name, pdf_bean, process_key from pdf_reports where user_name = ?";
    private static final String QUERY_DELETE =
            "delete from pdf_reports where user_name = ?";
    private AirMilesDAO dao;

    public PDFReportDAO() {
        this.dao = new AirMilesDAO();
    }

    public PDFReportDAO(AirMilesDAO dao) {
        this.dao = dao;
    }

    /**
     *
     * @param user
     * @param bean
     * @param processing
     * @throws SQLException
     */
    public void saveUpdate(ContextParams cp, PDFBean bean, PDFProcessProgressListenerKey processKey) {
        User user = cp.getUser();
        try {
            Object[] params = new Object[]{
                user.getLogin(),
                PDFBean.serialize(bean),
                PDFProcessProgressListenerKey.serialize(processKey)
            };
            dao.log("saveUpdatePDFReport", QUERY_UPSERT);
            dao.getLoneRunner().update(QUERY_UPSERT, params);
        } catch (SQLException ex) {
//            log.warn("Error update PDFReport for [" + user.getLogin() + "] error [" + ex + "]");
            log.warning(ESAPI.log().SECURITY,false,"Error update PDFReport for [" + user.getLogin() + "] error [" + ex + "]");
        }
    }

    /**
     *
     * @param pr
     * @throws SQLException
     */
    public void saveUpdate(ContextParams cp, PDFReport pr) throws SQLException {
        Object[] params = new Object[]{
            pr.getUserName(),
            PDFBean.serialize(pr.getBean()),
            PDFProcessProgressListenerKey.serialize(pr.getProcessKey())
        };
        dao.log("saveUpdatePDFReport", QUERY_UPSERT);
        dao.getLoneRunner().update(QUERY_UPSERT, params);
    }

    /**
     *
     * @param user
     * @return
     * @throws SQLException
     */
    public PDFReport find(ContextParams cp) {
        try {
            Object[] params = new Object[]{cp.getUser().getLogin()};
            dao.log("getPDFReport", QUERY_SELECT, params);
            return dao.getLoneRunner().query(QUERY_SELECT, new PDFReportHandler(), params);
        } catch (Throwable ex) {
//            log.warn(ESAPI.log().encoder().encodeForOS(new WindowsCodec(), "Error find PDFReport for [" + user.getLogin() + "] error [" + ex + "]"));
            log.warning(ESAPI.log().SECURITY,false, "Error find PDFReport for [" + cp.getUser().getLogin() + "] error [" + ex + "]");
        }
        return null;
    }

    /**
     *
     * @param user
     * @return
     */
    public int delete(ContextParams cp) {
        try {
            Object[] params = new Object[]{cp.getUser().getLogin()};
            dao.log("deletePDFReport", QUERY_DELETE, params);
            return dao.getLoneRunner().update(QUERY_DELETE, params);
        } catch (Throwable ex) {
            log.warning(ESAPI.log().SECURITY,false, "Error delete PDFReport for [" + cp.getUser().getLogin() + "]");
        }
        return 0;
    }
}
