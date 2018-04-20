package com.dhval.jpa;

public class TransactionTable {
    String id;
    String txnId;
    String userTrackingId;
    String requestDateTime;
    String queryType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getUserTrackingId() {
        return userTrackingId;
    }

    public void setUserTrackingId(String userTrackingId) {
        this.userTrackingId = userTrackingId;
    }

    public String getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(String requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
}
