


---------------------

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
