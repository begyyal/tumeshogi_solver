#!/bin/bash

tag_name=$1
repo_url=$2
name="tumeshogi_solver"
ext="#将棋 #詰将棋 #Java"
webUrl="https://begyyal.net/tss"
LF=$'\\n'
text="${name}@${tag_name} was published.${LF}webapp: ${webUrl}${LF}repos: ${repo_url}"
[ -n "$ext" ] && text=${text}${LF}${ext} || :
echo -n "{\"text\":\"${text}\"}"
