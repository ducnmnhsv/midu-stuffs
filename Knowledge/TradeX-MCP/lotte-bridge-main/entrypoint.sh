#!/usr/bin/env bash
echo "***************start running**************"

envDir=/appEnv
globalEnvFileName=app_env.sh

while (( "$#" )); do
  case "$1" in
    -s|--serviceName)
      serviceName="$2"
      shift
      ;;
    -e|--env)
      env="$2"
      shift 2
      ;;
    --) # end argument parsing
      shift
      break
      ;;
    -*|--*=) # unsupported flags
      echo "Error: Unsupported flag $1" >&2
      exit 1
      ;;
    *) # preserve positional arguments
      PARAMS="$PARAMS $1"
      shift
      ;;
  esac
done

set -e
runCommand="$PARAMS"
currentDir=$(pwd)
export TRADEX_WORKING_DIR="$currentDir"

cd ${envDir}
chmod -f +x ${globalEnvFileName}
eval "./${globalEnvFileName}"
echo "execute sript $globalEnvFileName"
. ./${globalEnvFileName}

cd services
if [ -f "$serviceName.sh" ]; then
    chmod -f +x "$serviceName.sh"
    echo "execute sript $serviceName.sh"
    . ./$serviceName.sh
fi

if [ -f "$serviceName-$env.sh" ]; then
    chmod -f +x "$serviceName-$env.sh"
    echo "execute sript $serviceName-$env.sh"
    . ./$serviceName-$env.sh
fi

cd ..

if [ -f "env.sh" ]; then
    chmod -f +x "env.sh"
    echo "execute sript env.sh"
    . ./env.sh
fi


resouceDir=services/deployment-resources/$serviceName-$env
if [ -d $resouceDir ]; then
    if [ -f $resouceDir/index.sh ]; then
        cd $resourceDir
        chmod -f +x "index.sh"
        echo "execute deployment resource script"
        . ./index.sh
        cd ../../..
    fi
fi

cd ${currentDir}

if [ -f "$postEnvScriptName" ]; then
    chmod -f +x "$postEnvScriptName"
    echo "execute sript $postEnvScriptName"
    . ./$postEnvScriptName
fi

export SERVICE_NAME=$serviceName
if [[ "$TRADEX_ENV_ENVIRONMENT" == "" ]]; then
    export TRADEX_ENV_ENVIRONMENT=$env
fi

echo "***********$(date) starting $runCommand ************"
eval $runCommand