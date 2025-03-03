
SELECT 
    acc1.TABLE_NAME AS referencing_table, 
    acc1.COLUMN_NAME AS referencing_column, 
    ac2.TABLE_NAME AS referenced_table, 
    acc2.COLUMN_NAME AS referenced_column
FROM 
    ALL_CONSTRAINTS ac1
JOIN 
    ALL_CONS_COLUMNS acc1 ON ac1.CONSTRAINT_NAME = acc1.CONSTRAINT_NAME
JOIN 
    ALL_CONSTRAINTS ac2 ON ac1.R_CONSTRAINT_NAME = ac2.CONSTRAINT_NAME
JOIN 
    ALL_CONS_COLUMNS acc2 ON ac2.CONSTRAINT_NAME = acc2.CONSTRAINT_NAME
WHERE 
    ac2.CONSTRAINT_TYPE = 'P'  -- 'P' stands for primary key
    AND ac1.CONSTRAINT_TYPE = 'R'  -- 'R' stands for referential integrity (foreign key)
    AND ac2.TABLE_NAME = 'YOUR_TABLE_NAME'  -- Replace with your table name containing the primary key
ORDER BY 
    acc1.TABLE_NAME, acc1.COLUMN_NAME;


===============
function validatePositiveNumber(value) {
  // Regular Expression to match positive integers or positive fractional numbers
  let regex = /^[+]?\d*\.?\d+$/;

  if (regex.test(value)) {
    console.log("Valid Positive Number");
    return true;
  } else {
    console.log("Invalid Number");
    return false;
  }
}

===============




---------------------
SELECT
    s.sid,
    s.serial#,
    s.username,
    s.status,
    s.osuser,
    s.machine AS hostname,
    s.program,
    s.logon_time
FROM
    v$session s
WHERE
    s.status = 'ACTIVE'
    AND s.machine = '<hostname>';
-------------------

--------------------
#!/bin/bash

ARGUMENT=$1
JAR_FILE="myjar.jar"

########## Check if the process is already running with the specified argument################
if pgrep -f "java -jar $JAR_FILE $ARGUMENT" > /dev/null; then
    echo "Process with argument '$ARGUMENT' is already running. Exiting..."
    exit 1
else
    echo "No existing process found. Starting new process..."
    java -jar $JAR_FILE $ARGUMENT &
    exit 0
fi

------------------

------------
