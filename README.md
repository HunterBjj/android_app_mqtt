# <p align="center"> Андройд приложение с Blutootch и mqtt сервером на python </p>

Инструкции по Установке и Запуску

Установка и Запуск Python Сервиса

Установите и настройте виртуальную среду Python:


sh

sudo apt-get update

sudo apt-get install -y python3-venv

python3 -m venv venv

source venv/bin/activate

Установите необходимые зависимости:


sh

pip install -r python_service/requirements.txt

Запустите MQTT брокер (например, Mosquitto):

sh


sudo apt-get install -y mosquitto

sudo systemctl start mosquitto

Настройте и запустите сервис через systemd:

sh



sudo cp python_service/mqtt_ble_service.service /etc/systemd/system/

sudo systemctl daemon-reload

sudo systemctl start mqtt_ble_service

sudo systemctl enable mqtt_ble_service

