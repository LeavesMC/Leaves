sha1() {
  sha1sum $1 | awk '{print $1}'
}

md5() {
  md5sum $1 | awk '{print $1}'
}

prop() {
  grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}

commitid=$(git log --pretty='%h' -1)
mcversion=$(prop mcVersion)
gradleVersion=$(prop version)
tagid="$mcversion-$commitid"
jarName="leaves-$mcversion.jar"
leavesid="Leaves-$commitid"
releaseinfo="releaseinfo.md"
discordmes="discordmes.json"

rm -f $discordmes
rm -f $releaseinfo

mv build/libs/Leaves-paperclip-$gradleVersion-reobf.jar $jarName
echo "name=$leavesid" >> $GITHUB_ENV
echo "tag=$tagid" >> $GITHUB_ENV
echo "jar=$jarName" >> $GITHUB_ENV
echo "info=$releaseinfo" >> $GITHUB_ENV
echo "discordmes=$discordmes" >> $GITHUB_ENV

echo "$leavesid [![download](https://img.shields.io/github/downloads/LeavesMC/Leaves/$tagid/total?color=0)](https://github.com/Leaves/LeavesMC/releases/download/$tagid/$jarName)" >> $releaseinfo
echo "=====" >> $releaseinfo
echo "" >> $releaseinfo
echo "### Commit Message" >> $releaseinfo

number=$(git log --oneline master ^`git describe --tags --abbrev=0` | wc -l)
echo "$(git log --pretty='> [%h] %s' -$number)" >> $releaseinfo

echo "" >> $releaseinfo
echo "### Checksum" >> $releaseinfo
echo "| File | $jarName |" >> $releaseinfo
echo "| ---- | ---- |" >> $releaseinfo
echo "| MD5 | `md5 $jarName` |" >> $releaseinfo
echo "| SHA1 | `sha1 $jarName` |" >> $releaseinfo

echo "{\"embeds\":[{\"title\":\"$leavesid\",\"url\":\"https://github.com/LeavesMC/Leaves/releases/tag/$tagid\",\"fields\":[{\"name\":\"Changelog\",\"value\":\"$(git log --pretty='> [%h] %s' -$number)\",\"inline\":true}]}]}" >> $discordmes