@echo off

rem 设置程序的根目录。
cd /d "%~dp0..\.."
SET "basedir=%cd%"

rem JVM 内存设置。
rem 如果您希望系统自动分配内存，注释掉下方注释...
SET jvm_memory_opts=^
-Xmx40m ^
-XX:MaxMetaspaceSize=40m ^
-XX:ReservedCodeCacheSize=8m ^
-XX:CompressedClassSpaceSize=5m
rem 并打开此注释。
rem SET jvm_memory_opts=

rem JAVA 日志配置。
rem 固定配置，请勿编辑此行。
SET java_logging_opts=^
-Dlog4j2.configurationFile=confext/poof/logging-settings.xml,conf/logging/poof/settings.xml ^
-Dlog4j.shutdownHookEnabled=false ^
-Dlog4j2.is.webapp=false

rem 打开目录，执行程序。
cd "%basedir%"
java -classpath "lib\*;libext\poof\*" ^
%jvm_memory_opts% ^
%java_logging_opts% ^
${mainClass.poof} "%1" "%2"

rem 退出脚本，退出码与程序退出码一致。
exit %ERRORLEVEL%
