sha256() {
  sha256sum $1 | awk '{print $1}'
}

prop() {
  grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}

# shellcheck disable=SC2154
echo "$tag"
project_id="leaves"
mcversion=$(prop mcVersion)
if [ "$(prop preVersion)" = "true" ]; then
  channel="experimental"
else
  channel="default"
fi

number=$(git log --oneline master ^"$(git describe --tags --abbrev=0)" | wc -l)
changes=$(git log --pretty='%H<<<%s>>>' -"$number" | sed ':a;N;$!ba;s/\n//g')
jar_name="leaves-$mcversion.jar"
jar_sha256=$(sha256 "$jar_name")

./GetBuildNumber.sh
# shellcheck disable=SC2154
curl --request POST --header "Content-Type: application/json" --header "X-LeavesMC-Authorization: $(echo -n "$(date +%s | sed 's/.$//')$WEBHOOK_PASSWORD" | md5sum | awk '{print $1}')" --data "{\"repo\":\"LeavesMC/Leaves\",\"project\":\"$project_id\",\"version\":\"$mcversion\",\"build\":\"$BUILD_NUMBER\",\"commit\":\"$(git rev-parse --short "$GITHUB_SHA")\"}" "$ENTRYPOINT"

# shellcheck disable=SC2154
curl --location --request POST "https://api.leavesmc.org/v2/commit/build" --header "Content-Type: application/json" --header "Authentication: Bearer $secret_v2" --data-raw "{\"project_id\":\"$project_id\",\"version\":\"$mcversion\",\"channel\":\"$channel\",\"changes\":\"$changes\",\"jar_name\":\"$jar_name\",\"sha256\":\"$jar_sha256\",\"tag\":\"$tag\"}"
