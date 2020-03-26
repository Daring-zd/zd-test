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
      stage('Prepare') {
        steps {
          sh '''
npm config set sass-binary-site http://cdn.npm.taobao.org/dist/node-sass
npm install
'''
        }
      }
      stage('Build') {
        steps {
          sh '''
./script/dygraph-hotfix.sh
npm run build
'''
        }
      }
      stage('Deliver') {
        steps {
          deliver(module)
          sh """
REPO_NAME=\$(echo \$JOB_NAME | cut -d\'/\' -f1)
cp dist/static/js/config.js \$REPO_NAME/dist/static/js/config.js.template
          """
          compress()
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