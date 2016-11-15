#!/bin/sh

es_index=vengine_datatrust_prod
repository=es-blue
es_host=localhost
es_port=9201
protocol="http://"

cluster_url=${protocol}${es_host}:${es_port}
snapshot_name=`date +%Y%m%d_%H%M%S`
snapshot_url=${cluster_url}/_snapshot/${repository}/${snapshot_name}

echo "Creating snapshot of ES index ${es_index}/${snapshot_name} in ${repository} repository..."

echo

curl -XPUT ${snapshot_url}?wait_for_completion=false -d "{
        \"indices\": \"${es_index}\",
        \"include_global_state\": false,
        \"ignore_unavailable\": false
    }
}"

echo

curl -XGET ${snapshot_url}/?pretty

echo "To check status run - curl -XGET ${snapshot_url}/?pretty"
echo "To check detailed status run - curl -XGET ${snapshot_url}/_status?pretty"

echo; echo