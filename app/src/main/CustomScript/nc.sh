#!/bin/bash


cur=$(date +%s.%N)
prev=$(date +%s.%N)
max=0
min=65535
avg=0
delta=0
count=0
prev_line="START_LINE"
first_packet=1
last_packet=0

nc -k -l 29000 | while read line ; \
do \

	cur=$(date +%s.%N)	

	if [[ first_packet -eq 1 ]]; then
		echo "FIRST"
		first_packet=0
		prev_line=$line 
	else

		delta=$(echo "$cur $prev" | awk '{printf "%f", $1 - $2}')

		if [ "$line" != "$prev_line" ]; then 
			#NEW payload -> new transmission rate, print statistics
			avg=$(echo "$avg $count" | awk '{printf "%f", $1/$2}')
			echo "MIN: $min MAX: $max AVG: $avg CNT: $count" 
			prev_line=$line 
			max=0
			min=65535
			avg=0
			count=0

			if [ "$line" == "XXXENDXXX" ]; then 
				echo "LAST"
				cur=$(date +%s.%N)
				prev=$(date +%s.%N)
				max=0
				min=65535
				avg=0
				delta=0
				count=0
				prev_line="START_LINE"
				first_packet=1
				last_packet=0	
				continue			
			fi

		fi

		if (( $(echo "$delta > $max" |bc -l) )); then
			max=$delta
		fi

		if (( $(echo "$delta < $min" |bc -l) )); then
			min=$delta
		fi		

		avg=$(echo "$avg + $delta" | bc)
		count=$((count+1))
		 
		#echo "${delta} ${line} $count"
	fi
	
	prev=$cur ;

done