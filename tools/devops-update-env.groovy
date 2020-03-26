library 'bizseer-ci'

pipeline {
  agent {
    node {
      label 'master'
    }
  }
  environment {
    DINGTALK_TOKEN = credentials('dingding-deploy')
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
    // choice(name: 'type', choices: ['s', 'p'] , description: 'chose type: <br> s => standard <br> p => platform ')
    // choice(name: 'client', choices: ['base', 'cmbc', 'ceb', 'ccb', 'cmb', 'bcm', 'cgb', 'spdb'] , description: '选择版本')
    // choice(name: 'branch', choices: ['master', 'dev'], description: '选择分支')
    booleanParam(name: 'update', defaultValue: false, description: '使用modules.json更新所有算法默认配置')
    // booleanParam(name: 'user_env', defaultValue: false, description: '是否使用自定义环境 <br>使用自定义环境，请填写下面参数，不使用则点击构建')
    // string(name: 'testenv_user', defaultValue: '', description: '请输入自定义环境的用户名')
    // string(name: 'testenv_ip', defaultValue: '', description: '请输入自定义环境的主机IP')
    // string(name: 'testenv_path', defaultValue: '', description: '请输入自定义环境的testenv路径')
  }

  stages {
    stage('Update env') {
      steps {
        script{
          def default_user = "centos"
          // def mapping=[
          //   "p-base-master": "172.31.8.2",
          // ]
          // server = mapping.get(params.target)
          withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
            sh "ssh -i $PEM_FILE -o StrictHostKeyChecking=no  ${default_user}@${params.target}  \"cd ${env.BIZSEER_TESTENV_PATH} && ./bizseer.sh update && ./testbed.sh restart\""
            if (params.update) {
              sh "ssh -i $PEM_FILE -o StrictHostKeyChecking=no  ${default_user}@${params.target}  \"cd ${env.BIZSEER_TESTENV_PATH} && ./bizseer.sh update_config\""
            }
          }
        }
      }
    }
  }
  post {
    success {
      script {
        def default_user = "centos"
        withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
          def commits = sh(returnStdout: true, script: "ssh -i $PEM_FILE -o StrictHostKeyChecking=no  ${default_user}@${params.target}  \"cd ${env.BIZSEER_TESTENV_PATH} && ./bizseer.sh commits\"").trim()
          dingding("${env.DINGTALK_TOKEN}",
            "## 测试环境 => ${params.target}  \n" +
            "- 更新状态: 成功 \n" +
            "- 访问链接，[点击](http://${params.target})访问\n" +
            "- ${commits}\n",
            "更新结果↑: @all",
            "[]",
            true
          )
        }
      }
    }
    unsuccessful {
      dingding("${env.DINGTALK_TOKEN}",
        "## 测试环境 => ${params.target}  \n" +
        "- 更新状态: 失败 ",
        "更新结果↑: @all",
        "[]",
        true
      )
    }
  }
}