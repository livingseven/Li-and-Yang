#!/usr/bin/env bash
# 在另一台 Linux 电脑上解压后执行： bash verify.sh
# 一次跑完测试 + 打包；看到 BUILD SUCCESS 和 target/*.jar 即通过脚手架验收
set -e
cd "$(dirname "$0")"

echo "== Java =="
java -version
echo "== Maven =="
mvn -v
echo "== 测试 + 打包 =="
mvn -B -ntp clean verify
echo "== 产物 =="
ls -lh target/*.jar
echo "== 运行 =="
echo "java -jar target/task-api-0.2.0-SNAPSHOT.jar"
echo "== 文档 =="
echo "http://localhost:8080/swagger-ui.html"
