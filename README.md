SELECT
    sp.hostname,
    sd.name AS dbname,
    sp.loginame,
    COUNT(*) AS connection_count
FROM master..sysprocesses sp
LEFT JOIN master..sysdatabases sd ON sp.dbid = sd.dbid
GROUP BY sp.hostname, sd.name, sp.loginame
ORDER BY connection_count DESC;
