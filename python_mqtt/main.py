import paho.mqtt.client as mqtt
from bleak import BleakClient
import asyncio

# MQTT settings
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
MQTT_TOPICS = [("mqtt/color", 0), ("mqtt/glimpse", 0)]  

# BLE settings
BLE_DEVICE_ADDRESS = "11:11:12:12:11:12"  
BLE_CHARACTERISTIC_UUID = "00002a37-0000-1000-8000-00805f9b34fb"  

client = mqtt.Client()

# Global variables to hold the current color and glimpse values
current_color = None
current_glimpse = None

async def send_ble_data(data):
    try:
        async with BleakClient(BLE_DEVICE_ADDRESS) as ble_client:
            await ble_client.write_gatt_char(BLE_CHARACTERISTIC_UUID, data.encode())
            print(f"Sent data to BLE device: {data}")
    except Exception as e:
        print(f"Failed to send data to BLE device: {e}")

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
    
    if current_color is not None and current_glimpse is not None:
        #combined_data = f"Color: {current_color}, Glimpse: {current_glimpse}"
        print(f"Received message: {msg.topic} {data}")
        asyncio.run(send_ble_data(current_color))
        asyncio.run(send_ble_data(current_glimpse))

async def handle_ble_notification(sender, data):
    message = data.decode()
    print(f"Received data from BLE device: {message}")

    if 'R' or 'Y' or 'G' or 'W' in message:  # Данные которые пришли по BLE. 
        client.publish("mqtt/color", message)
        print(f"Published {message} to mqtt/color")
    elif '0' or '1' or '2' in message:
        client.publish("mqtt/glimpse", message)
        print(f"Published {message} to mqtt/glimpse")

async def listen_ble_notifications():
    try:
        async with BleakClient(BLE_DEVICE_ADDRESS) as ble_client:
            await ble_client.start_notify(BLE_CHARACTERISTIC_UUID, handle_ble_notification)
            print("Listening for BLE notifications...")
            while True:
                await asyncio.sleep(1)
    except Exception as e:
        print(f"Failed to listen for BLE notifications: {e}")

def main():
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()  

    loop = asyncio.get_event_loop()
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        print("Exiting program...")
    finally:
        client.loop_stop()
        client.disconnect()

if __name__ == "__main__":
    main()
