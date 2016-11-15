#!/bin/sh

# build Elasticsearch Index from scratch
[[ "" == "$es_index" ]] && es_index=vengine_datatrust_dev
[[ "" == "$es_host" ]] && es_host=localhost
[[ "" == "$es_port" ]] && es_port=9200
[[ "" == "$mapping_path" ]] && mapping_path=mappings/person-datatrust.json
version="_v1"
idx="$es_index$version"
protocol="http://"

echo
cluster_url=${protocol}${es_host}:${es_port}
index_url=${protocol}${es_host}:${es_port}/${idx}
alias_url=${protocol}${es_host}:${es_port}/_aliases

read -p "Will re-create $index_url index (Yy): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo
    is_index_exist=$(curl -XHEAD --write-out %{http_code} --silent --output /dev/null ${index_url})
    if [ ${is_index_exist} -eq 200 ]; then
        echo "Deleting index $idx ..."
        curl -XDELETE ${index_url}; echo
        echo "Deleted"; echo
    fi

    echo "Creating index $idx ..."
    FILE="$( cd -P "$( dirname "$0" )/.." && pwd )/${mapping_path}"

    curl -XPOST ${index_url} -d @${FILE}; echo

    is_index_exist=$(curl -XHEAD --write-out %{http_code} --silent --output /dev/null ${index_url})
    if [ ${is_index_exist} -ne 200 ]; then
        echo; echo "ERROR!!! Cannot create $idx index"; echo
        exit;
    fi
    echo "Created"; echo

    echo "Creating alias $es_index ..."
    curl -XPOST ${alias_url} -d @- << EOF
    {
        "actions" : [
            { "add" : { "index": "${idx}", "alias" : "${es_index}" } }
        ]
    }
EOF
    echo; echo "Created"; echo

    sleep 1
    curl -XPOST ${index_url}/_close
    sleep 1

    echo; echo

    echo "Configuring global cluster settings ..."
    curl -XPUT ${index_url}/_settings -d @- << EOF
    {
        "persistent":
        {
            "threadpool": {
                "bulk": { "queue_size": "500" },
                "search": { "queue_size": "2000" },
                "index": { "queue_size":"1000" }
            }
        }
    }
EOF

    echo; echo

    echo "Setting to recover a cluster ..."
    # (number of master-eligible nodes / 2) + 1
    curl -XPUT ${index_url}/_settings -d '{"persistent":{"discovery.zen.minimum_master_nodes": 6}}'

    echo; echo

    curl -XPOST ${index_url}/_open
    sleep 1

    echo; echo

    echo "Optimizing index for bulk upload ..."
    if [ ${es_index} != "vengine_dev" ]; then
        curl -XPUT ${index_url}/_settings -d '{"index": {"refresh_interval": "-1"}}'
    echo
    fi
    curl -XPUT ${index_url}/_settings -d '{"index": {"number_of_replicas": 0}}'
    echo; echo

    # report out on settings and mappings
    echo "Cluster settings:";
    curl -XGET ${index_url}/_settings;echo;echo

    echo "Cluster health:";
    curl -XGET ${cluster_url}/_cat/health;echo

    echo "DONE"
fi

