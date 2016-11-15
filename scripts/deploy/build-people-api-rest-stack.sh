#!/bin/bash

set -o pipefail

ENVIRONMENT=$1

[[ "" == "$ENVIRONMENT" ]] && ENVIRONMENT="development"

function abort() {
    set -e
    exit 1
}

function assignStackParameter () {
    stackVariable=`echo "$OUTPUTS" | jq -r '[.[] | select(.OutputKey=='\""$1"\"').OutputValue][0] // empty'`
    [[ -z "$stackVariable" ]] && {
        printf "\n*** ERROR ***\nCannot assign value to [$2] parameter.\n"
        printf "CloudFormation stack [$ECS_STACK_NAME] does not contain [$1] output parameter.\n\n"
        abort
    }
    export "$2=$stackVariable"
    echo "$2 ($1): ${stackVariable}"
}

function validateStackParameter () {
    eval stackVariable=\$$1
    [[ -z "$stackVariable" ]] && {
        printf "\n*** ERROR ***\nCannot assign value to [$1] parameter.\n"
        printf "$2\n\n"
        abort
    }
}

echo "*** Reading settings for $ENVIRONMENT environment"

export $(aws s3 cp s3://artifacts.targetedvictory.us/configurations/services/people-api-rest/${ENVIRONMENT}/people-api-rest.env - | xargs) > /dev/null

parameters=(
    "SERVICE_STACK_NAME"
    "ECS_STACK_NAME"
    "DESIRED_COUNT_OF_TASKS"
    "MIN_CAPACITY"
    "MAX_CAPACITY"
    "REQUIRED_CPU"
    "REQUIRED_MEMORY"
    "LISTENER_RULE_CONDITIONS"
    "LISTENER_RULE_PRIORITY"
    "AKKA_LOGLEVEL"
    "ES_PEOPLE_INDEX"
    "ES_VOTER_DATA_CLUSTER_NAME"
    "ES_VOTER_DATA_CLUSTER_URI"
    "HTTP_SEARCH_USER_NAME"
    "HTTP_SEARCH_USER_PASSWORD"
)

for parameter in "${parameters[@]}"
do
   :
   validateStackParameter ${parameter} "Check s3://{configuration}/[$ENVIRONMENT/people-api-rest.env] file"
   echo "export ${parameter}=[secure]"
done

echo
echo "ENVIRONMENT: ${ENVIRONMENT}"
echo "SERVICE_STACK_NAME: ${SERVICE_STACK_NAME}"
echo "ECS_STACK_NAME: ${ECS_STACK_NAME}"
echo

STACK_TEMPLATE_FILE="scripts/templates/people-api-rest.json"
STACK_TEMPLATE_PARAMS_TEMPLATE_FILE="scripts/templates/parameters/people-api-rest-params.template"
STACK_TEMPLATE_PARAMS_FILE="scripts/templates/people-api-rest-params.json"

echo "STACK_TEMPLATE_FILE: ${STACK_TEMPLATE_FILE}"
echo "STACK_TEMPLATE_PARAMS_FILE: ${STACK_TEMPLATE_PARAMS_FILE}"
echo

echo "*** Checking if a stack exists with name $SERVICE_STACK_NAME"

STACK_STATUS=$(aws cloudformation describe-stacks --stack-name ${SERVICE_STACK_NAME} --output text --query 'Stacks[0].StackStatus'  2> /dev/null)
STATUS=$?
COMMAND="create-stack"

if [ ${STATUS} -eq 0 ]; then
  echo "*** Stack with name $SERVICE_STACK_NAME does exist, updating instead of creating"

  COMMAND="update-stack"

  echo "*** Checking if update-stack can be performed on stack $SERVICE_STACK_NAME ..."

  echo ${STACK_STATUS} | grep -q "COMPLETE"

  if [ $? -eq 1 ]; then
    echo "*** ERROR ***"
    echo "*** Stack is NOT in an updatable state"
    echo "*** Current stack status is $STACK_STATUS"
    abort
  fi

  echo ${STACK_STATUS} | grep -q "PROGRESS"

  if [ $? -eq 0 ]; then
    echo "*** ERROR ***"
    echo "*** Stack is NOT in an updatable state"
    echo "*** Current stack status is $STACK_STATUS"
    abort
  fi
fi

echo "*** Assigning input parameters for stack $SERVICE_STACK_NAME"

OUTPUTS=$(aws cloudformation describe-stacks --stack-name ${ECS_STACK_NAME} --query 'Stacks[0].Outputs[]')

assignStackParameter "ecsservicename"       "ECS_NAME"
assignStackParameter "ecscluster"           "ECS_CLUSTER_NAME"
assignStackParameter "targetgrouparn"       "TARGET_GROUP_ARN"
assignStackParameter "listenerarn"          "LISTENER_ARN"
assignStackParameter "cloudwatchloggroup"   "CLOUDWATCH_LOG_GROUP"
assignStackParameter "notificationsnstopic" "NOTIFICATION_SNS_TOPIC"

IMAGE_NAME=`cat ./target/docker-image.version`

validateStackParameter "IMAGE_NAME" "The file docker-image.version doesn't exist or empty.\nRun [sbt \"project people-api-rest\" makeDockerVersion] command to generate this file."
export "IMAGE_NAME=$IMAGE_NAME"
echo "IMAGE_NAME: $IMAGE_NAME"

envsubst < ${STACK_TEMPLATE_PARAMS_TEMPLATE_FILE} > ${STACK_TEMPLATE_PARAMS_FILE}

echo "*** Running $COMMAND on stack $SERVICE_STACK_NAME"

STACK_ID=$(aws cloudformation ${COMMAND} --stack-name ${SERVICE_STACK_NAME} --template-body file://${STACK_TEMPLATE_FILE} \
    --parameters file://${STACK_TEMPLATE_PARAMS_FILE} --output text 2>&1)

echo ${STACK_ID}

echo ${STACK_ID} | grep -q "ValidationError"

STATUS=$?

if [ ${STATUS} -eq 0 ]; then
  echo ${STACK_ID} | grep -q "No updates"

  if [ $? -eq 1 ]; then
      echo "*** ERROR - Command failed with code $STATUS"
      abort
  fi

  echo "*** No updates performed, continuing ..."
fi

echo "*** Command completed with return code $STATUS"

NEXT_WAIT_TIME=0
MAX_WAIT_TIMES=15
SLEEP_SECONDS=60

echo "*** This may take up to $(( $MAX_WAIT_TIMES * $SLEEP_SECONDS )) seconds..."

while [ ${NEXT_WAIT_TIME} -lt ${MAX_WAIT_TIMES} ]; do
  STATUS=$(aws cloudformation describe-stacks --stack-name ${SERVICE_STACK_NAME} --query 'Stacks[0].StackStatus')
  echo ${STATUS} | grep "ROLLBACK"
  if [ $? -eq 0 ]; then
    RETCODE=1
    echo "*** ERROR - $COMMAND failed"
    echo "*** Waiting for 2 minutes to make sure stack rolled back successfully..."
    sleep 2m

    STATUS=`aws cloudformation describe-stacks --stack-name ${SERVICE_STACK_NAME} --query 'Stacks[0].StackStatus'`
    echo ${STATUS} | grep "FAILED"
    if [ $? -eq 0 ]; then
      echo "*** CRITICAL ERROR - rollback has failed"
    else
      echo "*** Stack rolled back"
    fi
    if [ ${COMMAND} = "create-stack" ]; then
      echo "*** Stack creation failed. Printing events ..."
      aws cloudformation describe-stack-events --stack-name ${STACK_ID}
      echo "*** Removing unstable stack ..."
      aws cloudformation delete-stack --stack-name ${STACK_ID}
    fi
    abort
  fi

  echo ${STATUS} | grep "COMPLETE"
  if [ $? -eq 0 ]; then
    echo "*** Operation $COMMAND completed successfully"
    exit 0
  else
    echo "Current stack status: $STATUS"
  fi
  (( NEXT_WAIT_TIME++ )) && sleep ${SLEEP_SECONDS}
done

echo "Failed due to timeout"
abort