nvm use
echo "copy jhipster file to another directory"
cp -R src/main/resources/config/liquibase/changelog liquibase-changelog-backup
jhipster entity $1
echo "copy jhipster file to another directory"
cp -R src/main/resources/config/liquibase/changelog liquibase-changelog-backup-new
echo "copy jhipster file back"
cp -R liquibase-changelog-backup/* src/main/resources/config/liquibase/changelog/ 
echo "now run diff"
mvn liquibase:diff

echo "now please check the change from liquibase. and confirm. type y or Y"
read -p "Are you sure? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
   rm -R liquibase-changelog-backup
   rm -R liquibase-changelog-backup-new
fi

