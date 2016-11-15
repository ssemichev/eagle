#!/usr/bin/env bash

data_file=$(dirname $0)/../data/datatrust-ds.json
index=vengine_datatrust_dev

curl -XPOST http://localhost:9200/${index}/person/_bulk --data-binary @${data_file}

echo; echo

curl -XPOST http://localhost:9200/${index}/_flush

echo; echo