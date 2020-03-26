pipeline {
  agent any
  environment {
    JENKINS_VIEWER_TOKEN = credentials('jenkins-viewer-token')
  }
  parameters {
    choice(name: 'product', choices: ['platform', 'standard'] , description: '产品类型')
    string(name: 'customer', defaultValue: '', description: '客户类型')
    string(name: 'version', defaultValue: '', description: '版本标识')
  }
  stages {
    stage('Build') {
      steps {
        script {
          withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
            sh "scp -i $PEM_FILE -o StrictHostKeyChecking=no jenkins@172.31.22.148:${env.JENKINS_PIPLINE_LIB_PATH}/db.json ./"
          }
          def db_package = readJSON file: 'db.json'
          def packages = db_package["packages"]
          packages.each {
            if (it.product == "${product}" && it.customer == "${customer}" && it.version == "${tag}") {
              modules = it.branches
              modules.each { module, branch ->
                sh """
                curl -XPOST https://viewer:\${JENKINS_VIEWER_TOKEN}@jenkins.bizseer.com/job/${module}/job/${branch}/build
                sleep 5
                """
              }
            }
          }
        }
      }
    }
  }
}