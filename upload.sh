################################clean up scripts#####################################
#!/bin/bash

# Login for remote machine
NETID=$1

scp -r src/ pxl141030@dc45:./workspace/cl_protocol/
