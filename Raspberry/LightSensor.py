from gpiozero import LightSensor

# pin: number of pin where sensor is connected
# thr: value above that we consider there is sufficient light (need to be calculated based on sensor type and capacitor dimension)
class MyLightSensor:
    def __init__(self, pin, thr=0.1):
        self.ldr = LightSensor(pin=pin, threshold=thr)

    def isBright():
        return self.ldr.light_detect()

    def isDark():
        return not self.ldr.light_detect()