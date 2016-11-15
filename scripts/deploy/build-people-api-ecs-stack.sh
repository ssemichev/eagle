#!/bin/bash

set -o pipefail

ENVIRONMENT=$1
STACK_NAME=$2

[[ "" == "$ENVIRONMENT" ]] && ENVIRONMENT="development"
[[ "" == "$STACK_NAME" ]] && STACK_NAME="people-api-ecs-dev-stack"

function abort() {
    set -e
    exit 1
}

echo
echo "ENVIRONMENT: ${ENVIRONMENT}"
echo "STACK_NAME: ${STACK_NAME}"

echo
read -p "Will create / update CloudFormation $STACK_NAME (Yy): " -n 1 -r
echo

if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    abort
fi

STACK_TEMPLATE_FILE="scripts/templates/people-api-ecs.json"
STACK_TEMPLATE_PARAMS_FILE="scripts/templates/parameters/${ENVIRONMENT}/people-api-ecs-params.json"

echo "STACK_TEMPLATE_FILE: ${STACK_TEMPLATE_FILE}"
echo "STACK_TEMPLATE_PARAMS_FILE: ${STACK_TEMPLATE_PARAMS_FILE}"

echo "*** Checking if a stack exists with name $STACK_NAME"

STACK_STATUS=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --output text --query 'Stacks[0].StackStatus'  2> /dev/null)
STATUS=$?
COMMAND="create-stack"

if [ ${STATUS} -eq 0 ]; then
  echo "*** Stack with name $STACK_NAME does exist, updating instead of creating"

  COMMAND="update-stack"

  echo "*** Checking if update-stack can be performed on stack $STACK_NAME ..."

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

echo "*** Running $COMMAND on stack $STACK_NAME"

STACK_ID=$(aws cloudformation ${COMMAND} --stack-name ${STACK_NAME} --template-body file://${STACK_TEMPLATE_FILE} \
    --parameters file://${STACK_TEMPLATE_PARAMS_FILE} --output text)

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
MAX_WAIT_TIMES=10
SLEEP_SECONDS=60

echo "*** This may take up to $(( $MAX_WAIT_TIMES * $SLEEP_SECONDS )) seconds..."

while [ ${NEXT_WAIT_TIME} -lt ${MAX_WAIT_TIMES} ]; do
  STATUS=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query 'Stacks[0].StackStatus')
  echo ${STATUS} | grep "ROLLBACK"
  if [ $? -eq 0 ]; then
    RETCODE=1
    echo "*** ERROR - $COMMAND failed"
    echo "*** Waiting for 2 minutes to make sure stack rolled back successfully..."
    sleep 2m

    STATUS=`aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query 'Stacks[0].StackStatus'`
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