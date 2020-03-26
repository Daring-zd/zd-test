import groovy.transform.Field

@Field def phone = [
  "lhy": 15810292399,
  "zwc": 18602462517,
  "nxh": 13466604873,
  "lhd": 18800100639,
  "cyr": 18801380776,
  "whq": 15210588685,
  "yy": 15114090407,
  "wy": 18811723016,
  "xzc": 18600886744,
  "zws": 17710237407,
  "swl": 18501033156,
  "ghb": 18335832151,
  "zyj": 15038707637,
  "pzz": 18811210135
]

@Field def branch_maintainers = [
  "aiops-restapi": [
    "ccb-dev": [phone["zws"]],
    "ccb-master": [phone["zws"]]
  ],
  "simple-java-maven-app": [
    "master": [phone["lhy"]]
  ]
]

@Field def repo_maintainers = [
  "anomaly-core": [phone["nxh"], phone["zwc"]],
  "fluxrank-core": [phone["nxh"]],
  "overseer-core": [phone["nxh"]],
  "carpenter-core": [phone["nxh"], phone["lhd"]],
  "volcano-core": [phone["nxh"], phone["cyr"]],
  "quoridor-core": [phone["nxh"], phone["whq"]],
  "anomaly-restapi": [phone["wy"], phone["xzc"]],
  "anomaly-overseer": [phone["wy"], phone["xzc"]],
  "anomaly-web": [phone["swl"], phone["ghb"]],
  "aiops-restapi": [phone["wy"], phone["xzc"]],
  "aiops-kraken": [phone["wy"], phone["xzc"]],
  "aiops-web": [phone["swl"], phone["ghb"]],
  "atlas": [phone["zyj"], phone["lhd"]],
  "bizseer-kaptain": [phone["zyj"], phone["lhd"]],
  "bizseer-waterdrop": [phone["zyj"], phone["lhd"]],
  "simple-java-maven-app": [phone["lhy"]]
]

def call(def result) {
  def job_name = env.JOB_NAME.split('/')[0]
  def branch_name = env.BRANCH_NAME
  def maintainers = []
  if (branch_maintainers.containsKey(job_name) && branch_maintainers[job_name].containsKey(branch_name)) {
    maintainers = branch_maintainers[job_name][branch_name]
  } else {
    maintainers = repo_maintainers[job_name]
  }
  dingding("${env.DINGTALK_BUILD_TOKEN}",
    "# ${job_name}构建${result}\n" +
    "分支: ${env.BRANCH_NAME}\n\n" +
    "commit: ${env.GIT_COMMIT.take(7)}\n\n" +
    "[点击](https://jenkins.bizseer.com/blue/organizations/jenkins/${job_name}/detail/${branch_name}/${env.BUILD_NUMBER}/pipeline)查看详情",
    "构建结果↑: ${maintainers.collect { "@$it" }.join(' ')}", // @1 @2
    "[\"${maintainers.join('", "')}\"]",
    false
  )
}