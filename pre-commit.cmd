set CHECKSTYLE=F:\checkstyle-8.11-all.jar
set JAVA_SCRIPT=F:\Repositories\project\hooks\svn-checkstyle.jar
set CHECKSTYLE_CONFIG=F:\my_checkstyle.xml
set TMPDIR=F:\mytmp

set REPOS=%1
set TXN=%2


java -cp %JAVA_SCRIPT% com.fescotech.svn.checkstyle.core.SvnCheckScriptMain %REPOS% %TXN% %CHECKSTYLE% %CHECKSTYLE_CONFIG% %TMPDIR%

if %ERRORLEVEL% neq 0 (
	exit %ERRORLEVEL%
)