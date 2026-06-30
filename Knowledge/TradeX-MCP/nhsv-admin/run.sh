#!/usr/bin/env bash
echo "***************start running**************"
echo "finding which shell is using"
echo "\$0"
echo $0
echo "\$SHELL"
echo $SHELL
echo "ps -p \$\$"
ps -p "$$"
echo "========================================="
isRoot=""
if [ "$(whoami)" == "root" ]; then
    isRoot="1"
fi
#{commonVar}

serviceName="#{serviceName}"
env="#{serverEnvironment}"

CONTAINER_APP_ENV_FILENAME="#{bamboo_APP_ENV_FILE}"
set -e
runCommand="$@"
currentDir=$(pwd)
export TRADEX_WORKING_DIR="$currentDir"

echo "preparing env==========================="
preEnvScriptName="pre_environment.sh"
postEnvScriptName="post_environment.sh"
if [ -f "$preEnvScriptName" ]; then
    if [ "$isRoot" == "1" ]; then
        chmod +x "$preEnvScriptName"
    fi
    echo "execute sript $preEnvScriptName"
    . ./$preEnvScriptName
fi

cd ${CONTAINER_APP_ENV_DIR}
if [ "$isRoot" == "1" ]; then
    chmod +x ${CONTAINER_APP_ENV_FILENAME}
fi
eval "./${CONTAINER_APP_ENV_FILENAME}"
echo "execute sript $CONTAINER_APP_ENV_FILENAME"
. ./${CONTAINER_APP_ENV_FILENAME}

cd services
if [ -f "$serviceName.sh" ]; then
    if [ "$isRoot" == "1" ]; then
        chmod +x "$serviceName.sh"
    fi
    echo "execute sript $serviceName.sh"
    . ./$serviceName.sh
fi

if [ -f "$serviceName-$env.sh" ]; then
    if [ "$isRoot" == "1" ]; then
        chmod +x "$serviceName-$env.sh"
    fi
    echo "execute sript $serviceName-$env.sh"
    . ./$serviceName-$env.sh
fi

if [ -f "$serviceName-$env.sh" ]; then
    if [ "$isRoot" == "1" ]; then
        chmod +x "$serviceName-$env.sh"
    fi
    echo "execute sript $serviceName-$env.sh"
    . ./$serviceName-$env.sh
fi

cd ..

if [ -f "env.sh" ]; then
    if [ "$isRoot" == "1" ]; then
        chmod +x "env.sh"
    fi
    echo "execute sript env.sh"
    . ./env.sh
fi


resouceDir=services/deployment-resources/$serviceName-$env
if [ -d $resouceDir ]; then
    if [ -f $resouceDir/index.sh ]; then
        cd $resourceDir
        if [ "$isRoot" == "1" ]; then
            chmod +x "index.sh"
        fi
        echo "execute deployment resource script"
        . ./index.sh
        cd ../../..
    fi
fi

cd ${currentDir}

if [ -f "$postEnvScriptName" ]; then
    if [ "$isRoot" == "1" ]; then
        chmod +x "$postEnvScriptName"
    fi
    echo "execute sript $postEnvScriptName"
    . ./$postEnvScriptName
fi

export SERVICE_NAME=$serviceName
if [[ "$TRADEX_ENV_ENVIRONMENT" == "" ]]; then
    export TRADEX_ENV_ENVIRONMENT=$env
fi

echo "***********$(date) using those env************"
env
echo "running==="
eval $runCommand