#!/bin/bash

cmd_dir=`dirname $0`
source ${cmd_dir}/commons.sh

prefix="$1"
event_path=$2
git_dir=$3
repos=$4
token=$5
head_refs="${git_dir}refs/heads/stg"
dev_head_refs="${git_dir}refs/heads/dev"

last_tree="$(
$shjp "$event_path" -t commits | 
$shjp -t tree_id |
tee ${tmp}target_trees |
tail -n 1)"
[ $? != 0 ] && end 1 || :  
first_tree=$(cat ${tmp}target_trees | head -n 1)

function main(){

  to=''; started='';
  git rebase dev
  [ $? != 0 ] && end 1 || :

  parent="$(git log --pretty="%T %H" | 
  awk '{if(lim=="" || lim>=NR){print $0};if($1=="'$first_tree'"){lim=NR+1};}' |
  tac | tee ${tmp}diff | head -n 1 | cut -d " " -f 2)"
  if [ -z "$parent" ]; then
    echo "The first tree of push commits is not found in the latest target branch." >&2
    end 1
  fi

  cat ${tmp}diff | sed 1d |
  while read tree commit; do

    author=$(git cat-file -p $commit | grep ^author -m 1 | cut -d " " -f 2-)
    git cat-file -p $commit | awk '{if(flag==1){print $0}else if($0==""){flag=1}}' > ${tmp}comments
    if [ -z $started ]; then
      started=$(cat ${tmp}comments | awk '{if(NR==1 && $0 !~ /^('$prefix').*$/){print "1"}}')
      [ -z $started ] && continue || :
    fi

    target_flag=$(cat ${tmp}target_trees | grep ^$tree)
    if [ -n $target_flag ]; then
      cat ${tmp}comments > ${tmp}comments_cp
      cat ${tmp}comments_cp | 
      awk -v prefix="${prefix} " '{if(NR==1 && $0 !~ /^('$prefix').*$/){print prefix $0}else{print}}' > ${tmp}comments 
      if [ "$tree" == "$last_tree" -a "$(cat ${tmp}comments | tail -n 1)" != "[skip ci]" ]; then
        echo "[skip ci]" >> ${tmp}comments 
      fi
    fi

    git commit-tree $tree -p $parent -m "$(cat ${tmp}comments)" > $head_refs
    git reset --hard HEAD
    git commit --amend --author="$author" -C HEAD --allow-empty
    parent=$(cat $head_refs)
    if [ -n $target_flag ]; then 
      to=$parent
      echo "$tree $parent" > ${tmp}target_tc
    fi
  done
  [ $? != 0 ] && end 1 || :
}

function checkDiff(){
  git fetch
  diff -q ${tmp}head_refs_bk ${git_dir}refs/remotes/origin/stg 1>/dev/null && \
  diff -q ${tmp}dev_head_refs_bk ${git_dir}refs/remotes/origin/dev 1>/dev/null
}

git reset --hard HEAD
git checkout dev # set upstream
git checkout stg

i=0
cp $head_refs ${tmp}head_refs_bk
cp $dev_head_refs ${tmp}dev_head_refs_bk
main
while ! checkDiff ; do
  if [ $((++i)) -gt 100 ]; then
    echo "The process repeated more than 100 times, maybe a bug of loop happened..." >&2
    end 1
  fi
  git branch -D dev && git checkout dev || :
  git branch -D stg && git checkout stg || :
  cp $head_refs ${tmp}head_refs_bk
  cp $dev_head_refs ${tmp}dev_head_refs_bk
  main
done
[ $? != 0 ] && end 1 || :

git push origin HEAD -f
[ $? != 0 ] && end 1 || :

git reset --hard $to
git checkout dev
git merge stg
[ $? != 0 ] && end 1 || :
git push origin dev
[ $? != 0 ] && end 1 || :

git log origin/mst --pretty=%T > ${tmp}mst_trees
mst_dup_flag=''; head=''; 
while read tree commit; do
  if cat ${tmp}mst_trees | grep ^$tree 1>/dev/null ; then
    mst_dup_flag=1
    echo $commit" is skipped for merging to mst because the tree is duplicated." >&2
  else
    head=${commit}
    printf "${commit} "
  fi
done < <(cat ${tmp}target_tc) > ${tmp}remains
[ $? != 0 ] && end 1 || :

curl \
  -X POST \
  -H "AUTHORIZATION: token ${token}" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/${repos}/statuses/${head} \
  -d '{"state":"success","context":"ci-passed-mst"}'

if [ -f ${tmp}remains ]; then
  git checkout mst
  if [ -z "$mst_dup_flag" ]; then
    git merge dev
  else
    git cherry-pick "$(cat ${tmp}remains)" 
  fi
  [ $? != 0 ] && end 1 || :
  git push origin mst
  [ $? != 0 ] && end 1 || :
  git push origin release -d 2>/dev/null || :
  git checkout -b release
  git commit --allow-empty -m "Branch from mst."
  git push origin release
fi

end 0
