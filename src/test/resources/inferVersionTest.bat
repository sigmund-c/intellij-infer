@echo off
if "%~1"=="--version-json" goto version
goto end

:version
echo {
echo "major": 0,
echo "minor": 17,
echo "patch": 0,
echo "commit": "4a91616",
echo "branch": "HEAD",
echo "tag": "v0.17.0"
echo }

:end