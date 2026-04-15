#!/bin/bash

# Function to build helm dependencies in subdirectories
build_helm_deps() {
    local dir=$1
    echo "Step: $dir"
    cd "$dir" || return
    
    for d in */; do
        # Remove trailing slash for display
        subdir=${d%/}
        if [ -f "$subdir/Chart.yaml" ]; then
            echo "Building $subdir"
            helm dependency build "$subdir"
        fi
    done
    cd ..
}

echo "Building Helm dependencies..."

build_helm_deps "observability"
build_helm_deps "infrastructure"
build_helm_deps "platform"
build_helm_deps "databases"
build_helm_deps "microservices"

echo "Step 5: Umbrella"
cd umbrella && helm dependency build && cd ..

echo "DONE"
