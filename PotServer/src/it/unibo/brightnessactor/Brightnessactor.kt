/* Generated by AN DISI Unibo */ 
package it.unibo.brightnessactor

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Brightnessactor ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		 
				lateinit var SENSOR_ID : String
				val CURRENT_PLANT = `it.greengers`.potserver.core.CurrentPlant.currentPlant
				lateinit var SENSOR : `it.greengers`.potserver.sensors.InputSensor<Double>
				var CURR_VALUE : Double = 0.0
				var POLLING_TIME : Long = 5000
				var ERROR_FOUND = false
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						println("TemperatureActor: started")
					}
					 transition(edgeName="t16",targetState="getSensor",cond=whenEvent("loadComplete"))
				}	 
				state("getSensor") { //this:State
					action { //it:State
						 
									ERROR_FOUND = false
									val loadId = `it.greengers`.potserver.sensors.SensorFactory.getMainId(`it.greengers`.potserver.sensors.SensorType.BRIGHTNESS)
									if(loadId == null) {
										ERROR_FOUND = true
						forward("sensorError", "sensorError(BRIGHTNESSACTOR,NO_ID_FOUND)" ,"manageractor" ) 
						
									} else {
										SENSOR_ID = loadId!!
									
										val loadSensor = `it.greengers`.potserver.sensors.SensorFactory.getSensor(SENSOR_ID) as `it.greengers`.potserver.sensors.InputSensor<Double>
										if(loadSensor == null) {
						forward("sensorError", "sensorError($SENSOR_ID,ERROR_LOADING)" ,"manageractor" ) 
						
										}  else {
											SENSOR = loadSensor!!
										}
						forward("sensorStarted", "sensorStarted($SENSOR_ID)" ,"manageractor" ) 
						 }  
					}
					 transition( edgeName="goto",targetState="work", cond=doswitchGuarded({ ERROR_FOUND == false  
					}) )
					transition( edgeName="goto",targetState="exit", cond=doswitchGuarded({! ( ERROR_FOUND == false  
					) }) )
				}	 
				state("work") { //this:State
					action { //it:State
						println("$name | Working")
						stateTimer = TimerActor("timer_work", 
							scope, context!!, "local_tout_brightnessactor_work", POLLING_TIME )
					}
					 transition(edgeName="t017",targetState="polling",cond=whenTimeout("local_tout_brightnessactor_work"))   
					transition(edgeName="t018",targetState="polling",cond=whenEvent("polling"))
					transition(edgeName="t019",targetState="handleReadRequest",cond=whenRequest("sensorRead"))
					transition(edgeName="t020",targetState="exit",cond=whenEvent("criticalErr"))
				}	 
				state("polling") { //this:State
					action { //it:State
						 CURR_VALUE = SENSOR.read()  
						println("$name | Read brightness from sensor [$CURR_VALUE]")
						emit("sensorValue", "sensorValue($SENSOR_ID,$CURR_VALUE)" ) 
						 if(CURRENT_PLANT.optimalPlantCondition.brightnessRange.isOutOfRange(CURR_VALUE)) {  
						emit("valueOutOfRange", "valueOutOfRange($SENSOR_ID,$CURR_VALUE)" ) 
						 }  
					}
					 transition( edgeName="goto",targetState="work", cond=doswitch() )
				}	 
				state("handleReadRequest") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("sensorRead(SENSOR_ID)"), Term.createTerm("sensorRead(SENSOR_ID)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("$name in ${currentState.stateName} | $currentMsg")
								 if(payloadArg(0) == SENSOR_ID) {
													CURR_VALUE = SENSOR.read()
								println("$name | Read brightness from sensor [$CURR_VALUE]")
								answer("sensorRead", "readedValue", "readedValue($SENSOR_ID,$CURR_VALUE)"   )  
								 }  
						}
					}
					 transition( edgeName="goto",targetState="work", cond=doswitch() )
				}	 
				state("exit") { //this:State
					action { //it:State
						println("$name | exit")
					}
				}	 
			}
		}
}
