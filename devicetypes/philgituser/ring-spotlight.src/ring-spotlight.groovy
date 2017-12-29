/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  On/Off Button Tile
 *
 *  Author: SmartThings
 *
 *  Date: 2013-05-01
 */
metadata {
	definition (name: "Ring Spotlight", namespace: "philgituser", author: "Philip") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "off"
		}
		main "button"
		details "button"
	}
    preferences {
        input "username", "email", title: "Ring Username", description: "Email used to login to Ring.com", displayDuringSetup: true, required: true
        input "password", "password", title: "Ring Password", description: "Password you login to Ring.com", displayDuringSetup: true, required: true
        //Not sure there is a better way to do this than ask? 
        input "deviceid", "text", title: "Device ID", description: "The numeric value that identifies the device", displayDuringSetup: true, required: true
    }
}

def parse(String description) {
}

def authenticate() 
{
	def s = "${username}:${password}"
	String encodedUandP = s.bytes.encodeBase64()
    
    def token = "EMPTY"
    def params = [
    	uri: "https://api.ring.com",
    	path: "/clients_api/session",
        headers: [
        	Authorization: "Basic ${encodedUandP}",
            "User-Agent": "iOS"
    	],
        requestContentType: "application/x-www-form-urlencoded",
        body: "device%5Bos%5D=ios&device%5Bhardware_id%5D=a565187537a28e5cc26819e594e28213&api_version=9"
	]

    try {
        httpPost(params) { resp ->
            log.debug "POST response code: ${resp.status}"
            
            log.debug "response data: ${resp.data}"
            token = resp.data.profile.authentication_token
        }
    } catch (e) {
        log.error "HTTP Exception Received on POST: $e"
        log.error "response data: ${resp.data}"
        return
        
    }
    
    log.debug "Authenticated, Token Found."
    return token
}

def on() {
	
    log.debug "Attempting to Switch On."
    def token = authenticate()
    //Send Command to Turn On
    def paramsforPut = [
    	uri: "https://api.ring.com",
    	path: "/clients_api/doorbots/${deviceid}/floodlight_light_on",
        query: [
        	api_version: "9",
            "auth_token": token
    	]
	]
    try {
        httpPut(paramsforPut) { resp ->
        }
    } catch (e) {
        //ALWAYS seems to throw an exception?
        //Platform bug maybe? 
        log.debug "HTTP Exception Received on PUT: $e"
    }
    sendEvent(name: "switch", value: "on")
}

def off() {

    log.debug "Attempting to Switch Off"
    def token = authenticate()
    
    //Send Command to Turn Off
    def paramsforPut = [
    	uri: "https://api.ring.com",
    	path: "/clients_api/doorbots/${deviceid}/floodlight_light_off",
        query: [
        	api_version: "9",
            "auth_token": token
    	]
	]
    try {
        httpPut(paramsforPut) { resp ->
            //log.debug "PUT response code: ${resp.status}"
        }
    } catch (e) {
        //ALWAYS seems to throw an exception?
        //Platform bug maybe? 
        log.debug "HTTP Exception Received on PUT: $e"
    }
	log.debug "Switched OFF!"
    sendEvent(name: "switch", value: "off")
}