package com.lo.export.midmif;

import com.lo.config.Confs;
import com.lo.db.dao.LocationDAO;
import com.lo.db.helper.OraReaderWriterHelper;
import com.lo.db.proxy.GridProxy;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.oracle.OraReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.sql.STRUCT;

/**
 *
 * @author rarif
 */
public class MidMifExport extends AbstractMidMifExport {

    private MidMifMetadata metadata;
    ResultSet rs;
    LocationDAO locationDAO;
    private static GeometryFactory factory;

    public ResultSet getRs() {
        return rs;
    }

    public void setLocationDAO(LocationDAO locationDAO) {
        this.locationDAO = locationDAO;
    }

    public LocationDAO getLocationDAO() {
        return locationDAO;
    }

    public MidMifExport(GridProxy taProxy, LocationDAO locationDAO, String search, String direction, String property, List<GridProxy.Filter> filtersValuesList) throws Exception {
        this.rs = taProxy.getResultSet(null, null, search, direction, property, filtersValuesList);
        factory = new GeometryFactory(new PrecisionModel(), Confs.STATIC_CONFIG.SRID());
        buildMetadata();
    }

    private void buildMetadata() throws Exception {
        Set<String> excludedColumns = Confs.STATIC_CONFIG.tradeareaMidmifExcludedColumnsSet();
        
        try {
            this.metadata = new MidMifMetadata();
            HashMap<String, String> x = new HashMap<String, String>();
            metadata.setCoordsysClause("Earth Projection 1, 0");
            int bytes = 0;
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String mifType = getMifType(rs.getMetaData().getColumnType(i));
                bytes += getMifTypeSize(mifType);
                if (bytes <= 3500) {
                    String name = rs.getMetaData().getColumnName(i);
                    if (excludedColumns != null && excludedColumns.contains(name)) {
                        continue;
                    }
                    if (!metadata.getColumnNames().contains(name)) {
                        metadata.getColumnNames().add(name);
                        metadata.getColumnTypes().add(mifType);
                    }
                } else {
                    System.out.println("something went wrong ....");
                }
            }
        } catch (Exception exception) {
            throw exception;
        }
    }

    private String getMifType(int type) {
        if (type == Types.BIGINT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
            return "Integer";
        } else if (type == Types.FLOAT || type == Types.DOUBLE || type == Types.DECIMAL || type == Types.NUMERIC || type == Types.REAL) {
            return "Float";
        } else if (type == Types.BOOLEAN) {
            return "Logical";
        }
        return String.format("Char(%s)", STRING_LIMIT);
    }

    private int getMifTypeSize(String mifType) {
        switch (mifType) {
            case "Integer":
                return 4;
            case "Float":
                return 4;
            case "Logical":
                return 1;
            default:
                return STRING_LIMIT;
        }
    }

    @Override
    public MidMifRecord nextRecord() {
        metadata.setCoordsysClause("Earth Projection 1, 0");
        OraReader oraReader = new OraReader(factory);
        oraReader.setDimension(OraReaderWriterHelper.ORACLE_GEOM_DIMENSION);

        MidMifRecord record = new MidMifRecord();

        try {
            String name, type;
            if (rs.next()) {
                for (int i = 0, lg = metadata.getColumnNames().size(); i < lg; i++) {

                    name = metadata.getColumnNames().get(i);
                    type = metadata.getColumnTypes().get(i);
                    switch (type) {
                        case "Integer": {
                            record.getDescriptiveValues().add(rs.getInt(name));
                            break;
                        }
                        case "Float": {
                            record.getDescriptiveValues().add(rs.getDouble(name));
                            break;
                        }
                        case "Logical": {
                            record.getDescriptiveValues().add(rs.getBoolean(name));
                            break;
                        }
                        default: {
                            record.getDescriptiveValues().add(rs.getString(name));
                            break;
                        }
                    }
                }
                record.setGeometry(oraReader.read((STRUCT) rs.getObject("GEOM")));
                record.setStyle(rs.getString("STYLE"));
            } else {
                return null;
            }

        } catch (SQLException ex) {
            Logger.getLogger(MidMifExport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return record;
    }

    @Override
    public MidMifMetadata getMetadata() throws Exception {
        return metadata;
    }

}
