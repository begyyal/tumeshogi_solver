#!/bin/bash

version=$(cat ./.version)
ghtoken=$(cat ./.ghtoken)
curl -L \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $ghtoken" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  https://api.github.com/repos/begyyal/xj_formatter/actions/workflows/publish.yml/dispatches \
  -d '{"ref":"main", "inputs":{"version":"'$version'"}}'
