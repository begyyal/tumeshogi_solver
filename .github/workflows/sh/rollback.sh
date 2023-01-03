#!/bin/bash

tmp_dir='/tmp/'$(date +%Y%m%d%H%M%S)
mkdir -p $tmp_dir
tmp=${tmp_dir}'/'$$'_'

cmd_dir=`dirname $0`
shjp=${cmd_dir}/shjp

function end(){
  rm -f ${tmp}*
  exit $1
}

event_path=$1
git_dir=$2
repos=$3
dev_before=$4
target=$5
head_refs="${git_dir}refs/heads/$target"

$shjp "$event_path" -t commits | 
$shjp -t tree_id > ${tmp}target_trees
[ $? != 0 ] && end 1 || : 
before=$($shjp "$event_path" -t before)
[ $? != 0 ] && end 1 || : 

function main(){

  remains="$(git log --pretty="%T %H" | 
  awk '{if($2=="'$before'"){flag=1};if(flag!=1){print $0};}' |
  tac |
  while read tree commit; do
    [ -z "$(cat ${tmp}target_trees | grep -o $tree)" ] && printf "${commit} " || :
  done )"
  [ $? != 0 ] && end 1 || :

  git reset --hard $before
  if [ -n "$remains" ] && git cherry-pick "$remains"; then
    echo "Cherry-pick failed, it seems succeeding commits depend on this rollback target." >&2
    end 1
  fi
}

function checkDiff(){
  git fetch
  diff -q ${tmp}head_refs_bk ${git_dir}refs/remotes/origin/$target 1>/dev/null
}

i=0
git checkout $target
cp $head_refs ${tmp}head_refs_bk
main
while ! checkDiff ; do
  if [ $((++i)) -gt 100 ]; then
    echo "The process repeated more than 100 times, maybe a bug of loop happened..." >&2
    end 1
  fi
  git checkout mst
  git branch -D $target
  git checkout $target
  cp $head_refs ${tmp}head_refs_bk
  main
done
[ $? != 0 ] && end 1 || :

git push origin HEAD -f
[ $? != 0 ] && end 1 || :

dev_now=$(git log origin/dev --pretty=%H | head -n1)
if [ $dev_before != $dev_now ]; then
  git checkout dev
  git reset --hard $dev_before
  git push origin dev -f
fi

end 0
