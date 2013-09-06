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

cp -a $distDir/* $tmpDir/
mkdir $tmpDir/TweakMode/src

file_to_copy=`ls $projectDir`
for file in $file_to_copy
do
	if [ $file != "dist" ]
	then
		echo "# CP $file $tmpDir/TweakMode/src"
		cp -a $file $tmpDir/TweakMode/src
	fi
done

packageName="tweakmode-${VERSION}.zip"
pushd $tmpDir
zip -r $distDir/$packageName TweakMode
popd

echo "done at: $distDir/$packageName"

exit 0





