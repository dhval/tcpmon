package org.apache.dhval.jpa;

import java.util.Objects;

public class TransactionTable {
    String id;
    String txnId;
    String userTrackingId;
    String requestDateTime;
    String queryType;
    String requestData;
    String responseData;

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

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionTable that = (TransactionTable) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(txnId, that.txnId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, txnId);
    }
}
