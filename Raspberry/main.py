import time
from HumidityTemperatureSensor import HumidityTemperatureSensor

dht11 = HumidityTemperatureSensor()

while True:
	print("Temperature: " + str(dht11.getTemperature()))
	print("Humidity: " + str(dht11.getHumidity()))
	time.sleep(10)
