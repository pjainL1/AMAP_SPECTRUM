package com.lo.db.proxy;

import com.korem.Proxy;
import com.lo.ContextParams;
import com.lo.config.Confs;
import com.lo.util.PreparedStatementLogger;
import com.spinn3r.log5j.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rarif
 */
public abstract class GridProxy extends Proxy {

    private static final String JSON_DATE_FORMAT = "MM/dd/yyyy";
    private static final String SQL_CONDITION_TRUE = "1=1";
    private static final Logger LOGGER = Logger.getLogger();
    
    public static class Filter {
        public enum Comparison {
            LT("<"), GT(">"), EQ("=");
            
            private String sql;
            
            private Comparison(String sql) {
                this.sql = sql;
            }

            public String getSql() {
                return sql;
            }
        }
        public enum FilterType {
            NUMERIC,
            DATE,
            IN,
            STRING;
        }
        
        private FilterType type;
        private Comparison comparison;
        private String value = "";
        private String field = "";

        public Filter(String type, String comparison, String value, String field) {
            this.type = FilterType.valueOf(type.toUpperCase());
            this.field = field;
            this.value = value;
            if (type.equalsIgnoreCase("numeric") || type.equalsIgnoreCase("date")) {
                this.comparison = Comparison.valueOf(comparison.toUpperCase());
            }
        }

        public FilterType getType() {
            return type;
        }

        public Comparison getComparison() {
            return comparison;
        }

        public String getValue() {
            return value;
        }

        public String getField() {
            return field;
        }
    }
    
    public GridProxy() throws SQLException {
        super();
    }
    
    abstract protected String getBaseQuery();
    
    abstract protected Map<String, String> getFieldsMap();
    
    abstract protected Set<String> getSearcheableFields();
    
    abstract protected String getIdColumn();
    
    protected String getSearchFilters(Set<String> searcheableFields, String searchValue) {
        StringBuilder sb = new StringBuilder("(");
        int i = 0;
        for (String field : searcheableFields) {
            if (i++ > 0) {
                sb.append(" OR ");
            }
            sb.append(" LOWER(").append(getFieldsMap().get(field)).append(") LIKE LOWER(?) ");
        }
        sb.append(")");
        
        return sb.toString();
    }
    
    protected String setSearchFilters(Set<String> searcheableFields, String searchValue, String query) {
        String filters = "";
        if (searchValue != null && !searchValue.isEmpty()) {
            filters = getSearchFilters(searcheableFields, searchValue);
        }
        if (!filters.isEmpty()) {
            return String.format(Confs.CONSOLE_QUERIES.queryWithFilters(), query, filters);
        }
        
        return query;
    }
    
    protected String setGridFilters(Map<String, String> fieldNameMapping, List<Filter> filtersValuesList, String query) {
        StringBuilder sb = new StringBuilder(SQL_CONDITION_TRUE);
        if (!filtersValuesList.isEmpty()) {
            for (Filter filter : filtersValuesList) {
                sb.append(buildFiltersSQL(filter, fieldNameMapping));
            }
            
            return String.format(Confs.CONSOLE_QUERIES.queryWithFilters(), query, sb.toString());
        }
        
        return query;
    }
    
    public int count(Integer start, Integer limit, String sortProperty, String direction, String search, List<Filter> filtersValuesList) throws SQLException, ParseException {
        String query = getQuery(null, null, sortProperty, direction, search, filtersValuesList);
        query = String.format(Confs.CONSOLE_QUERIES.queryWithCount(), query);
        PreparedStatement stmt = prepare(query);
        
        setStmtParameters(null, null, sortProperty, direction, search, filtersValuesList, stmt);
        
        try (ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
    
    private String getUniqueSort(String col, String order) {
        String sortSql = col + " " + order;
        
        sortSql += ", " + getIdColumn();
        
        return sortSql;
    }
    
    public String getQuery(Integer start, Integer limit, String sortProperty, String direction, String search, List<Filter> filtersValuesList) throws SQLException {
        String query = getBaseQuery();
        
        query = setSearchFilters(getSearcheableFields(), search, query);
        query = setGridFilters(getFieldsMap(), filtersValuesList, query);
        
        if (!sortProperty.equals("") && !direction.equals("")) {
            //there is sorting 
            String sortColumn = getFieldsMap().get(sortProperty);
            query = String.format(Confs.CONSOLE_QUERIES.queryWithOrderBy(), query, getUniqueSort(sortColumn, direction));
        }
        
        if (start != null && limit != null) {
            query = String.format(Confs.CONSOLE_QUERIES.queryWithPaging(), query);
        }

        // reapply ordering to make sure paging didn't mess with it.
        if (!sortProperty.equals("") && !direction.equals("")) {
            //there is sorting 
            String sortColumn = getFieldsMap().get(sortProperty);
            query = String.format(Confs.CONSOLE_QUERIES.queryWithOrderBy(), query, getUniqueSort(sortColumn, direction));
        }
        
        return query;
    }
    
    protected Object[] setStmtParameters(Integer start, Integer limit, String sortProperty, String direction, String search, List<Filter> filtersValuesList, PreparedStatement ps) throws SQLException, ParseException {
        int i = 1;
        List<Object> params = new ArrayList<>();
        
        if (search != null && !search.isEmpty()) {
            for (String field : getSearcheableFields()) {
                setParam(ps, params, i++, search + "%");
            }
        }

        i = fillFilterValues(ps, filtersValuesList, i, params);
        
        if (start != null && limit != null) {
            setParam(ps, params, i++, limit + 1);
            setParam(ps, params, i++, start);
        }
        
        return params.toArray();
    }

    public ResultSet getResultSet(Integer start, Integer limit, String sortProperty, String direction, String search, List<Filter> filtersValuesList) throws SQLException, ParseException {
        String query = getQuery(start, limit, sortProperty, direction, search, filtersValuesList);

        PreparedStatement ps = prepare(query);
        Object[] params = setStmtParameters(start, limit, sortProperty, direction, search, filtersValuesList, ps);
        
        PreparedStatementLogger.log(LOGGER, query, params);

        return ps.executeQuery();

    }

    public String buildFiltersSQL(Filter filter, Map<String, String> mapConfig) {

        String filterQueryFormatted = "";
        String comparator = "";
        if (filter.getComparison() == Filter.Comparison.EQ) {
            comparator = " LIKE ";
        } else if (filter.getComparison() != null) {
            comparator = filter.getComparison().getSql();
        }

        if (filter.getType() == Filter.FilterType.NUMERIC) { // when the flter is on the numeric column
            if (filter.getComparison() == Filter.Comparison.EQ) {

                filterQueryFormatted = String.format(" AND %s LIKE ?", mapConfig.get(filter.getField()));
            } else {
                filterQueryFormatted = String.format(" AND %s %s ?", (String) mapConfig.get(filter.getField()), comparator);
            }

        } else if (filter.getType() == Filter.FilterType.STRING) { // case when the filter is a string
            filterQueryFormatted = String.format(" AND lower(%s) LIKE lower(?)", (String) mapConfig.get(filter.getField()));

        } else if (filter.getType() == Filter.FilterType.DATE) { // when it is a date column
            if (filter.getComparison() == Filter.Comparison.EQ) {
                filterQueryFormatted = String.format(" AND ( %s >= ? and %s <= ? )", (String) mapConfig.get(filter.getField()), (String) mapConfig.get(filter.getField()));
            }
            if (filter.getComparison() == Filter.Comparison.GT) {
                filterQueryFormatted = String.format(" AND %s >= ?", (String) mapConfig.get(filter.getField()));
            }
            if (filter.getComparison() == Filter.Comparison.LT) {
                filterQueryFormatted = String.format(" AND %s < ?", (String) mapConfig.get(filter.getField()));
            }
        } else if (filter.getType() == Filter.FilterType.IN) {
            filterQueryFormatted = String.format(" AND %s IN (%s)", (String) mapConfig.get(filter.getField()), filter.getValue());
        }
        return filterQueryFormatted;
    }
    
    private void setParam(PreparedStatement ps, List<Object> params, int index, Object object) throws SQLException {
        ps.setObject(index, object);
        params.add(object);
    }

    private int fillFilterValues(PreparedStatement ps, List<Filter> filtersValuesList, int i, List<Object> params) throws SQLException, ParseException {
        for (Filter filter : filtersValuesList) {
            if (filter.getType() == Filter.FilterType.NUMERIC) {
                setParam(ps, params, i++, Integer.parseInt(filter.getValue()));
            }
            if (filter.getType() == Filter.FilterType.STRING) {
                setParam(ps, params, i++, filter.getValue() + "%");
            }
            if (filter.getType() == Filter.FilterType.DATE) {
                if (filter.getComparison() == Filter.Comparison.LT) {
                    SimpleDateFormat formatter = new SimpleDateFormat(JSON_DATE_FORMAT);

                    Date date = formatter.parse(filter.getValue());
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    java.sql.Date sqlDateTo = new java.sql.Date(c.getTime().getTime());
                    setParam(ps, params, i++, sqlDateTo);
                } else {
                    setParam(ps, params, i++, getSqlDate(filter.getValue()));
                }
            }
        }
        return i;
    }

    private java.sql.Date getSqlDate(String dateInString) throws SQLException, ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(JSON_DATE_FORMAT);
        Date date = formatter.parse(dateInString);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        return sqlDate;
    }
}
