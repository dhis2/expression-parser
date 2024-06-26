set -x

branch=$(git rev-parse --abbrev-ref HEAD)

if [ "$branch" = "main" ]; then
  ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository -PremoveSnapshotSuffix
else
  ./gradlew publishToSonatype
fi
