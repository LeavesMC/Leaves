#!/usr/bin/env bash

sha256() {
  sha256sum "$1" | awk '{print $1}'
}

sha1() {
  sha1sum "$1" | awk '{print $1}'
}

md5() {
  md5sum "$1" | awk '{print $1}'
}

prop() {
  grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}

commitid=$(git log --pretty='%h' -1)
mcversion=$(prop mcVersion)
gradleVersion=$(prop version)
preVersion=$(prop preVersion)
tagid="$mcversion-$BUILD_NUMBER-$commitid"
jarName="leaves-$mcversion.jar"
leavesid="Leaves-$tagid"
releaseinfo="releaseinfo.md"
discordmes="discordmes.json"
make_latest=$([ "$preVersion" = "true" ] && echo "false" || echo "true")

rm -f $discordmes
rm -f $releaseinfo

mv leaves-server/build/libs/leaves-leavesclip-"$gradleVersion"-mojmap.jar "$jarName"
{
  echo "name=$leavesid"
  echo "tag=$tagid"
  echo "jar=$jarName"
  echo "info=$releaseinfo"
  echo "discordmes=$discordmes"
  echo "pre=$preVersion"
  echo "make_latest=$make_latest"
} >> "$GITHUB_ENV"

{
  echo "$leavesid [![download](https://img.shields.io/github/downloads/LeavesMC/Leaves/$tagid/total?color=0)](https://github.com/LeavesMC/Leaves/releases/download/$tagid/$jarName)"
  echo "====="
  echo ""
  if [ "$preVersion" = "true" ]; then
    echo "> This is an early, experimental build. It is only recommended for usage on test servers and should be used with caution."
    echo "> **Backups are mandatory!**"
    echo ""
  fi
  echo "### Commit Message"
} >> $releaseinfo

number=$(git log --oneline master ^"$(git describe --tags --abbrev=0)" | wc -l)
git log --pretty='> [%h] %s' "-$number" >> $releaseinfo

{
  echo ""
  echo "### Checksum"
  echo "| File | $jarName |"
  echo "| ---- | ---- |"
  echo "| MD5 | $(md5 "$jarName") |"
  echo "| SHA1 | $(sha1 "$jarName") |"
  echo "| SHA256 | $(sha256 "$jarName") |"
} >> $releaseinfo

{
  echo -n "{\"content\":\"Leaves New Release\",\"embeds\":[{\"title\":\"$leavesid\",\"url\":\"https://github.com/LeavesMC/Leaves/releases/tag/$tagid\",\"fields\":[{\"name\":\"Changelog\",\"value\":\""
  # shellcheck disable=SC2046
  echo -n $(git log --oneline --pretty='> [%h] %s\\n' "-$number")
  echo "\",\"inline\":true}]}]}"
} >> $discordmes