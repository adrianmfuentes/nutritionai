@echo off
echo Abriendo puerto 3000 en el Firewall de Windows para Node.js...
netsh advfirewall firewall add rule name="Node.js Port 3000" dir=in action=allow protocol=TCP localport=3000
echo.
echo Regla agregada. Verifica si el problema persiste.
echo Asegurate tambien que tu movil este en la MISMA red WiFi (mismo rango de IP 192.168.1.x)
pause
