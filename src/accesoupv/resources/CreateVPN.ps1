$importXML = New-Object XML
$importXML.Load("${env:temp}\XMLNAME")
Add-VpnConnection -Name "VPNNAME" -ServerAddress "vpn.upv.es" -AuthenticationMethod Eap -EncryptionLevel Required -RememberCredential -TunnelType Sstp -EapConfigXmlStream $importXML