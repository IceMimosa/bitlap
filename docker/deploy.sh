#!/usr/bin/env bash

tag=$1

origin=`pwd`
cd $(dirname $0)/../

mkdir -p dist/
find dist/ -name 'bitlap*.tar.gz' | xargs rm -f
find docker/ -name 'bitlap*.tar.gz' | xargs rm -f


# make tar
TAR_FILE="bitlap-server/target/bitlap*.tar.gz"
cmd="./mvnw clean package -DskipTests -Passembly -Pwebapp -Drevision=${tag} -am -pl bitlap-server"
echo "========================================================================================================================================"
echo "==================  🔥 package start: ${cmd}  ============================"
echo "========================================================================================================================================"
eval ${cmd}

# move to dist directory
if [[ $? -eq 0 ]]; then
  mv ${TAR_FILE} docker/
  echo "=============================================================================="
  echo "===============  🎉 package end in docker directory !!!  ======================="
  echo "=============================================================================="
fi
pwd

cd $origin
# 构建镜像
cmd2="docker buildx build --build-arg bitlap_server=bitlap-${tag} . -t liguobin/bitlap:${tag} --cache-to type=inline --cache-from type=registry,ref=liguobin/bitlap:${tag} -f ./Dockerfile"
echo "========================================================================================================================================"
echo "==================  🔥 build image start: ${cmd2}  ============================"
echo "========================================================================================================================================"
eval ${cmd2}

if [[ $? -eq 0 ]]; then
  echo "=============================================================================="
  echo "===============  🎉 build image successfully !!!  ======================="
  echo "=============================================================================="
  
  # 运行server
  docker run --name bitlap-$tag -dit -p 24333:24333 -p 23333:23333 -p 22333:22333  liguobin/bitlap:$tag
  echo "===============  🎉 bitlap_server running successfully !!!  ======================="
fi
pwd