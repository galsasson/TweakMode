#/bin/sh

VERSION=$1

if [ -z $VERSION ]
then
	echo "./usage: $0 <version tag>"
	exit 1
fi

## make sure this tag exists
if ! git tag | grep $VERSION >> /dev/null
then
	echo "error: version '$VERSION' does not exists in this repository."
	exit 2
fi

pushd `dirname $0`
projectDir=`pwd`
popd

distDir="$projectDir/dist"
tmpDir="`mktemp -dt tweakmodebuild-XXXXX`"

# copy distribution
cp -a $distDir/* $tmpDir/

# copy source code
cp -a $projectDir/src $tmpDir/TweakMode

# pack
packageName="tweakmode.zip"
pushd $tmpDir
zip -r $distDir/$packageName TweakMode
popd

cp $distDir/TweakMode/mode.properties $distDir/tweakmode.txt

echo "done:"
echo "  $distDir/$packageName"
echo "  $distDir/tweakmode.txt"
exit 0





