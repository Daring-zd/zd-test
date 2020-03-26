pipeline {
  agent any
  environment {
    AWS_ACCESS_KEY_ID     = credentials('amazon-s3-access-key')
    AWS_SECRET_ACCESS_KEY = credentials('amazon-s3-secret-key')
  }
  parameters {
    choice(
        name: 'department', 
        choices: [
            "research", 
            "bigdata",
            "deliver",
            "presale",
            "algorithm",
            "develop",
            "efficiency",
            "product",
        ] , 
        description: 'department'
    )
    string(name: 'username', defaultValue: 'Full spelling of user name', description: 'username')
  }
  stages {
    stage('Build') {
      steps {
        script {
          def default_user = "centos"
          withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
            sh """
            ssh -i $PEM_FILE -o StrictHostKeyChecking=no  ${default_user}@172.31.22.214 "
            sudo sed -i '/${params.department}[ ]*\$/a\\${params.username} `tr -cd '[:alnum:]' </dev/urandom | head -c  8`' /etc/openvpn/psw-file"
            """
          }
        }
      }
    }
  }
}
