################################clean up scripts#####################################
#!/bin/bash

# Login for remote machine
NETID=$1

scp -r src/ scripts/ config/ pxl141030@dc45:./workspace/lc_protocol/
