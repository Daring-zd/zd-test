def call(def token, def markdown, def text, def maintainers, def atAll) {
  httpRequest contentType: 'APPLICATION_JSON_UTF8',
    httpMode: 'POST',
    requestBody: """
      {
        "msgtype": "markdown",
        "markdown": {
          "title":"构建结果",
          "text": "${markdown}",
        }
      }
    """,
    responseHandle: 'NONE',
    url: "https://oapi.dingtalk.com/robot/send?access_token=${token}"
  httpRequest contentType: 'APPLICATION_JSON_UTF8',
    httpMode: 'POST',
    requestBody: """
      {
        "msgtype": "text",
        "text": {
          "content":"${text}",
        },
        "at": {
          "atMobiles": ${maintainers},
          "isAtAll": ${atAll}
        }
      }
    """,
    responseHandle: 'NONE',
    url: "https://oapi.dingtalk.com/robot/send?access_token=${token}"
}