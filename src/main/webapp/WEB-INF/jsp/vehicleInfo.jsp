<!DOCTYPE html>
<html>
  <head>
    <style>
#map {
        width: 100%;
        height: 800px;
     }
    </style>
  </head>
  <body>
	<h1>Vehicle Info for ${model.vehicle.name}</h1>
	
	<p>The last report for this vehicle was at ${model.position.timestamp}</p>
	
	<p>It was at lat long ${model.position.lat}, ${model.position.longitude}</p>
	
	<p>The position of vehicle is obtained from  position tracker microservice  running on port ${model.microServicePort}</p>

    <div id="map"></div>
    <script>
      function initMap() {
        var myLatLng = {lat: ${model.position.lat}, lng: ${model.position.longitude}};
      
        var mapDiv = document.getElementById('map');
        var map = new google.maps.Map(mapDiv, {
            center: myLatLng,
            zoom: 14
        });
        
          var marker = new google.maps.Marker({
    		position: myLatLng,
    		map: map,
    		title: '${model.position.timestamp}'
  		  });               
      }
      
   
    </script>
    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAV9ajYP7G-jlDGH30DzC8jRbH_5kJxuSg&callback=initMap">
    </script>
  </body>
</html>