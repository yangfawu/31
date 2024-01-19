#!/bin/bash
out=./out.txt
obj=./obj.js
echo -e "<section id=\"force-load-resources\">" > $out
echo -e "{" > $obj
echo -e "\tnode: \$(\"body section#force-load-resources\")" >> $obj
ls | egrep "*.png" | while IFS= read line ; do
	id=$(cut -d '.' -f1 <<< $line)
	rank=$(cut -d '-' -f1 <<< $id)
	suit=$(cut -d '-' -f2 <<< $id)
	echo -e "\t<img class=\"${id}\" src=\"./assets/${line}\" alt=\"${rank} of ${suit}\">" >> $out
	echo -e "\t\"${id}\": \$(\"body section#force-load-resources img.${id}\")," >> $obj
done
echo -e "</section>" >> $out
echo -e "}" >> $obj
