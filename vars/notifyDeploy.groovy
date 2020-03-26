def call(def info, def type, def client, def branch, def target) {
  dingding("${env.DINGTALK_DEPLOY_TOKEN}",
    "# 测试环境更新${info}\n" +
    "产品类型: ${type}\n\n" +
    "客户: ${client}\n\n" +
    "分支: ${branch}\n\n" +
    "[点击](http://${target}:8000)访问",
    "更新结果↑: @all",
    "[]",
    true
  )
}