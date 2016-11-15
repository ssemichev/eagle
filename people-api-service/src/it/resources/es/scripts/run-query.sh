#!/usr/bin/env bash

if [ '--help' == "$1" ] || [ '-help' == "$1" ]  || [ '-h' == "$1" ]
then
  echo "usage: $0 query-file es_host es_port es_index"
  exit 1
fi

file_name=$1 es_host=$2 es_port=$3 es_index=$4


[[ "" == "$file_name" ]] && file_name=match-all.json
[[ "-" == "$file_name" ]] && file_name=match-all.json
[[ "" == "$es_mapping" ]] && es_mapping=person
[[ "" == "$verb" ]] && verb=GET
[[ "" == "$esAPI" ]] && esAPI=_search
[[ "" == "$es_index" ]] && es_index=vengine_datatrust_dev
[[ "-" == "$es_index" ]] && es_index=vengine_datatrust_dev
[[ "" == "$es_host" ]] && es_host=localhost
[[ "-" == "$es_host" ]] && es_host=localhost
[[ "" == "$es_port" ]] && es_port=9200
[[ "-" == "$es_port" ]] && es_port=9200
[[ "" == "$protocol" ]] && protocol="http://"

query_file=$(dirname $0)/../queries/${file_name}

cluster_url=${protocol}${es_host}:${es_port}

time {
    curl -X${verb} ${cluster_url}/${es_index}/${es_mapping}/${esAPI} --data-binary @${query_file}
}

echo; echo
