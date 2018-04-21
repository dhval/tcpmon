SELECT TOP 25 id, txnID, requestDateTime, userTrackingNumber, queryType
, LEN(ISNULL(requestData,'')) as request, LEN(ISNULL(responseData,'')) as response
 FROM Audit_Log.dbo.TransactionLog
 where userTrackingNumber not like '%healthcheck' and userTrackingNumber not like '%Monitor%' and userTrackingNumber not like 'wm-monitor%'
 order by requestDateTime desc