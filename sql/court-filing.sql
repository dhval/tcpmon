SELECT TOP 25 id, txnID, requestDateTime, userTrackingNumber, queryType
, LEN(ISNULL(requestData,'')) as request, LEN(ISNULL(responseData,'')) as response
 FROM Audit_Log.dbo.TransactionLog
 where userTrackingNumber not like '%healthcheck' and userTrackingNumber not like '%Monitor%' and userTrackingNumber not like 'wm-monitor%'
 and (queryType like '%SubmitCourtFiling%' or queryType like '%CourtFilingResponse%'or queryType like '%EFilingResponse%' or queryType like '%ReceiveCourtFilingResponse%')
 order by requestDateTime desc