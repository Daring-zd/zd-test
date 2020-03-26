def call(def target) {
  sh """
REPO_NAME=\$(echo \${JOB_NAME} | cut -d\'/\' -f1)
tar -czvf \${REPO_NAME}.tar.gz \${REPO_NAME}
CHECKSUM=\$(openssl md5 -binary \${REPO_NAME}.tar.gz | base64)
DEST=\${AWS_S3_BIZSEER_RELEASE}/package/compressed/\${REPO_NAME}/\${BRANCH_NAME}
s3cmd put \${REPO_NAME}.tar.gz \${DEST}/
s3cmd get \${DEST}/\${REPO_NAME}.tar.gz tmp.tar.gz
CHECKSUM_NEW=\$(openssl md5 -binary tmp.tar.gz | base64)
rm tmp.tar.gz
if [[ \${CHECKSUM} != \${CHECKSUM_NEW} ]]
then
  exit 1
fi
"""

  script {
    withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
      sh "scp -i $PEM_FILE -o StrictHostKeyChecking=no jenkins@172.31.22.148:${env.JENKINS_PIPLINE_LIB_PATH}/db.json ./"
    }
  }
  script {
    def job_name = "${env.JOB_NAME}".split('/')[0]
    def branch = "${env.BRANCH_NAME}"

    def cur_module_type="com"
    if (job_name.endsWith("core")){
        cur_module_type="jar"
    }
    def db_package = readJSON file: 'db.json'
    def packages = db_package["packages"]
    def belongsToPackage = false
    packages.each {
      it.branches.each { key, value ->
        if (job_name == key && branch == value){
          writeJSON file: "versions.json", json: it, pretty: 4
          belongsToPackage = true
          def sync_path_parent="${env.AWS_S3_BIZSEER_RELEASE}/package/full/${it.product}/${it.customer}/${it.version}"
          def sync_path = "${sync_path_parent}/${cur_module_type}"
          sh """
          s3cmd put ${job_name}.tar.gz ${sync_path}/
          # checksum
          s3cmd get ${sync_path}/${job_name}.tar.gz tmp.tar.gz
          CHECKSUM=\$(openssl md5 -binary ${job_name}.tar.gz | base64)
          CHECKSUM_NEW=\$(openssl md5 -binary tmp.tar.gz | base64)
          rm tmp.tar.gz
          if [[ \${CHECKSUM} != \${CHECKSUM_NEW} ]]
          then
            exit 1
          fi
          # update versions.json
          s3cmd put versions.json ${sync_path_parent}/
          # update commits.txt
          # s3cmd get ${sync_path_parent}/commits.txt --force || true
          # sed -i "/${job_name}/d" commits.txt || true
          # echo ${job_name}: \${GIT_COMMIT:0:7} >> commits.txt
          # sort commits.txt > commits-sorted.txt
          # s3cmd put commits-sorted.txt ${sync_path_parent}/commits.txt
          """
        }
      }
    }
    if (belongsToPackage == false) {
      sh """
      echo "模块未出现在任何一个整包中, 请定义整包结构"
      exit 1
      """
    }
  }
}