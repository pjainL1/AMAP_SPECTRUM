package com.lo.db.dao;

import com.lo.ContextParams;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbutils.QueryRunner;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import com.lo.db.LODataSource;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.SponsorGroup.Sponsor;
import com.lo.util.Encryptor;

/**
 *
 * @author ydumais
 */
public final class AirMilesDAO {

    private static final Logger log = ESAPI.log();
    private static final String PREFIX = "--------";
    private Map<String, QueryRunner> runners;

    public AirMilesDAO() {
        runners = Collections.synchronizedMap(new HashMap<String, QueryRunner>());
    }

    public QueryRunner getRunner(String dataSourceName) {
        QueryRunner runner = runners.get(dataSourceName);
        if (runner == null) {
            runners.put(dataSourceName, runner = new QueryRunner(LODataSource.getDataSource(dataSourceName), true));
        }
        return runner;
    }
    
    public QueryRunner getRunner(ContextParams cp) {
        return getRunner(cp.getSponsor().getRollupGroupCode().toLowerCase());
    }

    public QueryRunner getRunner(SponsorGroup sponsor) {
        return getRunner(sponsor.getRollupGroupCode().toLowerCase());
    }
    
    public QueryRunner getLoneRunner() {
        return getRunner(LODataSource.LONE_DATASOURCE);
    }

    public static String prepareInFragment(int size){
        StringBuilder builder = new StringBuilder(" in (");
        for(int i=0; i<size; i++){
            if (i > 0) {
                builder.append(',');
            }
            builder.append("?");
        }
        builder.append(")");
        return builder.toString();
    }

    public void log(String what, String query, Object[] params) {
        log.debug(ESAPI.log().SECURITY,false,String.format("%s %s: %s [%s]", PREFIX, what, query, Arrays.toString(params)));
    }

    public void log(String what, String query) {
//        String queryStr = ESAPI.encoder().encodeForOS(new WindowsCodec(), query);
        log.debug(ESAPI.log().SECURITY,false,String.format("%s %s: %s", PREFIX, what, query));
    }

    public String encrypt(String password) {
        String result = password;
        try {
            result = new Encryptor(password).encrypt();
        } catch (Exception e) {
            log.debug(ESAPI.log().SECURITY,false,"Using unencrypted password, cause: " + e);
        }
        return result;
    }
}
