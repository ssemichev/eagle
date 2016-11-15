#!/bin/sh

export es_index=vengine_datatrust_prod
export es_host=localhost
export es_port=9201
source $(dirname $0)/build-index.sh