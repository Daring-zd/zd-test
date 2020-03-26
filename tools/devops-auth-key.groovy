pipeline {
  agent any
  environment {
    AWS_ACCESS_KEY_ID     = credentials('amazon-s3-access-key')
    AWS_SECRET_ACCESS_KEY = credentials('amazon-s3-secret-key')
  }
  parameters {
    string(name: 'address', defaultValue: '172.31.1.10', description: 'address')
    string(name: 'strauth', defaultValue: 'input auth_key', description: 'strauth')
  }
  stages {
    stage('Build') {
      steps {
        script {
          def default_user = "centos"
          withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
            sh """
            ssh -i $PEM_FILE -o StrictHostKeyChecking=no  ${default_user}@${params.address} "echo ${params.strauth} >> /home/centos/.ssh/authorized_keys"
            """
          }
        }
      }
    }
  }
}
