<?php
//ukljucivanje klijenta za redis
require "predis/autoload.php";
Predis\Autoloader::register();

try {
	$redis = new Predis\Client();

}
catch (Exception $e) {
	die($e->getMessage());
}
// echo "proba";

$redis->set('message', 'Hello world');

//pamcenje ulogovanih korisnika i njihove lokacije

$usersL = array();
$usersL['1'] = array();
$usersL['1']['lat'] = "43.581751";
$usersL['1']['lon'] = "21.328264";

$usersL['7'] = array();
$usersL['7']['lat'] = "43.580461";
$usersL['7']['lon'] = "21.324166";


$redis->set('1', json_encode($usersL['1']));

$redis->set('7', json_encode($usersL['7']));





header("HTTP/1.1 200 OK");



$request = json_decode($_POST['req'],true);

//echo($user);

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "mosis_app";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);



// Check connection
if (mysqli_connect_error()) {
    echo("Connection failed: " . $conn->connect_error);
} 
//echo "Connected successfully";
 
 

//$sql = "SELECT * from User";
//echo $sql;





//print_r($result);
switch($request){
case 1: 
$user = json_decode($_POST['user'],true);

$sql = "SELECT * FROM User WHERE username='" . $user["username"] ."' AND password='" . $user["password"] . "'";
$result = mysqli_query($conn, $sql); 
	if($result != null){
		if ($result->num_rows > 0) {
		

			// output data of each row
			$user_fetched = $result->fetch_assoc();
			
			
			//$users[$user_fetched["id"]] = $user_fetched;
			//$users[$user_fetched["id"]]["lat"] = json_decode($_POST['lat'],true)["lat"];
			//$users[$user_fetched["id"]]["lon"] = json_decode($_POST['lon'],true)["lon"];
			$id = $user_fetched["id"];
			$usersL[$id] = array();
			$usersL[$id]['lat'] = json_decode($_POST['lat'],true);
			$usersL[$id]['lon'] = json_decode($_POST['lon'],true);
			
			//print_r($usersL);
			echo json_encode($user_fetched);
			//echo $users[$user_fetched["id"]]["latitude"];
			
			 
			
			
			
			


		} else {
			echo "Login error, please try again!";
		}
	}
	else{
		echo "Login error, please try again!";
	}
break;
case 2: //REGISTER
$user = json_decode($_POST['user'],true);

$sql = "SELECT * FROM User WHERE username='" . $user["username"] . "'";
//echo $sql;
$result = mysqli_query($conn, $sql); 
//echo mysqli_num_rows($result);
if(mysqli_num_rows($result) > 0){
	//if ($result->num_rows > 0) {
			// output data of each row
			//$user_fetched = $result->fetch_assoc();
			echo "That username is taken, try another!";

	/*	} else {
		
			echo "Register successful!";
		} */
		
	}
	else{
	
	$sql = "INSERT INTO USER (username, password, firstname, lastname, phonenumber, userphoto)
            VALUES ('" . $user["username"] . "', '" . $user["password"] . "', '" . $user["firstname"] . "', '" .  $user["lastname"]  . "', '" .  $user["phonenumber"] . "', '" .  $user["userphoto"] . "')";
			 
			// echo $sql;
			 
			 $result = mysqli_query($conn, $sql); 

			 if($result == TRUE){
		echo "Success";
		}
		else {
		echo "Failed";
		}
		
	}

break;
case 3: //ADD_EVENT
//echo $_POST['event'];
$event_json = $_POST['event'];
$event = json_decode($event_json ,true);

	$sql = "INSERT INTO EVENT (name, description, type, number_of_players, location, user_id, datetime) VALUES ('" . $event["name"] . "', '" . $event["description"] .  "', '" . $event["type"] .  "', '" . $event["number_of_players"] .  "', ST_GeomFromText( 'POINT(" . $event['latitude'] . " " . $event['longitude'] . ")'), '" . $event['user_id']   . "', '" . $event['datetime'] . "')";
			 
			 //echo $sql;
			 
			 
			 
			 $result = mysqli_query($conn, $sql); 
			 
			 $event["id"] = mysqli_insert_id($conn);//vraca poslednji auto-increment koji oznacava id event-a

			 if($result == TRUE){
			 
		
			 //dodavanje eventa i njegove lokacije u redis, radi kasnije provere radiusa
			// $redis->geoadd("events_locations", $event['latitude'], $event['longitude'], json_encode($event));
			$redis->geoadd("events_locations", $event['latitude'], $event['longitude'], $event["id"]);
			 
			 //dodavanje radi provere da li je aktivan
			 $redis->hset("events_active",  mysqli_insert_id($conn), "p");//proveriti slucaj kada je baza prazna
			 
			 
			 //da bi se dodalo u redis sve sto je potrebno
			 $sql = "SELECT * FROM User WHERE id='" . $event["user_id"] . "'";
//echo $sql;
			$result = mysqli_query($conn, $sql);
			 


			$user_fetched = $result->fetch_assoc();
			

						 $data = array();
						$data['userId'] = $event['user_id'];
						$data['username'] = $user_fetched["username"];



			 //e od event, p od players//pamti se u redisu za taj event da je korisnik prijavljen 
$ret1 = $redis->hset("e" . $event["id"] . "p", $event["user_id"], json_encode($data));//??//hash tabela korisnika koji su prijavljeni za event

$ret2 = $redis->hset("p" . $event["user_id"] . "e", $event["id"], json_encode($event));//hash tabela u redisu u kojoj se za tog korisnika
																	    //pamti za koje evente je prijavljen
			 
			 
			 
		echo json_encode($event);}
		else {
		echo "Adding unsuccessful, try again!";
		}
break;	
case 4: //ADD_FRIEND  //insert into friend (user_id, friend_id) values ('3', '4'), ('4', '3')
$friend_id = $_POST['friend_id'];
$user_id = $_POST['user_id'];

	$sql = "INSERT INTO FRIEND (user_id, friend_id) VALUES ('" . $user_id . "', '" . $friend_id .  "'), " . 
	         "('" . $friend_id . "', '" . $user_id .  "')" ;
			 
			// echo $sql;
			 
			 $result = mysqli_query($conn, $sql); 

			 if($result == TRUE){
		//echo "Friend added successfully";
		$sql = "SELECT * FROM User WHERE id='" . $friend_id . "'";
		//echo $sql;
		$result = mysqli_query($conn, $sql); 
		
		$ret = $result->fetch_assoc();
		
		
		echo json_encode($ret);
		}
		else {
		echo "Adding unsuccessful, try again!";
		}
break;		
case 5:


	$sql = "SELECT id, name, description, type, number_of_players, ST_AsText(location) as location, user_id FROM EVENT";
			 
			// echo $sql;
			 
			 $result = mysqli_query($conn, $sql); 
			//print_r($result);
			 if(mysqli_num_rows($result) > 0){
		
		//$events_fetched = $result->fetch_assoc();
		$events = array();
		
		while($row = $result->fetch_assoc())
		{
		//print_r($row);
			$location = GetBetween("(", ")", $row["location"]);
			list($latitude, $longitude) = explode(" ", $location);
			$event["id"] = $row["id"];
			$event["name"] = $row["name"];
			$event["description"] = $row["description"];
			$event["type"] = $row["type"];
			$event["number_of_players"] = $row["number_of_players"];			
			$event["latitude"] = $latitude;
			$event["longitude"] = $longitude;
			$event["user_id"] = $row["user_id"];
			//echo json_encode($event);
			$events[] = $event;
		}
		
		
		
		echo json_encode($events);}
		else {
		echo "Adding unsuccessful, try again!";
		}
break;

case 6://GET_FRIENDS(and signed events)
$id = json_decode($_POST['id'],true);
$sql = "SELECT * FROM user WHERE id IN ( SELECT friend_id FROM friend WHERE user_id = '" . $id . "')";
			 
			// echo $sql;
			 
			 $result = mysqli_query($conn, $sql); 
			//print_r($result);
			 if(mysqli_num_rows($result) > 0){
		
		$friends = array();
		
		while($row = $result->fetch_assoc())
		{
		//print_r($row);
			$friends[] = $row;
		}

		$ret = array();
		$ret["friends"] = $friends;
		
		
		//preuzimanje evenata na koje je korisnik prijavljen
		
		$signed_events_json = $redis->hvals("p" . $id . "e");
		
	
		$signed_events = array();
		foreach($signed_events_json as $s){
		$signed_events[] = json_decode($s);
		}
	
		
		$ret["events"] = $signed_events;
		
		echo json_encode($ret);}
		else {
		echo "Adding unsuccessful, try again!";
		}




break;


case 7://GET_FRIENDS


$id = json_decode($_POST['id'],true);
$lat = json_decode($_POST['lat'],true);
$lon = json_decode($_POST['lon'],true);

//postavljanej korisnikove lokacije
$location = array();
$location['lat'] = $lat;
$location['lon'] = $lon;
//$usersL[$id]['lat'] = $lat;
//$usersL[$id]['lon'] = $lon;

$redis->set($id, json_encode($location));



//postavljanje korisnikove lokacije radi kasnije pretrage koriscenjem georadiusbymember

 $redis->geoadd("users_locations", $lat, $lon, $id);

$sql = "SELECT id FROM user WHERE id IN ( SELECT friend_id FROM friend WHERE user_id = '" . $id . "')";
			 
			// echo $sql;
			 
			 $result = mysqli_query($conn, $sql); 
			//print_r($result);
			 if(mysqli_num_rows($result) > 0){
		
		$friends = array();
		
		while($row = $result->fetch_assoc())
		{
		//print_r($row);
		$id = $row['id'];
		//echo $id;
			$id = $row['id'];
			//echo $redis->get($id);
			$loc = json_decode($redis->get($id));
			if($loc != null){
			//echo "ok";
			$friend = array();
			$friend['id'] = $id;
			$friend['lat']= $loc->lat;
			$friend['lon']= $loc->lon;
			
			$friends[]= $friend;

			}  
		}

		
		
		echo json_encode($friends);}
		else {
		echo "Adding unsuccessful, try again!";
		}




break;
case 8://GET_DATA_FOR_VIEWEVENT


$userId = $_POST['userId'];
$eventId = $_POST['eventId'];
$num_of_mes_user = $_POST['num_of_mes'];


$check = $redis->hget("events_active", $eventId);


//za poruke (chat)             //e od event, c od chat
$num_of_mes_db = $redis->llen("e" . $eventId . "c");

//preuzimanje novih poruka u redisu
$messages_json = array();
if($num_of_mes_db != $num_of_mes_user){
$messages_json = $redis->lrange("e" . $eventId . "c", 0, $num_of_mes_db - $num_of_mes_user - 1);


}
else{
$messages_json = [];
}

//preuzimanje prijavljenih korisnika
$tmp = array(); //
$tmp = $redis->hVals("e" . $eventId . "p");

$return = array();
$return['messages'] = $messages_json;
$return['players'] = $tmp;


if($check == null){
	$return['finished'] = "yes";
}


echo json_encode($return);





break;

case 9://_SENT_CHAT_MESSAGE
$eventId = $_POST['eventId'];
$username = $_POST['username'];
$message = $_POST['message'];

$data = array();
$data['username'] = $username;
$data['message'] = $message;
$ret = $redis->lpush("e" . $eventId . "c", json_encode($data));
if($ret == 1)
echo "Success";
else echo "Failed";
break;

case 10: //SIGN_UP_FOR_EVENT
$eventId = $_POST['eventId'];
$username = $_POST['username'];
$userId = $_POST['userId'];
$event = $_POST['event'];

$data = array();
$data['userId'] = $userId;
$data['username'] = $username;

							//e od event, p od players//pamti se u redisu za taj event da je korisnik prijavljen 
$ret1 = $redis->hset("e" . $eventId . "p", $userId, json_encode($data));//??//hash tabela korisnika koji su prijavljeni za event

$ret2 = $redis->hset("p" . $userId . "e", $eventId, $event);//hash tabela u redisu u kojoj se za tog korisnika
																	    //pamti za koje evente je prijavljen
if(($ret1 == 1 || $ret1 == 0) && ($ret2 == 1 || $ret2 == 0))
echo "Success";
else echo "Failed";
break;






case 11:
$eventId = $_POST['eventId'];
$userId = $_POST['userId'];

//$data = array();
//$data['userId'] = $userId;
//$data['username'] = $username;//????????????????????????

							//e od event, p od players//pamti se u redisu za taj event da je korisnik prijavljen 
//$ret1 = $redis->hset("e" . $eventId . "p", $userId, json_encode($data));//??//hash tabela korisnika koji su prijavljeni za event

							//e od event, p od players
$ret1 = $redis->hdel("e" . $eventId . "p", $userId);//??

//$ret2 = $redis->hset("p" . $userId . "e", $eventId, $event);//hash tabela u redisu u kojoj se za tog korisnika
																	    //pamti za koje evente je prijavljen
																		
$ret2 = $redis->hdel("p" . $userId . "e", $eventId);

if(($ret1 == 1 || $ret1 == 0) && ($ret2 == 1 || $ret2 == 0))
echo "Success";
else echo "Failed";
break;

break;

case 12://Get ratings
$counter = $_POST['counter'];
$userId = $_POST['userId'];

//$data = array();
//$data['userId'] = $userId;
//$data['username'] = $username; //////???

//$redis->zAdd("ratings", 1, json_encode($data));
//$redis->zAdd("ratings", 2, json_encode($data));
//$redis->zAdd("ratings", 3, json_encode($data));
//$redis->zAdd("ratings", 4, json_encode($data));
							//e od event, p od players
if($counter != 0){
//$results = $redis->zRange("ratings", $counter*10-1, $counter*10+9, "withscores");//??
//$results1 = $redis->zRange("ratings", $counter*10-1, $counter*10+9);

$results = $redis->zRevRange("ratings", $counter*10-1, $counter*10+9, "withscores");
$results1 = $redis->zRevRange("ratings", $counter*10-1, $counter*10+9);
}
else{
//$results = $redis->zRange("ratings", 0, 9, "withscores");
//$results1 = $redis->zRange("ratings", 0, 9);

$results = $redis->zRevRange("ratings", 0, -1, "withscores");

$results1 = $redis->zRevRange("ratings", 0, -1);
}

$players = array();
foreach($results1 as $r){
$sql = "SELECT * FROM user WHERE id = '" . $r . "'";
			 
			// echo $sql;
			 
			 $result = mysqli_query($conn, $sql);
			 
			 if(mysqli_num_rows($result) > 0){
			 $row = $result->fetch_assoc();
			// echo $row;
			 $player["userId"] = $row["id"];
			 $player["username"] = $row["username"];
			 
			 $players[] = $player;
			}
}


$ret = array();
$ret['scores'] = array_values($results);
$ret['players'] = $players;

echo json_encode($ret);


break;


case 13:
//SERVICE_EVENTS - poziva iz NotifyService-a, proverava da li u radiusu od 250m od korisnika postoje dogadjaji ili
//korisnici i vraca liste nadjenih dogadjaja ili korisnika 
$userId = $_POST['userId'];
$lat = $_POST['lat'];
$lon = $_POST['lon'];




$users = $redis->georadiusbymember("users_locations", $userId, 500, "m");//??




$events_ids = $redis->georadius("events_locations", $lat, $lon, 250, "m");

$users_ret = array();
foreach($users as $u){
$users_ret[] = json_decode($u);
}

$events_ret = array();
foreach($events_ids as $id){
//upiti za mysql bazu



$sql = "SELECT id, name, description, type, number_of_players, ST_AsText(location) as location, user_id from event WHERE id = '" . $id . "'";

$result = mysqli_query($conn, $sql); 

	    if(mysqli_num_rows($result) > 0){		
		
		$row = $result->fetch_assoc();
		
		$location = GetBetween("(", ")", $row["location"]);
			list($latitude, $longitude) = explode(" ", $location);
			$event["id"] = $row["id"];
			$event["name"] = $row["name"];
			$event["description"] = $row["description"];
			$event["type"] = $row["type"];
			$event["number_of_players"] = $row["number_of_players"];			
			$event["latitude"] = $latitude;
			$event["longitude"] = $longitude;
			$event["user_id"] = $row["user_id"];
			//echo json_encode($event);
			
			

		$events_ret[] = $event;
		}
		
		}



$return = array();
$return["users"] = $users_ret;
$return["events"] = $events_ret;

echo json_encode($return);


break;


case 14://GET_EVENTS 

$userId = $_POST['userId'];
$lat = $_POST['lat'];
$lon = $_POST['lon'];
$radius = $_POST['radius'];

$singedUpEventsIds = json_decode($_POST['singedUpEventsIds']);

$finishedEvents = array();


foreach($singedUpEventsIds as $id){
		$check = $redis->hget("events_active", $id);
		if($check == null){
		$finishedEvents[] = $id;
		}
}


$events_ids = $redis->georadius("events_locations", $lat, $lon, $radius, "km");
$events_ret = array();



foreach($events_ids as $id){
//upiti za mysql bazu 

$sql = "SELECT id, name, description, type, number_of_players, ST_AsText(location) as location, user_id FROM event WHERE id = '" . $id . "'";

$result = mysqli_query($conn, $sql); 

			 if(mysqli_num_rows($result) > 0){		
		
		$row = $result->fetch_assoc();
		
		$location = GetBetween("(", ")", $row["location"]);
			list($latitude, $longitude) = explode(" ", $location);
			$event["id"] = $row["id"];
			$event["name"] = $row["name"];
			$event["description"] = $row["description"];
			$event["type"] = $row["type"];
			$event["number_of_players"] = $row["number_of_players"];			
			$event["latitude"] = $latitude;
			$event["longitude"] = $longitude;
			$event["user_id"] = $row["user_id"];
		

		$events_ret[] = $event;
		}
		}


$ret["events"] = $events_ret; //eventi koji moraju da se prikazu na mapi
$ret["ids"] = $finishedEvents; //idijevi zavrsenih dogadjaja

echo json_encode($ret);

break;

case 15: //za pretrazivanje objekata na serveru (jedan kveri pretrazi naziv, kategoriju, opis)

$query = $_POST['query'];

$name= $_POST['name'];
$category= $_POST['category'];
$descripiton= $_POST['description'];

//dodati i %rec rec%  jos $rec $rec$ i rec$ ????????!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
if($name == "true"){
$sql = "SELECT id, name, description, type, number_of_players, ST_AsText(location) as location, user_id FROM event WHERE name LIKE '%" . $query . "%'";
}
else if ($category == "true"){
$sql = "SELECT id, name, description, type, number_of_players, ST_AsText(location) as location, user_id FROM event WHERE type LIKE '%" . $query . "%'" ;
}
else{
$sql = "SELECT id, name, description, type, number_of_players, ST_AsText(location) as location, user_id FROM event WHERE description LIKE '%" . $query . "%'";
}


	$result = mysqli_query($conn, $sql); 

			//print_r($result);
			 if(mysqli_num_rows($result) > 0){
		
		//$events_fetched = $result->fetch_assoc();
		$events = array();
		
		while($row = $result->fetch_assoc())
		{
		$location = GetBetween("(", ")", $row["location"]);
			list($latitude, $longitude) = explode(" ", $location);
			$event["id"] = $row["id"];
			$event["name"] = $row["name"];
			$event["description"] = $row["description"];
			$event["type"] = $row["type"];
			$event["number_of_players"] = $row["number_of_players"];			
			$event["latitude"] = $latitude;
			$event["longitude"] = $longitude;
			$event["user_id"] = $row["user_id"];
			//echo json_encode($event);
			$events[] = $event;
		}
		
		
		echo json_encode($events);
		}
		else {
		echo "No object was found!";
		}
		
break;

case 16://EVENT_FINISH

$eventId = $_POST["eventId"];

$event = $_POST["event"];

//dodavanje radi provere da li je aktivan
				$return = $redis->zrem("events_locations",$eventId);
			
			 
			 
	
			
			 $return = $redis->hdel("events_active",  $eventId);//proveriti slucaj kada je baza prazna
			 
			 
//if ($return != FALSE)
echo "Success";


break;

case 17://GET_PlAYERS_FOR_EVENT

$eventId = $_POST['eventId'];

$players1_json = $redis->hvals("e" . $eventId  . "p");

//print_r($players1_json);
$players = array();


foreach($players1_json as $p){
//echo $p;
$players_tmp= json_decode($p);


$sql = "SELECT * FROM user WHERE id = '" . $players_tmp->userId . "'";
			 
			// echo $sql;
			 
			 $result = mysqli_query($conn, $sql);
			 
			 if(mysqli_num_rows($result) > 0){
			 $row = $result->fetch_assoc();
			// echo $row;
			 $players[] = $row;
			}
}


		
		echo json_encode($players);

		

break;

case 18: //SUBMIT_PLAYER_RATINGS

$ratings = json_decode($_POST["ratings"]);
$ids = json_decode($_POST["ids"]);

for($x = 0; $x < count($ids); $x++){
$redis->zincrby("ratings", $ratings[$x], $ids[$x]);
}

echo "Success";

break;	

case 19://LOG_OFF
$id = $_POST['id'];

$result = $redis->zrem("users_locations", $id);

$redis->del($id);

if($result == 1)
echo "Success";
else echo "Failed";

break;	
		

}

$conn->close();


 function GetBetween($var1="",$var2="",$pool){
$temp1 = strpos($pool,$var1)+strlen($var1);
$result = substr($pool,$temp1,strlen($pool));
$dd=strpos($result,$var2);
if($dd == 0){
$dd = strlen($result);
}

return substr($result,0,$dd);
} 







?>

