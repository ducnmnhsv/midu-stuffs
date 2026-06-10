#!/usr/bin/env bash

shouldReturnError=1

err_report() {
    echo "Error on line $1 - $shouldReturnError"
    if [ "$shouldReturnError" = "1" ]; then
      exit $shouldReturnError
    fi
}

trap 'err_report $LINENO' ERR

dockerHost=""
dockerPort=""
dockerUsername=""
dockerPassword=""
serviceParameters=""
appEnvDir="/home/ubuntu/containers/share"
CONTAINER_APP_ENV_DIR=/appEnv
serviceName=""
additionalServiceParameter=""
version="1.0.0"
buildNumber=""
containerUser=""
networkType=""

RESTART=""
PARAMS=""
TEST=""
DUPLICATE=""
NUMBER_OF_INSTANCE=1
START_FROM_INSTANCE_NO=1
DELAY=""
while (( "$#" )); do
  case "$1" in
    -r|--restart)
      RESTART="1"
      shift
      ;;
    -a|--appEnvDir)
      appEnvDir="$2"
      shift 2
      ;;
    -s|--serviceName)
      serviceName="$2"
      shift
      ;;
    -du|--dockerUsername)
      dockerUsername="$2"
      shift 2
      ;;
    -dh|--dockerHost)
      dockerHost="$2"
      shift 2
      ;;
    -dP|--dockerPort)
      dockerPort="$2"
      shift 2
      ;;
    -dp|--dockerPassword)
      dockerPassword="$2"
      shift 2
      ;;
    -n|--number-of-nodes)
      NUMBER_OF_INSTANCE="$2"
      shift 2
      ;;
    -ns|--start-from-node)
      START_FROM_INSTANCE_NO="$2"
      shift 2
      ;;
    -d|--dulicate)
      DUPLICATE="1"
      shift
      ;;
    -dl|--delay)
      DELAY="$2"
      shift
      ;;
    -p|--params)
      serviceParameters="$2"
      shift 2
      ;;
    -P|--addParams)
      additionalServiceParameter="$2"
      shift 2
      ;;
    --test)
      TEST="1"}
      shift
      ;;
    -v|--version)
      version="$2"
      shift 2
      ;;
    -bn|--buildNumber)
      buildNumber="$2"
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


containersDir=$(dirname $appEnvDir)

if [ "${bamboo_replaceDockerParams}" != "" ]; then
    serviceParameters="${bamboo_replaceDockerParams}"
fi

shouldReturnError=0
ip=$(getip)
localIpFile="~/containers/share/localIp"
if [ -f $localIpFile ]; then
    ip=$(cat $localIpFile)
fi
shouldReturnError=1
if [ "$dockerHost" != "" ]; then
  serviceFilter="${dockerHost}:${dockerPort}/${serviceName}:"
else
  serviceFilter="${serviceName}"
fi
containerIds=$(docker ps -a --format "{{.ID}}:{{.Image}}:{{.Names}}" | grep ${serviceFilter} | cut -d ':' -f 1)
containerIdsCount=$(docker ps -a --format "{{.ID}}:{{.Image}}:{{.Names}}" | grep ${serviceFilter} | wc -l | grep -o -E '[0-9]+')
echo "serviceFilter: $serviceFilter"
echo "containerIds: $containerIds"
echo "containerIdsCount: $containerIdsCount"
echo "There are ${containerIdsCount} containers. Ids are:\n ${containerIds}"

imageNeedToRemove=""
imageNeedToRemoveCount="0"


commonParams="-e DOCKERMASTERIP=\"$ip\" -v ${appEnvDir}:${CONTAINER_APP_ENV_DIR}"
if [ "$TEST" != "" ]; then
    commonParams="$commonParams --entrypoint bash"
    if [[ $commonParams == *" -t"* ]]; then
        commonParams="$commonParams -ti --rm"
    fi
else
    commonParams="$commonParams -d --restart unless-stopped "
fi

if [ "$containerUser" != "" ]; then
    commonParams="$commonParams --user=$containerUser"
fi

if [ "$networkType" != "" ]; then
    commonParams="$commonParams --network=$networkType"
fi


if [[ "$RESTART" == "" && "$dockerHost" != "" ]]; then
  echo "login repository"
  if [[ "$dockerUsername" = "" ]]; then
    echo "docker username is required"
    exit 1
  fi
  
  if [[ "$dockerPassword" = "" ]]; then
    echo "docker password is required"
    exit 1
  fi

  docker login -u "${dockerUsername}" -p "${dockerPassword}" "${dockerHost}:${dockerPort}"
fi

if [ "$dockerHost" != "" ]; then
  if [ "$buildNumber" != "" ]; then
    imageDocker="${dockerHost}:${dockerPort}/${serviceName}:${version}.${buildNumber}"
  else
    imageDocker="${dockerHost}:${dockerPort}/${serviceName}:${version}"
  fi
  cmdPullDocker="docker pull $imageDocker"
  if $cmdPullDocker > /dev/null; then
    echo "pull image: $imageDocker sucess"
  else
    echo "pull image: $imageDocker failed"
  fi
else
  if [ "$buildNumber" != "" ]; then
    imageDocker="${serviceName}:${version}.${buildNumber}"
  else
    imageDocker="${serviceName}:${version}"
  fi
fi

if [ "$RESTART" == "" ]; then
  if [[ "$containerIdsCount" != "0" ]]; then
    echo "stop and destroy current containers $containerIdsCount"
    docker stop ${containerIds}
    docker rm ${containerIds}
  fi
  if [ "$dockerHost" != "" ]; then
    imageNeedToRemove=$(docker images --format "{{.ID}}:{{.Repository}}:{{.Tag}}" | grep ${serviceFilter} | cut -d ':' -f 1)
    imageNeedToRemoveCount=$(docker images --format "{{.ID}}:{{.Repository}}:{{.Tag}}" | grep ${serviceFilter} | cut -d ':' -f 1 | wc -l)
  fi
fi

if [[ "$NUMBER_OF_INSTANCE" == "1" && "$START_FROM_INSTANCE_NO" == "1" ]]; then
  if [ "$additionalServiceParameter" == "" ] && [ "${bamboo_addDockerParams}" != "" ]; then
    additionalServiceParameter="${bamboo_addDockerParams}"
  fi
  additionalServiceParameter="$additionalServiceParameter -v $containersDir/logs/$serviceName:/logs"
  additionalServiceParameter="$additionalServiceParameter -v $containersDir/data/$serviceName:/data"
  if [[ -f "$appEnvDir/services/$serviceName.sh" ]]; then
    additionalServiceParameter="$additionalServiceParameter -v $appEnvDir/services/$serviceName.sh:${CONTAINER_APP_ENV_DIR}/env.sh"
  fi
  dockerRunCommand="docker run --name ${serviceName} ${serviceParameters} ${additionalServiceParameter} $commonParams ${imageDocker}"
  echo "*********"
  echo $dockerRunCommand
  echo "*********"
  eval $dockerRunCommand
else
  orgAddParams="$additionalServiceParameter"
  orgCommonParams="$commonParams"
  for i in $(seq $START_FROM_INSTANCE_NO $((START_FROM_INSTANCE_NO + NUMBER_OF_INSTANCE - 1)))
  do
    additionalServiceParameter="$orgAddParams"
    commonParams="$orgCommonParams"
    if [ "$additionalServiceParameter" == "" ]; then
      if [[ "$bamboo_addDockerParams" != "" ]]; then
        additionalServiceParameter="$bamboo_addDockerParams"
      fi
      paramsVariable="bamboo_addDockerParams${i}"
      params="${!paramsVariable}"
      if [[ "$params" != "" ]]; then
        additionalServiceParameter="$additionalServiceParameter ${params}"
      fi
    fi
    additionalServiceParameter="$additionalServiceParameter -v $appEnvDir/../logs/$serviceName-$i:/logs"
    additionalServiceParameter="$additionalServiceParameter -v $appEnvDir/../data/$serviceName-$i:/data"
    if [[ -f "$appEnvDir/services/$serviceName-$i.sh" ]]; then
      additionalServiceParameter="$additionalServiceParameter -v $appEnvDir/services/$serviceName-$i.sh:${CONTAINER_APP_ENV_DIR}/env.sh"
    fi
    commonParams="$commonParams -e TRADEX_ENV_INSTANCE_ID=\"$i\" "
    dockerRunCommand="docker run --name ${serviceName}-${i} ${serviceParameters} ${additionalServiceParameter} $commonParams ${imageDocker}"
    echo "*********"
    echo $dockerRunCommand
    echo "*********"
    eval $dockerRunCommand
  done
fi
shouldReturnError=0
if [[ "$imageNeedToRemove" != "" ]]; then
  echo "removing image ${imageNeedToRemove}"
  docker rmi ${imageNeedToRemove}
fi

if [ "$RESTART" == "" ]; then
  if [ "$dockerHost" != "" ]; then
    echo "logout"
    docker logout "${dockerHost}:${dockerPort}"
  fi
fi

sleep 1
echo "build information:"
if [[ "$NUMBER_OF_INSTANCE" == "1" && "$START_FROM_INSTANCE_NO" == "1" ]]; then
  curentImage=$(docker ps --format "{{.Image}}={{.Names}}" | grep $serviceName | cut -d "=" -f 1)
else
  curentImage=$(docker ps --format "{{.Image}}={{.Names}}" | grep $serviceName-1 | cut -d "=" -f 1)
fi

echo "current running image $curentImage and expected image $imageDocker"

if [ "$DELAY" != "" ]; then
  sleep $DELAY
fi
exit 0