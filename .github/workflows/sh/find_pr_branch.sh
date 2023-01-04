#!/bin/bash

cmd_dir=`dirname $0`
source ${cmd_dir}/commons.sh

prefix=$1
head_tree=$($shjp $2 -t head_commit.tree_id)

touch ${tmp}hits
git fetch
git branch -a | 
grep -E '^\s*remotes/origin/'${prefix}'/[1-9][0-9]*$' |
while read b; do
    [ "$(git log $b --pretty=%T | head -n 1)" == ${head_tree} ] && echo ${b#remotes/origin/} || :
done >> ${tmp}hits

count=$(cat ${tmp}hits | wc -l)
if [ $count = 0 ]; then
    echo 'It is allowed only feature branch to push to stg branch.' >&2
    end 1
elif [ $count -gt 1 ]; then
    echo 'Some branches prefixed are found, it must be one.' >&2
    cat ${tmp}hits >&2
    end 1
fi

cat ${tmp}hits | head -n 1
end 0
