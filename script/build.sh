#!/bin/bash

#  SealClass Build
#
#  Copyright (c) 2019 RongCloud. All rights reserved.

# 该脚本原则上由 Jenkins 编译触发，如果想本地编译，请通过 pre_build.sh 触发本脚本

#sh scripts/build.sh

trap exit ERR

BUILD_PATH=`pwd`
OUTPUT_PATH="${BUILD_PATH}/output"
BRANCH=${Branch}
CUR_TIME=$(date "+%Y%m%d%H%M")
APP_VERSION=${APP_Version}

echo "Build Path:"$BUILD_PATH

#拉取源码，参数 1 为 git 仓库目录,2 为 git 分支
function pull_sourcecode() {
  path=$1
  branch=$2
  cd ${path}
  git fetch
  git reset --hard
  git checkout ${branch}
  git pull origin ${branch}
}


chmod +x gradlew
./gradlew clean -i
./gradlew sealclass:assemble


rm -rf $OUTPUT_PATH
mkdir -p $OUTPUT_PATH

cp -r  sealclass/build/outputs/apk/debug/sealclass-debug.apk ${OUTPUT_PATH}/SealClass_v${APP_VERSION}_${CUR_TIME}.apk
cp -r  sealclass/build/outputs/apk/release/sealclass-release.apk ${OUTPUT_PATH}/SealClass_v${APP_VERSION}_${CUR_TIME}_release.apk
