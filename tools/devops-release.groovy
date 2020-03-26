library 'bizseer-ci'

pipeline {
  agent {
    node {
      label 'jekins-slave-1.9'
    }
  }
  environment {
    DINGTALK_TOKEN = credentials('dingding-release')
    GITHUB_TOKEN = credentials('github-token')
  }
  parameters {
    choice(name: 'product', choices: ['platform', 'standard'], description: 'product')
    choice(name: 'customer', choices: ['base', 'ceb', 'cmbc'], description: 'customer')
    choice(name: 'version', choices: ['master', '4.x-master'], description: 'version')
    string(name: 'tag', defaultValue: '0.0.1', description: 'tag')
  }
  stages {
    stage('Check') {
      steps {
        script {
          withCredentials([file(credentialsId: 'bizseer-root-pem', variable: 'PEM_FILE')]) {
            sh "scp -i $PEM_FILE -o StrictHostKeyChecking=no jenkins@172.31.22.148:${env.JENKINS_PIPLINE_LIB_PATH}/db.json ./"
          }
          sh "rm -rf /tmp/VERSIONS.md"
          def db_package = readJSON file: 'db.json'
          def packages = db_package["packages"]
          packages.each {
            // 如果是要发布的包
            if (it.product == "${product}" && it.customer == "${customer}" && it.version == "${version}") {
              modules = it.branches
              modules.each { module, branch ->
                def module_type="com"
                if (module.endsWith("core")){
                  module_type="jar"
                }
                sh """
                base=/home/centos/s3-bucket/bizseer-release
                src=\${base}/package/full/${product}/${customer}/${version}
                cd \${src}
                # 从压缩包中获取COMMIT
                COMMIT_LOCAL=\$(tar -axf ${module_type}/${module}.tar.gz ${module}/COMMIT.md -O)
                set +x
                # 从GITHUB获取COMMIT
                response=\$(curl -H "Authorization: token \$GITHUB_TOKEN"\
                  https://api.github.com/repos/bizseer/${module}/commits/${branch})
                COMMIT_REMOTE=\${response:12:40}
                set -x
                # 对比两个COMMIT
                if [[ \$COMMIT_LOCAL == \$COMMIT_REMOTE ]]
                then
                  echo -n "${module}: " >> /tmp/VERSIONS.md
                  echo \${COMMIT_LOCAL:0:7} >> /tmp/VERSIONS.md
                else
                  echo "version check failed: ${module}, ${branch}"
                  exit 1
                fi
                """
              }
            }
          }
        }
      }
    }
    stage('Release') {
      steps {
        sh """
        base=/home/centos/s3-bucket/bizseer-release
        src=\${base}/package/full/${product}/${customer}/${version}
        dst_parent=\${base}/release/${product}/${customer}/${tag}
        rm -rf \${dst_parent}
        mkdir -p \${dst_parent}
        dst=\${dst_parent}/bizseer-${product}-${customer}-${tag}
        cp -r \${src} \${dst}
        cp /tmp/VERSIONS-sorted.md \${dst}/VERSIONS.md
        mkdir -p \${dst}/docs
        """
      }
    }
    stage('Upload docs') {
      steps {
        echo "请打开对应目录上传文档: https://file.bizseer.com/files/release/${product}/${customer}/${tag}/bizseer-${product}-${customer}-${tag}/docs"
        input 'finish uploadisng?'
      }
    }
    stage('Archive') {
      steps {
        sh """
        base=/home/centos/s3-bucket/bizseer-release
        parent=\${base}/release/${product}/${customer}/${tag}
        fname=bizseer-${product}-${customer}-${tag}
        rm -rf /tmp/\${fname}
        cp -r \${parent}/\${fname} /tmp/\${fname}
        cd /tmp
        zip -r \${fname}.zip \${fname}
        cp \${fname}.zip \${parent}/
        tar -czvf \${fname}.tar.gz \${fname}
        cp \${fname}.tar.gz \${parent}/
        rm -rf /tmp/\${fname}*
        """
      }
    }
  }
  post {
    success {
      dingding("${env.DINGTALK_TOKEN}",
        "# 新版本发布\n" +
        "- 版本类型: ${product}\n" +
        "- 客户类型: ${customer}\n" +
        "- 版本: ${tag}\n" +
        "- [点击](https://file.bizseer.com/files/release/${product}/${customer}/${tag})进入下载页面",
        "发布结果↑: @all",
        "[]",
        true
      )
    }
  }
}
