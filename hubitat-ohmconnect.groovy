/**
 *
 *  Hubitat OhmConnect Ohm Hour Virtual Switch
 *  Copyright 2023 Scott Deeann Chen 
 *
 */ 

 // 1. Login to 
 // 2. Navigate to https://login.ohmconnect.com/api/v2/settings in the broswer
 // 3. Take $USER_HASH from "verify_ohm_hour_url":"https://login.ohmconnect.com/verify-ohm-hour/$USER_HASH"
 // 4. Enter $USER_HASH into the device preferenes
 // 5. Enjoy

metadata {
    definition (name: "OhmConnect Ohm Hour Virtual Switch", namespace: "sdachen.ohmconnect", author: "Scott Deeann Chen") {
        capability "Switch"
        capability "Polling"
        command "refresh"
	}
    
	preferences {
        input name: "userHash", type: "string", title: "User Hash", defaultValue: "", required: true
        input name: "debug", type: "bool", title: "Debug", defaultValue: false, required: true
    }
}

def on(){
    sendEvent(name: "switch", value: "on")
}

def off(){
    sendEvent(name: "switch", value: "off")
}

def installed(){
	initialize()
}

def initialize(){
	off()
    updated()
}

def updated(){
    unschedule()

    // fetch 30 seconds past every 30 mins
    schedule('30 0,30 * ? * *', fetch)
    log.debug "Scheduled..."
}

def poll(){
    fetch()   
}

def refresh(){
    fetch()
}

void fetch() {
	def params = [
	  uri:  "https://login.ohmconnect.com",
	  path: "/verify-ohm-hour/${userHash}"
	]

    asynchttpGet("processResponse", params)
}

void processResponse(response, data) {
    boolean isOhmHour = response.getXml().text().endsWith('True')
    String currentState = device.currentValue("switch")

    if (isOhmHour && currentState == "off") {
        on()
        if (debug) log.debug "Ohm was off and is now turned on."
    } 
    
    if (!isOhmHour && currentState == "on") {
        off()
        if (debug) log.debug "Ohm was on and is now turned off."
    }
}