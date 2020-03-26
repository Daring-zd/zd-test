import groovy.transform.Field

@Field def phone = [
  "zx": 17710356602,
  "wjyuan": 13691059451,
  "wjyi": 13693280977,
  "zcj": 18510508363,
  "wn": 18631090668,
  "xd": 13488688984
]

@Field def maintainers = [
  "master": [phone["wjyuan"], phone["zcj"]],
  "cmbc": [phone["zx"], phone["wn"]],
  "ceb": [phone["zx"], phone["xd"]],
]

def call(def type, def client, def version) {
  dingding("${env.DINGTALK_TOKEN}",
    "# 新版本发布\n" +
    "- 版本类型: ${type}\n\n" +
    "- 客户类型: ${client}\n\n" +
    "- 版本: ${version}\n\n" +
    "- [点击](https://file.bizseer.com/files/release/${type}/${client}/${version})进入下载页面",
    "发布结果↑: @all",
    "[]",
    true
  )
}