pipeline {
  agent any
  environment {
    AWS_ACCESS_KEY_ID     = credentials('amazon-s3-access-key')
    AWS_SECRET_ACCESS_KEY = credentials('amazon-s3-secret-key')
  }
  parameters {
    choice(
      name: 'target',
      choices: [
        "172.31.1.6",
        "172.31.8.2",
        "172.31.8.3",
        "172.31.8.4",
        "172.31.8.5",
        "172.31.8.6",
      ],
      description: "请选择集成测试环境 8.2(p-base-master) 8.3(p-base-dev) 8.4(p-ceb-dev) 8.5(s-base-master) 8.6(p-base-master)"
    )
    choice(name: 'type', choices: ['platform', 'standard'] , description: '产品类型')
    string(name: 'client', defaultValue: '', description: '客户类型')
    string(name: 'category', defaultValue: '', description: '版本标识')
    string(name: 'desc', defaultValue: '', description: '测试通过说明')
  }
  stages {
    stage('Build') {
      steps {
        script {
          def default_user = "centos"
          withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
            def commits = sh(returnStdout: true, script: "ssh -i $PEM_FILE -o StrictHostKeyChecking=no  ${default_user}@${params.target}  \"cd ${env.BIZSEER_TESTENV_PATH} && ./bizseer.sh commits\"").trim()
            def sync_path_parent="${env.AWS_S3_BIZSEER_RELEASE}/package/full/${product}/${params.client}/${params.category}"
            sh """
            echo \"${commits}\" > /tmp/commits-${params.target}.txt
            ts=\$(date +%s)
            s3cmd put /tmp/commits-${params.target}.txt ${sync_path_parent}/commits-\${ts}-${params.desc}.txt
            """
          }
        }
      }
    }
  }
}