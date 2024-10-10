#!/bin/bash

# Execute specified command
"$@"

/usr/bin/supervisord -n >> /dev/null 2>&1 &
nohup dockerd >/dev/null 2>&1