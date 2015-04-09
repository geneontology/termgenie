rm -rf svn
rm -rf first_checkout
mkdir svn
SVN_TARGET_DIR="$PWD/svn"
svnadmin create $SVN_TARGET_DIR
echo $SVN_TARGET_DIR
svn import -m "initial import" ontology "file://$SVN_TARGET_DIR"
rm -rf ../work/svn
cp -r svn ../work/svn

