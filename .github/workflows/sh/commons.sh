#!/bin/bash

tmp_dir='/tmp/'$(date +%Y%m%d%H%M%S)
mkdir -p $tmp_dir
tmp=${tmp_dir}'/'$$'_'

shjp=${cmd_dir}/shjp

function end(){
  rm -f ${tmp}*
  exit $1
}