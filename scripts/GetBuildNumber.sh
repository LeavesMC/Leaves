#!/usr/bin/env bash

prop() {
  grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}

latest_build=$(curl -s -L "https://api.leavesmc.org/v2/projects/leaves/versions/$(prop mcVersion)/latestGroupBuildId")

if [[ $latest_build =~ ^[0-9]+$ ]]; then
    echo "BUILD_NUMBER=$((latest_build + 1))" >> "$GITHUB_ENV"
else
    echo "Error: Received non-integer value from API: $latest_build"
    exit 1
fi
