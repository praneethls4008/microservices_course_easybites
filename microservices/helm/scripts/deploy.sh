#!/bin/sh

# 1. Uninstall the existing release
echo "Uninstalling helm release..."
# The || true ensures the script continues even if the release doesn't exist
helm uninstall eazybytes-release-v1 || echo "Release not found, skipping..."

# 2. Remove all Chart.lock files and charts/ directories recursively
echo "Cleaning up Helm dependency files..."
find . -type f -name "Chart.lock" -delete
find . -type d -name "charts" -exec rm -rf {} +

# 3. Run the build-all script
echo "Running build-all.sh..."
if [ -f "./scripts/build-all.sh" ]; then
    chmod +x ./scripts/build-all.sh
    ./scripts/build-all.sh
else
    echo "Error: ./scripts/build-all.sh not found!"
    exit 1
fi

# 4. Install the new release
echo "Installing helm release..."
helm install eazybytes-release-v1 ./umbrella/ -f ./environments/dev.yaml