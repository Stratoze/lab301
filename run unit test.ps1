cd .\Lottery-App\frontend\   
npm run test
cd ..\..
cd .\Lottery-App\backend\checker\
.\mvnw.cmd test -Dtest="CheckerServiceImplTest,TicketServiceImplTest,UserServiceImplTest,AuthControllerTest" -DfailIfNoTests=false
cd ..\..\..
