from HumidityTemperatureSensor import HumidityTemperatureSensor

dht11 = HumidityTemperatureSensor()
dht11.read()
print(dht11.getTemperature())