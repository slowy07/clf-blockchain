#! /bin/sh
SOURCE=$1
SOURCE_FOLDER=$2
if [[ ${SOURCE} == "local" ]]; then
    echo "running local source code"
    if ! [[ -z "$SOURCE_FOLDER" ]]; then
        echo "copy local source from $SOURCE_FOLDER to /development"
        cp -r ${SOURCE_FOLDER}/. /development
    fi
        cp /development

sbt "run 0.0.0.0 8080"
