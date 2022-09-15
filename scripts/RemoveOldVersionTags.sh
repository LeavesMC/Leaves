if [ $# != 1 ]; then
  echo need input version
  exit 1
fi

version=$1
lastestTag=$(git describe --tags --abbrev=0 --match "$version-*")

echo "$(git tag --list "$version-*")" >> tags.temp

while read line
do
  if [ $line != $lastestTag ]; then
    git push origin :refs/tags/$line
    #echo $line
  fi
done < tags.temp

rm tags.temp
