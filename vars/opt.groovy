def call(def module) {
  pipeline {
    agent any
    options {
      timeout(time: 20, unit: 'MINUTES')
    }
    environment {
      DINGTALK_BUILD_TOKEN = credentials('dingding-build')
    }
    stages {
      stage('Test') {
        steps {
          sh '''pwd
  env'''
        }
      }
      stage('Build') {
        steps {
          sh 'mvn clean package -Dmaven.test.skip=true'
        }
      }
      stage('Deliver') {
        steps {
          sh """
DEST=\${AWS_S3_BIZSEER_RELEASE}/package/opt
s3cmd put \$(ls ${module}) \$DEST/
          """
        }
      }
    }
    post {
      success {
        notifyBuild("成功")
      }
      unsuccessful {
        notifyBuild("失败")
      }
    }
  }
}