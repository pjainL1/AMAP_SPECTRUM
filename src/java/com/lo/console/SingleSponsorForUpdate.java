/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.console;

import com.lo.db.om.SponsorGroup;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author akriaa
 */
public class SingleSponsorForUpdate implements ResultSetHandler<SponsorGroup> {
    private InputStream stream;

    public SingleSponsorForUpdate(InputStream stream) {
       this.stream = stream;
    }

    @Override
    public SponsorGroup handle(ResultSet rs) throws SQLException {
        while (rs.next()) {
             oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getObject(1);
             try (OutputStream outstream = blob.getBinaryOutputStream()) {
                        byte[] chunk = new byte[blob.getChunkSize()];
                        int n = -1;
                        while ((n = stream.read(chunk)) != -1) {
                            outstream.write(chunk, 0, n);
                        }
                        stream.close();
                        outstream.close();
                    }
             catch (Exception e){
                 
             }
        }
        return null;
    }
}
