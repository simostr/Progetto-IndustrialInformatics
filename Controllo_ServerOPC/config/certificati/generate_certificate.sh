sudo openssl req -x509 -nodes -newkey rsa:2048 -keyout my_private_key.pem -out my_cert.pem -config req.config -extensions 'v3_req'
sudo openssl x509 -outform der -in my_cert.pem -out my_cert.der
sudo chmod g=r my_private_key.pem
sudo chmod g=r my_cert.der
sudo chmod g=r my_cert.pem
sudo chmod o=r my_private_key.pem
sudo chmod o=r my_cert.der
sudo chmod o=r my_cert.pem
