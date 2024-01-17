set -ex

cd build/packages/js

npm version 0.1.0-beta.0

npm publish --tag beta