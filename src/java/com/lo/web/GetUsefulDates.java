package com.lo.web;

import com.korem.requestHelpers.GenericServlet;
import com.korem.requestHelpers.PlainGenericServlet.INoParams;
import com.lo.ContextParams;
import com.lo.util.InitialDatePickers;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public class GetUsefulDates extends GenericServlet<INoParams> {

    @Override
    protected String getJSON(HttpServletRequest req, INoParams params) throws Exception {
        ContextParams cp = ContextParams.get(req.getSession());
        
        InitialDatePickers datePicker = new InitialDatePickers(cp.getSponsor());
        return datePicker.getInitialDates();
    }
}
