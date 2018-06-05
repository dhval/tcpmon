SELECT TOP 5 id, txnID, requestDateTime, userTrackingNumber, queryType, LEN(ISNULL(requestData,'')) as request
, CAST(requestData AS XML).query('//*[local-name()="RequestCourtCaseEvent"]//text()[not(local-name()="RequestMetadata")]') as response
 FROM Audit_Log.dbo.TransactionLog
 where queryType like 'RequestCourt%'
 order by requestDateTime desc