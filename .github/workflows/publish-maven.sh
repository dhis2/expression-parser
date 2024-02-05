set -x

branch=$(git rev-parse --abbrev-ref HEAD)

if [ "$branch" = "main" ]; then
  ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
elif [ "$branch" = "beta" ]; then
  ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository -PbetaToSnapshot
fi
