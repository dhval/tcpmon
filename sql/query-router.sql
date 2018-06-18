SELECT TOP 25 id, txnID, requestDateTime, userTrackingNumber, queryType
 , LEN(ISNULL(requestData,'')) as request, LEN(ISNULL(responseData,'')) as response
FROM Audit_Log.dbo.TransactionLog
where userTrackingNumber not like '%healthcheck' and userTrackingNumber not like '%Monitor%' and userTrackingNumber not like 'wm-monitor%'
      and (
       queryType like '%lems%' or queryType like '%json%'or queryType like '%clean%' or queryType like '%Query Router%' or queryType like '%qds_ws-proxy%'
      )
 order by requestDateTime desc