#!/bin/bash

tmp_dir='/tmp/act_'$(date +%Y%m%d%H%M%S)
mkdir -p $tmp_dir
tmp=${tmp_dir}'/'$$'_'

function end(){
  rm -f ${tmp}*
  exit $1
}

bf=$1
af=$2

git diff $bf..$af --name-only | 
sort > ${tmp}diff
cat > ${tmp}ig_pattern << EOF
^(.+\/)?README.md$
^(.+\/)?LICENSE$
^(.+\/)?\.gitignore$
^\.github\/
EOF

cat ${tmp}ig_pattern |
while read -r ig; do
  cat ${tmp}diff |
  awk "\$0 ~ /$ig/ {print \$0}" 
done | sort | uniq > ${tmp}ignored

diff ${tmp}diff ${tmp}ignored 2>/dev/null >&2 || echo true
end 0
