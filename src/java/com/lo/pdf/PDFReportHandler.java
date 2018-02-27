/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.pdf;

import com.lo.db.dao.PDFReportDAO;
import com.lo.db.om.PDFReport;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author slajoie
 */
public class PDFReportHandler implements ResultSetHandler<PDFReport> {

    @Override
    public PDFReport handle(ResultSet rs) throws SQLException {
        PDFReport result = null;
        while (rs.next()) {
            result = new PDFReport();
            result.setUserName(rs.getString("user_name"));
            result.setProcessKey(PDFProcessProgressListenerKey.deserialize(rs.getBytes("process_key")));
            result.setBean(PDFBean.deserialize(rs.getBytes("pdf_bean")));
        }
        return result;
    }
}