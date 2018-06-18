package org.apache.dhval.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("db")
public class TransactionLog {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionLog.class);
    public String size = "10";

    @Autowired
    JdbcTemplate jdbcTemplate;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    private RowMapper<TransactionTable> tableRowMapper = new RowMapper<TransactionTable>() {
        public TransactionTable mapRow(ResultSet rs, int row) {
            TransactionTable response = new TransactionTable();
            try {
                response.setId(rs.getString("id"));
                response.setTxnId(rs.getString("txnID"));
                response.setUserTrackingId(rs.getString("userTrackingNumber"));
                response.setQueryType(rs.getString("queryType"));
                response.setRequestDateTime(rs.getString("requestDateTime"));
                response.setRequestData(rs.getString("request"));
                response.setResponseData(rs.getString("response"));
            } catch (SQLException sqe) {
                LOG.warn(sqe.getMessage(), sqe);
            }
            return response;
        };
    };

    @Transactional(readOnly=true)
    public List<TransactionTable> byQueryType(String queryType) {
        String[] queryTypes = queryType.split(",");
        List<TransactionTable> list = new ArrayList<>();
        for(String type:queryTypes) {
            String sql = "SELECT top "+ size + " id, txnID, requestDateTime, userTrackingNumber, queryType, LEN(ISNULL(requestData,'')) as request, LEN(ISNULL(responseData,'')) as response FROM Audit_Log.dbo.TransactionLog" +
                    " where queryType LIKE ? order by requestDateTime desc";
            list.addAll(jdbcTemplate.query(sql, new Object[] {"%" + type + "%"}, tableRowMapper));
        }
        return list;
    }

    @Transactional(readOnly=true)
    public List<TransactionTable> byTrackingId(String trackingId) {
        String sql = "SELECT top "+ size + " id, txnID, requestDateTime, userTrackingNumber, queryType, LEN(ISNULL(requestData,'')) as request, LEN(ISNULL(responseData,'')) as response FROM Audit_Log.dbo.TransactionLog" +
                " where userTrackingNumber LIKE ? order by requestDateTime desc";
        List<TransactionTable> list =  jdbcTemplate.query(sql, new Object[] {"%" + trackingId + "%"}, tableRowMapper);
        return list;
    }

    @Transactional(readOnly=true)
    public List<TransactionTable> byTrackingIdAndQueryType(String trackingId, String queryType) {
        String sql = "SELECT top "+ size + " id, txnID, requestDateTime, userTrackingNumber, queryType, LEN(ISNULL(requestData,'')) as request, LEN(ISNULL(responseData,'')) as response FROM Audit_Log.dbo.TransactionLog" +
                " where userTrackingNumber = ? and queryType = ? order by requestDateTime desc";
        List<TransactionTable> list =  jdbcTemplate.query(sql, new Object[] {trackingId, queryType}, tableRowMapper);
        return list;
    }

    @Transactional(readOnly=true)
    public String queryById1(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT TOP 1 requestData FROM Audit_Log.dbo.TransactionLog where id = ?", new Object[] { id }, String.class);
    }

    @Transactional(readOnly=true)
    public String queryById2(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT TOP 1 responseData FROM Audit_Log.dbo.TransactionLog where id = ?", new Object[] { id }, String.class);
    }

    @Transactional(readOnly=true)
    public List<TransactionTable>  query(String sql) {
        LOG.info("Exec  SQL: " + sql);
        List<TransactionTable> list =  jdbcTemplate.query(sql, tableRowMapper);
        return list;
    }
}
