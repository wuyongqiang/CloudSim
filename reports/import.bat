
set WORKPATH=c:\users\n7682905

set CLOUDPATH="C:\Documents and Settings\t9gw5733\CloudSimSrc\CloudSimExamples"\bin;"C:\Documents and Settings\t9gw5733\CloudSimSrc\CloudSim"\bin

cd %WORKPATH%
if  not "%1"=="" java -cp %CLOUDPATH%  org.cloudbus.cloudsim.examples.power.DoubleThreshold %1 %2


cd C:\Program Files\MySQL\MySQL Server 5.5\bin

mysqlimport --local -r -f --verbose  --fields-terminated-by=,    --ignore-lines=0 --lines-terminated-by="\r\n" --columns=simid,time,host,utilization,energy  test  c:\users\n7682905\simDetail.txt

mysqlimport --local -r -f --verbose  --fields-terminated-by=,    --ignore-lines=0 --lines-terminated-by="\r\n" --columns=simid,simdesc,length,migration,violation,avgviolation,energy  test  c:\users\n7682905\siminfo.txt

mysqlimport --local -r -f --verbose  --fields-terminated-by=,    --ignore-lines=0 --lines-terminated-by="\r\n" --columns=simid,time,vm,host,utilization,mips,hostmips,usedmips test  c:\users\n7682905\simvm.txt

mysqlimport --local -r -f --verbose  --fields-terminated-by=,    --ignore-lines=0 --lines-terminated-by="\r\n" --columns=simid,time,utilization test  c:\users\n7682905\simworkload.txt

mysqlimport --local -r -f --verbose  --fields-terminated-by=,    --ignore-lines=0 --lines-terminated-by="\r\n" --columns=simid,time,vm,host,req_utilization,aloc_utilization test  c:\users\n7682905\simviolation.txt

cd "C:\Documents and Settings\t9gw5733\CloudSimSrc"\reports

