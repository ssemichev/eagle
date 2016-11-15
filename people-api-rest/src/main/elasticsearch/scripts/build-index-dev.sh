#!/bin/sh

export es_host=localhost
export es_port=9200
export mapping_path=../../../../people-api-service/src/it/resources/es/mappings/person-datatrust-dev.json
source $(dirname $0)/build-index.sh