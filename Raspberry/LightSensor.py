from gpiozero import LightSensor

# pin: number of pin where sensor is connected
# thr: value above that we consider there is sufficient light (need to be calculated based on sensor type and capacitor dimension)
# GPIO 17 = pin 11
class MyLightSensor:
    def __init__(self, pin=17, thr=0.1):
        self.ldr = LightSensor(pin=pin, threshold=thr)

    def getValue(self):
        return self.ldr.value
