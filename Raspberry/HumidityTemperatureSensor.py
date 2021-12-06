import Adafruit_DHT

DHT_SENSOR = Adafruit_DHT.DHT11
DHT_PIN = 4

# We use DHT11 sensor, main characteristic:
# - 20-90 humidity (%) +- 5%
# - 0-50 temperature (°C) +- 2°C

# Max sample rate 1Hz

class HumidityTemperatureSensor:
    def getTemperature(self):
        humidity, temperature = Adafruit_DHT.read_retry(DHT_SENSOR, DHT_PIN)
        return temperature

    def getHumidity(self):
        humidity, temperature = Adafruit_DHT.read_retry(DHT_SENSOR, DHT_PIN)
        return humidity
