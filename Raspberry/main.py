import time
from HumidityTemperatureSensor import HumidityTemperatureSensor
from LightSensor import MyLightSensor

dht11 = HumidityTemperatureSensor()
ldr = MyLightSensor()

while True:
	dht11.read()
	print("Temperature: " + str(dht11.getTemperature()))
	print("Humidity: " + str(dht11.getHumidity()))
	print("Luminosity: " + str(ldr.getValue()))
	time.sleep(10)
