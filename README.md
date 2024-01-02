DECLARE @proc_definition VARCHAR(MAX)
SELECT @proc_definition = ISNULL(@proc_definition + CHAR(13) + CHAR(10), '') + TEXT
FROM syscomments
WHERE id = OBJECT_ID('YourStoredProcedureName')
ORDER BY colid

PRINT @proc_definition
