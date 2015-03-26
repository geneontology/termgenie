rm -rf svn
#rm -rf first_checkout
mkdir svn
SVN_TARGET_DIR="$PWD/svn"
svnadmin create $SVN_TARGET_DIR
echo $SVN_TARGET_DIR
svn import -m "initial import" ontology "file://$SVN_TARGET_DIR"
#svn co "file://$SVN_TARGET_DIR" first_checkout
#svn propset svn:externals -F first_checkout/external/svn.externals first_checkout/external
#svn commit -m"set externals" first_checkout/external
#svn update first_checkout
cp -r svn ../

