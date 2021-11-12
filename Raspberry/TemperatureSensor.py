from gpiozero import MCP3008

class MyTemperatureSensor:
    def __init__(self, channel = 0):
        self.mcp = MCP3008(channel = channel)

    # Vaule goes from 0 to 1, so we need to put here formula to obtain temperature
    def getTemperature():
        return self.mcp.value