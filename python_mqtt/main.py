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
        Service.__init__(self, peripheral, SERVICE_UUID, True)
        self.characteristic = MyCharacteristic(self, CHARACTERISTIC_UUID,
                                                Characteristic.FLAG_READ | Characteristic.FLAG_WRITE)

class MyPeripheral(Peripheral):
    def __init__(self):
        Peripheral.__init__(self)
        self.addService(MyService(self))
        self.advertise_name = "BLEDevice_MQTT"

def on_connect(client, userdata, flags, rc):
    print(f"Connected to MQTT broker with result code {rc}")
    for topic, qos in MQTT_TOPICS:
        client.subscribe((topic, qos))
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

def main():
    # Set up MQTT client
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()

    # Set up BLE server
    peripheral = MyPeripheral()
    print("BLE server is advertising...")
    while True:
        peripheral.advertise(peripheral.advertise_name)
        time.sleep(1)

if __name__ == "__main__":
    main()
