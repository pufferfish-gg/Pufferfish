exit_on_error() {
    echo "$1"
    exit 1
}

git reset HEAD --hard

oldHash=$(grep "paperRef=" gradle.properties | cut -d "=" -f2)
newHash=$(curl -s https://api.github.com/repos/PaperMC/paper/commits/master | jq -r .sha)

if [ "$oldHash" = "$newHash" ]; then
    echo "Upstream has not updated!"
    exit 0
fi

echo "Updating paper: $oldHash -> $newHash"

sed -i "s/$oldHash/$newHash/g" gradle.properties
git add gradle.properties

./gradlew applyPatches || exit_on_error "An error occurred when merging patches!"
./gradlew rebuildPatches || exit_on_error "An error occurred when rebuilding patches!"
./gradlew createReobfPaperclipJar || exit_on_error "An error occurred when building!"

scripts/upstreamCommit.sh $oldHash $newHash

echo "Created new commit, please review before pushing."
