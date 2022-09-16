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
releaseinfo="releaseinfo.md"

rm -f $releaseinfo

mv build/libs/Leaves-paperclip-$gradleVersion-reobf.jar $jarName
echo "name=Leaves-$commitid" >> $GITHUB_ENV
echo "tag=$tagid" >> $GITHUB_ENV
echo "jar=$jarName" >> $GITHUB_ENV
echo "info=$releaseinfo" >> $GITHUB_ENV

echo "Leaves-$commitid [![download](https://img.shields.io/github/downloads/LeavesMC/Leaves/$tagid/total?color=0)](https://github.com/Leaves/LeavesMC/releases/download/$tagid/$jarName)" >> $releaseinfo
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
