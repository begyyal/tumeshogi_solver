#!/bin/bash

consumer_key=$1
consumer_secret=$2
oauth_token=$3
oauth_secret=$4
data_json_file=$5

timestamp=$(date +%s)
nonce=$(openssl rand -base64 -hex 16)
url="https://api.twitter.com/2/tweets"

function parcentEncoding(){
    str=$1
    str=${str//"%"/"%25"}
    str=${str//":"/"%3A"}
    str=${str//"/"/"%2F"}
    str=${str//"?"/"%3F"}
    str=${str//"#"/"%23"}
    str=${str//"["/"%5B"}
    str=${str//"]"/"%5D"}
    str=${str//"@"/"%40"}
    str=${str//"!"/"%21"}
    str=${str//"$"/"%24"}
    str=${str//"&"/"%26"}
    str=${str//"'"/"%27"}
    str=${str//"("/"%28"}
    str=${str//")"/"%29"}
    str=${str//"*"/"%2A"}
    str=${str//"+"/"%2B"}
    str=${str//","/"%2C"}
    str=${str//";"/"%3B"}
    str=${str//"="/"%3D"}
    str=${str//" "/"%20"}
    echo -n $str
}

enc_consumer_key=$(parcentEncoding $consumer_key)
enc_oauth_token=$(parcentEncoding $oauth_token)

param_str="oauth_consumer_key=${enc_consumer_key}&"
param_str="${param_str}oauth_nonce=${nonce}&"
param_str="${param_str}oauth_signature_method=HMAC-SHA1&"
param_str="${param_str}oauth_timestamp=${timestamp}&"
param_str="${param_str}oauth_token=${enc_oauth_token}&"
param_str="${param_str}oauth_version=1.0"

enc_url=$(parcentEncoding $url)
enc_param_str=$(parcentEncoding $param_str)

sig_base_str="POST&"
sig_base_str="${sig_base_str}${enc_url}&"
sig_base_str="${sig_base_str}${enc_param_str}"

enc_consumer_secret=$(parcentEncoding $consumer_secret)
enc_oauth_secret=$(parcentEncoding $oauth_secret)

sig_key="${enc_consumer_secret}&${enc_oauth_secret}"

sig=$(echo -n $sig_base_str | openssl sha1 -binary -hmac $sig_key | base64)
sig=$(parcentEncoding $sig)

auth_header='authorization: OAuth '
auth_header="${auth_header}oauth_consumer_key=\"${consumer_key}\","
auth_header="${auth_header}oauth_nonce=\"${nonce}\","
auth_header="${auth_header}oauth_signature=\"${sig}\","
auth_header="${auth_header}oauth_signature_method=\"HMAC-SHA1\","
auth_header="${auth_header}oauth_timestamp=\"${timestamp}\","
auth_header="${auth_header}oauth_token=\"${oauth_token}\","
auth_header="${auth_header}oauth_version=\"1.0\""

curl -X POST $url \
  -H "$auth_header" \
  -H "content-type: application/json" \
  -d @$data_json_file
