#!/bin/bash

cur=$(date +%s.%N)
prev=$(date +%s.%N)

nc -k -l 29000 2>&1 | while read line; \
do \
cur=$(date +%s.%N); \
#echo -n "PREV=${prev} CUR=${cur}      " ;\
#echo "$(echo "$cur - $prev" | bc) $line" ; \
echo "$(echo "$cur $prev" | awk '{printf "%f", $1 - $2}') $line" ; \
prev=$cur ;\
done


