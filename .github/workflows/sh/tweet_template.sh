#!/bin/bash

tag_name=$1
repo_url=$2 # https
repos_name="tumeshogi_solver"
ext="#Java #将棋 #詰将棋" # hashtag etc...

LF=$'\\n'
text="${repos_name} updated to ${tag_name}${LF}${repo_url}"
[ -n "$ext" ] && text=${text}${LF}${ext} || :

echo -n "{\"text\":\"${text}\"}"