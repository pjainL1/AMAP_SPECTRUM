/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.util;

import com.lo.ContextParams;
import com.lo.db.om.User;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ydumais
 */
public class SessionUtils {

    public static boolean isValid(HttpSession session){
        boolean valid = false;
        ContextParams cp = ContextParams.get(session);
        if(cp != null){
            User user = cp.getUser();
            if(user != null){
                valid = true;
            }
        }
        return valid;
    }
}
