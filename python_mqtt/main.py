import paho.mqtt.client as mqtt
from bluepy.btle import Peripheral, UUID, Service, Characteristic
import threading

# MQTT settings
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
MQTT_TOPICS = [("mqtt/color", 0), ("mqtt/glimpse", 0)]

# UUIDs for BLE service and characteristic
SERVICE_UUID = UUID("12345678-1234-5678-1234-56789abcdef0")
CHARACTERISTIC_UUID = UUID("12345678-1234-5678-1234-56789abcdef1")

client = mqtt.Client()

current_color = None
current_glimpse = None

class BLEServer:
    def __init__(self):
        from bluepy import btle

        self.peripheral = btle.Peripheral()
        try:
            self.service = MyService(self.peripheral)
        except ValueError as e:
            print(f"Error initializing MyService: {e}")
            
    def advertise(self, name):
        print(f'Advertising BLE device with name: {name}')
        # Добавьте код для начала рекламы BLE сервиса здесь

    def send_data(self, data):
        service = self.peripheral.getServiceByUUID(SERVICE_UUID)
        characteristic = service.getCharacteristics(CHARACTERISTIC_UUID)[0]
        characteristic.write(data.encode())

    def set_service(self, *args):
        if len(args) == 4:  # Проверка количества элементов
            self.peripheral, uuidVal, self.hndStart, self.hndEnd = args
        else:
            print(f"Invalid args: expected 4, got {len(args)}")

        

class MyCharacteristic(Characteristic):
    def __init__(self, service, uuid, properties):
        Characteristic.__init__(self, service, uuid, properties)
        self.value = ""

    def read(self, _):
        return self.value.encode()

    def write(self, data):
        global current_color, current_glimpse
        self.value = data.decode()
        print(f"Characteristic value updated: {self.value}")

        # Обновление глобальных переменных
        if 'R' in self.value or 'Y' in self.value or 'G' in self.value or 'W' in self.value:
            current_color = self.value
        elif '0' in self.value or '1' in self.value or '2' in self.value:
            current_glimpse = self.value

        if current_color and current_glimpse:
            combined_data = f"Color: {current_color}, Glimpse: {current_glimpse}"
            print(f"Publishing to MQTT: {combined_data}")
            client.publish("mqtt/color", current_color)
            client.publish("mqtt/glimpse", current_glimpse)

class MyService(Service):
    def __init__(self, peripheral):
        try:
            Service.__init__(self, peripheral, SERVICE_UUID)
        except ValueError as e:
            print(f"Error initializing Service: {e}")
            # Возможно, следует пересмотреть передачу параметров в Service
        self.characteristic = MyCharacteristic(self, CHARACTERISTIC_UUID,
                                                Characteristic.FLAG_READ | Characteristic.FLAG_WRITE)

class MyPeripheral(Peripheral):
    def __init__(self):
        Peripheral.__init__(self)
        self.addService(MyService(self))
        self.advertise_name = "BLEDevice_MQTT"

    def advertise(self, name):
        print(f'Advertising BLE device with name: {name}')

    def send_data(self, data):
        service = self.getServiceByUUID(SERVICE_UUID)
        characteristic = service.getCharacteristics(CHARACTERISTIC_UUID)[0]
        characteristic.write(data.encode())

def on_connect(client, userdata, flags, rc):
    print(f"Connected to MQTT broker with result code {rc}")
    for item in MQTT_TOPICS:
        if len(item) == 2:  # Убедитесь, что есть два значения для распаковки
            topic, qos = item
            client.subscribe((topic, qos))
        else:
            print(f"Invalid item: {item}")
    print(f"Subscribed to topics: {MQTT_TOPICS}")

def on_message(client, userdata, msg):
    global current_color, current_glimpse
    data = msg.payload.decode()
    
    if msg.topic == "mqtt/color":
        current_color = data
    elif msg.topic == "mqtt/glimpse":
        current_glimpse = data

    if current_color and current_glimpse:
        print(f"Publishing to BLE: {combined_data}")
        if hasattr(userdata, 'peripheral') and userdata.peripheral:
            userdata.peripheral.send_data(current_color)
            userdata.peripheral.send_data(current_glimpse)
        # TODO: Протестировать отправку данных.

class UserData:
    def __init__(self):
        self.peripheral = BLEServer()


def main():
    global client
    
    client.on_connect = on_connect
    client.on_message = on_message

    userdata = UserData()
    client.user_data_set(userdata)
    
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()

    peripheral = userdata.peripheral
    def ble_advertise():
        print("BLE server is advertising...")
        while True:
            peripheral.advertise(peripheral.advertise_name)
            time.sleep(1)
    ble_thread = threading.Thread(target=ble_advertise)
    ble_thread.start()

if __name__ == "__main__":
    main()
