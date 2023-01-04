#!/bin/bash

cmd_dir=`dirname $0`
source ${cmd_dir}/commons.sh

event_path=$1
git_dir=$2
repos=$3
dev_before=$4
target=$5
head_refs="${git_dir}refs/heads/$target"

$shjp "$event_path" -t commits | 
$shjp -t tree_id > ${tmp}target_trees
[ $? != 0 ] && end 1 || : 
first_tree=$(cat ${tmp}target_trees | head -n 1)
tt_str="$(cat ${tmp}target_trees | awk '{a=a$0" "}END{print a}')"

function main(){

  parent=$(git log --pretty="%T %H" | 
  awk '{
    if((lim=="" || lim>=NR) && !match("'"$tt_str"'", $1)){print $0};
    if($1=="'$first_tree'"){lim=NR+1};
  }' | 
  tac | tee ${tmp}diff | head -n 1 | cut -d " " -f 2)
  if [ -z "$parent" ]; then
    echo "The first tree of push commits is not found in the latest target branch." >&2
    end 1
  fi

  tip="$(cat ${tmp}diff | tail -n 1 | cut -d " " -f 2)"
  if [ "$tip" == "$parent" ]; then
    pp="$(git cat-file -p $tip | grep ^parent -m 1 | cut -d " " -f 2)"
    git reset --hard $pp
    tree=$(git cat-file -p $tip | grep ^tree -m 1 | cut -d " " -f 2)
    author=$(git cat-file -p $tip | grep ^author -m 1 | cut -d " " -f 2-)
    git cat-file -p $tip | awk '{if(flag==1){print $0}else if($0==""){flag=1}}' > ${tmp}comments
    if [ "$(cat ${tmp}comments | tail -n 1)" != "[skip ci]" ]; then
      echo "[skip ci]" >> ${tmp}comments 
    fi
    git commit-tree $tree -p $pp -m "$(cat ${tmp}comments)" > $head_refs
    git reset --hard HEAD
    git commit --amend --author="$author" -C HEAD --allow-empty
  else
    cat ${tmp}diff | sed 1d |
    while read tree commit; do
      author=$(git cat-file -p $commit | grep ^author -m 1 | cut -d " " -f 2-)
      git cat-file -p $commit | awk '{if(flag==1){print $0}else if($0==""){flag=1}}' > ${tmp}comments
      if [ "$commit" == "$tip" -a "$(cat ${tmp}comments | tail -n 1)" != "[skip ci]" ]; then
        echo "[skip ci]" >> ${tmp}comments 
      fi
      git commit-tree $tree -p $parent -m "$(cat ${tmp}comments)" > $head_refs
      git reset --hard HEAD
      git commit --amend --author="$author" -C HEAD --allow-empty
      parent=$(cat $head_refs)
    done
  fi
  [ $? != 0 ] && end 1 || :
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
