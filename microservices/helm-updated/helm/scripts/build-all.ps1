Write-Host "Building Helm dependencies..."

Write-Host "Step 1: Infrastructure"
cd infrastructure
Get-ChildItem -Directory | ForEach-Object {
    if (Test-Path "$($_.FullName)\Chart.yaml") {
        Write-Host "Building $($_.Name)"
        cd $_.FullName
        helm dependency build
        cd ..
    }
}
cd ..

Write-Host "Step 2: Platform"
cd platform
Get-ChildItem -Directory | ForEach-Object {
    if (Test-Path "$($_.FullName)\Chart.yaml") {
        Write-Host "Building $($_.Name)"
        cd $_.FullName
        helm dependency build
        cd ..
    }
}
cd ..

Write-Host "Step 3: Databases"
cd databases
Get-ChildItem -Directory | ForEach-Object {
    if (Test-Path "$($_.FullName)\Chart.yaml") {
        Write-Host "Building $($_.Name)"
        cd $_.FullName
        helm dependency build
        cd ..
    }
}
cd ..

Write-Host "Step 4: Microservices"
cd microservices
Get-ChildItem -Directory | ForEach-Object {
    if (Test-Path "$($_.FullName)\Chart.yaml") {
        Write-Host "Building $($_.Name)"
        cd $_.FullName
        helm dependency build
        cd ..
    }
}
cd ..

Write-Host "Step 5: Umbrella"
cd umbrella
helm dependency build
cd ..

Write-Host "DONE"