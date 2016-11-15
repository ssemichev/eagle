# eagle

## Scala API Services

### Elasticsearch

To create production index

```sh
./people-api-rest/src/main/elasticsearch/scripts/build-index-prod.sh
```

### Populate local Elasticsearch cluster

To start local Elasticsearch cluster

```sh
<local-es-path>/bin/elasticsearch
<local-kibana-path>/bin/kibana
```

To create and populate dev index

```sh
./people-api-rest/src/main/elasticsearch/scripts/build-index-dev.sh
./people-api-service/src/it/resources/es/scripts/insert-datatrust.sh
./people-api-service/src/it/resources/es/scripts/count.sh
```

To run match all query

```sh
./people-api-service/src/it/resources/es/scripts/run-query.sh
```

To run count query

```sh
./people-api-service/src/it/resources/es/scripts/count.sh
```

To run match-by-criteria query

```sh
./people-api-service/src/it/resources/es/scripts/match-by-criteria.sh
```

### Test the service locally

To start dev server

```sh
sbt
people-api-rest/re-start
```
or
```sh
sbt "project people-api-rest" run
```

To run test requests

```sh
curl -i -w %{time_connect}:%{time_starttransfer}:%{time_total} http://127.0.0.1:9000/v1/health
curl -i http://127.0.0.1:9000/v1/about
curl -i http://127.0.0.1:9000/v1/status
curl -XGET 'http://127.0.0.1:9000/v1/people/datatrust?zip=10453&last_name=Show%20J' --user search-user:search-password
curl -XPOST -i -w %{time_connect}:%{time_starttransfer}:%{time_total} -H "Content-Type: application/json" http://127.0.0.1:9000/v1/people/datatrust/search -d \
    '{"first_name":"Carolyn","last_name":"Show Jn","zip":"10453"}' --user search-user:search-password
```

### Make
```sh
- test (run tests)
- test-it (run IT tests)
- test-ete (run E2E tests)
- test-bench (run benchmark tests)
- test-all (run all tests)
- codacy-coverage (update Codacy code coverage)
- run-l (run locally)
- get-version (get the current version of the project)
- docker-stage (publish docker artifacts to ./people-api-rest/target/docker/ folder)
- docker-publish (publish docker image to docker hub)
- deploy-p (deploy in production)
```

### Deployment

To create / update people-api-ecs stack from your local machine

```sh
./scripts/deploy/build-people-api-ecs-stack.sh <environment> <stack-name>
```

Allowed environments values: [ "development", "test", "staging", "production" ]

Example

```sh
./scripts/deploy/build-people-api-ecs-stack.sh production people-api-ecs-prod-stack
```

To deploy develop branch

```sh
//commit all your changes
git push
```

To deploy master branch

```sh
//commit all your changes
git push
sbt "project people-api-rest" "release with-defaults"
```

or if your need to set the build version manually
```sh
//commit all your changes
git push
sbt "project people-api-rest" release"
```

To manually create / update  people-api-rest stack from your local machine

```sh
sbt "project people-api-rest" docker:publish makeDockerVersion
```

or

```sh
sbt "project people-api-rest" makeDockerVersion
//modify ./target/docker-image.version file to use existing container
./scripts/deploy/build-people-api-rest-stack.sh development
```

### Configuration settings

s3://{configuration}/[$ENVIRONMENT/people-api-rest.env

!!! Do not forget to add validation to
the [build-people-api-rest-stack.sh](https://github.com/targetedvictory/eagle/blob/master/scripts/deploy/build-people-api-rest-stack.sh#L38-L52)
file for the new environmental variables

### Stress tests

Populate missing values in [user.properties](https://github.com/targetedvictory/eagle/blob/master/scripts/jmeter-test-plans/user.properties) file

```sh
./apache-jmeter-3.0/bin/jmeter -n -t ./people-api-v1-testplan.jmx --addprop ./user.properties
```

or with SOCKS proxy

```sh
./apache-jmeter-3.0/bin/jmeter -n -t ./people-api-v1-testplan.jmx --addprop ./user.properties -DsocksProxyHost=localhost -DsocksProxyPort=1234
```

### Show a list of project dependencies that can be updated

```sh
sbt dependencyUpdates
```

### Add test coverage to Codacy locally

```sh
export EAGLE_CODACY_PROJECT_TOKEN=<Project_Token>
```

```sh
export CODACY_PROJECT_TOKEN=$EAGLE_CODACY_PROJECT_TOKEN
sbt clean coverage testAll
sbt coverageReport
sbt coverageAggregate
sbt codacyCoverage
```