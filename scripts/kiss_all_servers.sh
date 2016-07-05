################################clean up scripts#####################################
#!/bin/bash

# Login for remote machine
NETID=$1

# Combine information extracted, construct command
for c in `seq -w 1 45`; do
	#echo "ssh $NETID@dc$c.utdallas.edu killall -u $NETID"
    #ssh $NETID@dc$c.utdallas.edu killall -u $NETID
    ssh $NETID@dc$c.utdallas.edu exit
    sleep 1
done
