package com.virtualpairprogrammers.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.virtualpairprogrammers.data.VehicleRepository;
import com.virtualpairprogrammers.domain.Vehicle;
import com.virtualpairprogrammers.services.PositionTrackerExternalMicroService;

@Controller
@Transactional
@RequestMapping("/website/vehicles")
public class VehicleController {
	@Autowired
	private VehicleRepository data;

	/*
	 * @Autowired private DiscoveryClient discoveryService;
	 */

	/*
	 * @Autowired private LoadBalancerClient loadbBalancer;
	 */

	@Autowired
	private PositionTrackerExternalMicroService positionTrackerExternalMicroService;

	@RequestMapping(value = "/newVehicle.html", method = RequestMethod.POST)
	public String newVehicle(Vehicle vehicle) {
		data.save(vehicle);
		return "redirect:/website/vehicles/list.html";
	}

	@RequestMapping(value = "/deleteVehicle.html", method = RequestMethod.POST)
	public String deleteVehicle(@RequestParam Long id) {
		data.delete(id);
		return "redirect:/website/vehicles/list.html";
	}

	@RequestMapping(value = "/newVehicle.html", method = RequestMethod.GET)
	public ModelAndView renderNewVehicleForm() {
		Vehicle newVehicle = new Vehicle();
		return new ModelAndView("newVehicle", "form", newVehicle);
	}

	@RequestMapping(value = "/list.html", method = RequestMethod.GET)
	public ModelAndView vehicles() {
		List<Vehicle> allVehicles = data.findAll();
		return new ModelAndView("allVehicles", "vehicles", allVehicles);
	}

	@RequestMapping(value = "/vehicle/{name}")
	public ModelAndView showVehicleByName(@PathVariable("name") String name) {
		Vehicle vehiclesFromDB = data.findByName(name);

		Position latestPosition = positionTrackerExternalMicroService
				.getLatestPositionForVehicleFromPositionTrackerRemoteMicroService(name);

		if(latestPosition.isUpToDate()) {
			vehiclesFromDB.setLat(latestPosition.getLat());
			vehiclesFromDB.setLongitude(latestPosition.getLongitude());
			vehiclesFromDB.setLastRecordedPosition(latestPosition.getTimestamp());
			//this is dirty check and vehiclesFromDB changes will be saved to database
		}
		
		
		Map<String, Object> model = new HashMap<>();
		model.put("vehicle", vehiclesFromDB);
		model.put("position", latestPosition);
		return new ModelAndView("vehicleInfo", "model", model);
	}

	/* old implenetation */
	/*
	 * @RequestMapping(value="/vehicle/{name}") public ModelAndView
	 * showVehicleByName(@PathVariable("name") String name) { Vehicle vehicle =
	 * data.findByName(name);
	 * 
	 * // get the current position for this vehicle from the microservice
	 * RestTemplate rest = new RestTemplate();
	 * 
	 * 
	 * // This code is suitable for DiscoveryClient service with one position
	 * tracker running //List<ServiceInstance>
	 * instances=discoveryService.getInstances("fleetman-position-tracker");
	 * //if(instances.isEmpty()) { // this means service is down as crashed // throw
	 * new RuntimeException("this means service is down as crashed"); //}
	 * //ServiceInstance firstInstance=instances.get(0); //String
	 * positionTrackerWebUrl=firstInstance.getUri().toString();
	 * 
	 * 
	 * // this code is useful when mutiple position tracker micro services are
	 * registered with eureka // here we are using ribbon as loadbalancer
	 * implementation, to balance traffic to position trackers. // Instance are
	 * assign for each call in round robbin fashion
	 * 
	 * ServiceInstance positionTrackerMicroServiceInstance=loadbBalancer.choose(
	 * "fleetman-position-tracker"); if(positionTrackerMicroServiceInstance == null
	 * ) { // this means service is down as crashed throw new
	 * RuntimeException("this means service is down , or may have crashed"); }
	 * String
	 * positionTrackerWebUrl=positionTrackerMicroServiceInstance.getUri().toString()
	 * ;
	 * 
	 * //TODO will be improved with Fein
	 * 
	 * // this is rest call Position position =
	 * rest.getForObject(positionTrackerWebUrl+ "/vehicles/" + name,
	 * Position.class);
	 * 
	 * 
	 * 
	 * Map<String,Object> model = new HashMap<>(); model.put("vehicle", vehicle);
	 * model.put("position", position); model.put("microServicePort",
	 * positionTrackerMicroServiceInstance.getPort()); return new
	 * ModelAndView("vehicleInfo", "model",model); }
	 */

}
