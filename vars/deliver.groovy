def call(def target) {
  sh """
REPO_NAME=\$(echo \$JOB_NAME | cut -d\'/\' -f1)
rm -rf \$REPO_NAME
mkdir -p \$REPO_NAME
cp -r ${target} \$REPO_NAME/ || true
case \$REPO_NAME in
  anomaly-restapi)
    cp src/main/resources/*.template \$REPO_NAME/
    ;;
  aiops-restapi)
    cp src/main/resources/application-prod.properties.template \$REPO_NAME/
    mkdir -p \$REPO_NAME/conf
    cp src/main/resources/conf/* \$REPO_NAME/conf/
    ;;
  aiops-kraken)
    cp src/main/resources/application-prod.properties.template \$REPO_NAME/
    ;;
  *)
    echo "other repos"
    ;;
esac
echo \$GIT_COMMIT > \$REPO_NAME/COMMIT.md
cp CHANGELOG.md \$REPO_NAME/CHANGELOG.md || true
"""
}