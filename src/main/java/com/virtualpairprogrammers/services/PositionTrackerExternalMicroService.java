package com.virtualpairprogrammers.services;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.virtualpairprogrammers.controllers.Position;
import com.virtualpairprogrammers.data.VehicleRepository;
import com.virtualpairprogrammers.domain.Vehicle;

@Service
public class PositionTrackerExternalMicroService {

	@Autowired
	private LoadBalancerClient loadbBalancer;
	
	@Autowired
	private VehicleRepository vehicleRepository;
	
	@HystrixCommand(fallbackMethod = "handlePositionTrackerMicroserviveDown")
	/*@HystrixCommand(fallbackMethod = "handlePositionTrackerMicroserviveDown",
    commandProperties= {@HystrixProperty(name="execution.isolation.strategy",value="SEMAPHORE")})
	//@Transactional(propagation=Propagation.NOT_SUPPORTED) // this is automatically added by hystrix */
	public Position getLatestPositionForVehicleFromPositionTrackerRemoteMicroService(String vehicleName) {

		System.out.println("** Entering getLatestPositionForVehicleFromPositionTrackerRemoteMicroService");
		RestTemplate rest = new RestTemplate();
		ServiceInstance positionTrackerMicroServiceInstance = loadbBalancer.choose("fleetman-position-tracker");
		
		if (positionTrackerMicroServiceInstance == null) {
			throw new RuntimeException("this means service is down , or may have crashed");
		}
		String positionTrackerWebUrl = positionTrackerMicroServiceInstance.getUri().toString();
		Position position = rest.getForObject(positionTrackerWebUrl + "/vehicles/" + vehicleName, Position.class);
		
		System.out.println("** Returning SUCCESSFUL position response");
		position.setUpToDate(true);
		
		return position;
	}
	
	
	public Position handlePositionTrackerMicroserviveDown(String vehicleName){
		
		//System.out.println("** Position tracker service called is DOWN , in fallback method");
		// instead dummy data ,bring from lastposition data of the vehicle from database.
		Vehicle vehiclesFromDB=vehicleRepository.findByName(vehicleName);
		
		Position position=new Position();
		position.setLat(vehiclesFromDB.getLat());
		position.setLongitude(vehiclesFromDB.getLongitude());
		position.setTimestamp(vehiclesFromDB.getLastRecordedPosition());
		//because it is from database , not from service success , set it position to upToDate as false
		position.setUpToDate(false);
		System.out.println("** Fallback Returning dummy position object");
		
		return position;
	}
	
	/*Back up ofImplementation of Hystrix*/
	/*@HystrixCommand(fallbackMethod = "handlePositionTrackerMicroserviveDown")
	public Position getLatestPositionForVehicleFromPositionTrackerRemoteMicroService(String vehicleName) {

		System.out.println("** Entering getLatestPositionForVehicleFromPositionTrackerRemoteMicroService");
		// get the current position for this vehicle from the microservice
		RestTemplate rest = new RestTemplate();

		// this code is useful when mutiple position tracker micro services are registered with eureka
		// here we are using ribbon as loadbalancer implementation, to balance traffic to position trackers.
		// Instance are assign for each call in round robbin fashion
		//System.out.println("** Getting information from Eureka of fleetman-position-tracker");
		ServiceInstance positionTrackerMicroServiceInstance = loadbBalancer.choose("fleetman-position-tracker");
		
		if (positionTrackerMicroServiceInstance == null) {
			// this means service is down as crashed
			throw new RuntimeException("this means service is down , or may have crashed");
		}
		String positionTrackerWebUrl = positionTrackerMicroServiceInstance.getUri().toString();

		// TODO will be improved with Fein
		// this is rest call
		//System.out.println("** Making Rest call to fleetman-position-tracker  with port "+positionTrackerMicroServiceInstance.getPort());
		
		Position position = rest.getForObject(positionTrackerWebUrl + "/vehicles/" + vehicleName, Position.class);
		System.out.println("** Returning SUCCESSFUL position response");
		
		return position;
	}*/
	
}
