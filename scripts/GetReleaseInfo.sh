sha1() {
  sha1sum $1 | awk '{print $1}'
}

md5() {
  md5sum $1 | awk '{print $1}'
}

tagid="1.19.2-`git log --pretty='%h' -1`"

echo "Leaves-$tagid [![download](https://img.shields.io/github/downloads/LeavesMC/Leaves/$tagid/total?color=0)](https://github.com/Leaves/LeavesMC/releases/download/$tagid/leaves-1.19.2.jar)" >> releaseinfo.md
echo "=====" >> releaseinfo.md
echo "" >> releaseinfo.md
echo "### Commit Message" >> releaseinfo.md

number=$(git log --oneline master ^`git describe --tags --abbrev=0` | wc -l)
echo `git log --pretty='> [%h] %s' -$number` >> releaseinfo.md

echo "" >> releaseinfo.md
echo "### Checksum" >> releaseinfo.md
echo "| File | leaves-1.19.2.jar |" >> releaseinfo.md
echo "| ---- | ---- |" >> releaseinfo.md
echo "| MD5 | `md5 "leaves-1.19.2.jar"` |" >> releaseinfo.md
echo "| SHA1 | `sha1 "leaves-1.19.2.jar"` |" >> releaseinfo.md
