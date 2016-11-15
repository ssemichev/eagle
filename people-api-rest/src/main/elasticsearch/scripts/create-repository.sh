#!/bin/sh

repository=es-blue
es_host=localhost
es_port=9201
protocol="http://"
access_key=${aws_access_key_id}
secret_key=${aws_secret_access_key}

cluster_url=${protocol}${es_host}:${es_port}

echo "Creating ES repository ${repository} ..."

echo

curl -XPUT ${cluster_url}/_snapshot/${repository} -d "{
        \"type\": \"s3\",
        \"settings\": {
            \"bucket\": \"backups.de.targetedvictory.us\",
            \"region\": \"us-east-1\",
            \"base_path\": \"elasticsearch_backups/${repository}\",
            \"access_key\": \"${access_key}\",
            \"secret_key\": \"${secret_key}\"
    }
}"

echo

curl -XGET ${cluster_url}/_snapshot/_all?pretty

echo; echo