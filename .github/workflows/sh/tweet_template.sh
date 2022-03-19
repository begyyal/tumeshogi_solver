#!/bin/bash

tag_name=$1
repo_url=$2

LF=$'\\n'
text="tumeshogi_solver updated to ${tag_name}${LF}${repo_url}${LF}#Java #将棋 #詰将棋"

echo -n "{\"text\":\"${text}\"}"