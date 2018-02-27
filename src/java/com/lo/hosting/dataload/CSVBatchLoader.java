/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hosting.dataload;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import au.com.bytecode.opencsv.CSVReader;

import com.lo.db.dao.AirMilesDAO;
import com.lo.hosting.om.Extract;
import com.spinn3r.log5j.Logger;

/**
 * 
 * @author YDumais
 */
public class CSVBatchLoader {

    private static final Logger log = Logger.getLogger();
    public static final int NUM_LINE = 10000;
    private Extract extract;
    private final String query;
    private AirMilesDAO dao = new AirMilesDAO();
    private String databaseName;

    public CSVBatchLoader(Extract extract, String query, String databaseName) {
        this.extract = extract;
        this.query = query;
        this.databaseName = databaseName;
    }

    public void go() throws SQLException {
        CSVReader reader = null;
		InputStreamReader in = null;
		BufferedReader b = null;
		GZIPInputStream is = null;
        int overall = 0;
        try {
			is = new GZIPInputStream(new FileInputStream(extract.getFile()));
			in = new InputStreamReader(is);
			b = new BufferedReader(in);
			reader = new CSVReader(b);
            String[] line;
            List<Object[]> batch = new ArrayList<Object[]>();
            log.debug("batch update with query: " + query);
            while ((line = reader.readNext()) != null) {
                overall++;
                Object[] args = extract.interpret(line);
                batch.add(args);
                if (batch.size() >= NUM_LINE) {
                    log.debug("insert count: " + overall);
                    batchInsert(batch);
                    batch.clear();
                }
            }
            if (batch.size() > 0) {
                batchInsert(batch);
            }
        } catch (Exception e) {
			log.info(String.format("Inserted %s row from extract %s.", overall,
					extract));
            throw new SQLException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
				if (in != null) {
					in.close();
				}
				if (is != null) {
					is.close();
				}
				if (b != null) {
					b.close();
				}
            } catch (IOException ex) {
                log.error(null, ex);
            }
        }
    }

	// private Reader prepareReader(File file) throws FileNotFoundException,
	// IOException {
	// GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
	// return new BufferedReader(new InputStreamReader(is));
	// }

    private void batchInsert(List<Object[]> batch) throws SQLException {
        dao.getRunner(databaseName).batch(query, batch.toArray(new Object[0][]));
    }
}
