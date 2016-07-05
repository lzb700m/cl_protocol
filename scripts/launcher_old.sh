################################launch scripts#####################################
#!/bin/bash

# Name of configuration file, located in the current directory
CONFIG=$1

# Login for remote machine
NETID=$2

# Classpath on remote machine
REMOTECLASSPATH=./workspace/aos_prj1

# Class name containing main program
PROG=AosPrj1

# Count number of lines, and calculate number of nodes
lineCount=$(cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | wc -l)
n=$((lineCount / 2))

# Extract parameters and construct command for each remote machine
cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | (
	# Read first line (containing global parameters, same for each node)
    read global_parameters
	
	# Read next n lines (containing nodeId, hostName and listenPort)
    for(( c=1; c<=n; c++ )) do
    	read line
    	nodeId[$c]=$( echo $line | awk '{ print $1 }')
    	hostId[$c]=$( echo $line | awk '{ print $2 }')
    	portId[$c]=$( echo $line | awk '{ print $3 }')
    done
    
    # Read next n lines (neighboring nodes for each node)
    for(( c=1; c<=n; c++ )) do
    	read line
		neighbors[$c]=$line
    done
    
    # Combine information extracted, construct command
    for(( c=1; c<=n; c++ )) do
    	cmd="ssh $NETID@${hostId[$c]} java -cp $REMOTECLASSPATH $PROG $global_parameters ${nodeId[$c]} ${portId[$c]} ${neighbors[$c]} &"
    	echo $cmd
    done
)