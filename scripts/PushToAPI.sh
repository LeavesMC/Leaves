sha256() {
  sha256sum $1 | awk '{print $1}'
}

prop() {
  grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}
echo "$tag"
project_id="leaves"
project_name="leaves"
mcversion=$(prop mcVersion)
ctime=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
pre=$(prop preVersion)
if [ $pre = "true" ]; then
  channel="experimental"
else
  channel="default"
fi
promoted=false

number=$(git log --oneline master ^`git describe --tags --abbrev=0` | wc -l)
changes=$(git log --pretty='%H<<<%s>>>' -"$number" | sed 's/\\n/\\\\n/g')
jar_name="leaves-$mcversion.jar"
jar_sha256=`sha256 $jar_name`

data=$(cat <<EOF
'{
    "project_id": "$project_id",
    "project_name": "$project_name",
    "version": "$mcversion",
    "time": "$ctime",
    "channel": "$channel",
    "promoted": $promoted,
    "changes": "$changes",
    "downloads": {
        "application": {
            "name": "$jar_name",
            "sha256": "$jar_sha256",
            "url": "https://github.com/LeavesMC/Leaves/releases/download/$tag/$jar_name"
        }
    },
    "secret": "$secret"
}'
EOF
)

curl -X POST -H "Content-Type: application/json" -d $data https://api.leavesmc.top/new_release
