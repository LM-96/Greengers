%====================================================================================
% potserver description   
%====================================================================================
context(ctxpotserver, "localhost",  "TCP", "9000").
 qactor( manageractor, ctxpotserver, "it.unibo.manageractor.Manageractor").
  qactor( temperatureactor, ctxpotserver, "it.unibo.temperatureactor.Temperatureactor").
  qactor( humidityactor, ctxpotserver, "it.unibo.humidityactor.Humidityactor").
  qactor( brightnessactor, ctxpotserver, "it.unibo.brightnessactor.Brightnessactor").
  qactor( batteryactor, ctxpotserver, "it.unibo.batteryactor.Batteryactor").
