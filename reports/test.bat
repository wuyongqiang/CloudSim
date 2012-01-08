@echo test started
@echo off

rem FOR   %%b IN (0.8 0.1,0.7 0.2,0.6 0.3,0.4 0.5) DO (

rem call import %%b ;

set calltest=import
if NOT "%1"=="pid" ( 
@echo "DOUBLE THRESHOLD TESt"
call %calltest% 0.8 0.2;
call %calltest% 0.8 0.3;
call %calltest% 0.8 0.4;
call %calltest% 0.6 0.2;
call %calltest% 0.8 0.5;
call %calltest% 0.8 0.6;
call %calltest% 0.8 0.7;
call %calltest% 0.7 0.4;
call %calltest% 0.6 0.4;
call %calltest% 0.5 0.4;
call %calltest% 0.6 0.2;
)
else
(
@echo "PID TEST"
call %calltest% 0.9 0.0;
call %calltest% 0.8 0.0;
call %calltest% 0.7 0.0;
call %calltest% 0.6 0.0;
call %calltest% 0.5 0.0;
call %calltest% 0.4 0.0;
call %calltest% 0.3 0.0;
call %calltest% 0.2 0.0;
call %calltest% 0.1 0.0;
)
rem @echo "OK" )
@echo on