amber-java-clients
==================

To repozytorium zawiera kod bibliotek klienckich używanych do sterowania robota mobilnego pracującego pod kontrolą platformy [Amber](https://github.com/kgadek/Amber).

## Gotowe paczki

Skompilowane pliki JAR wraz z przykładami można pobrać [stąd](http://amber.octanum.info/jars/).


## Kompilacja

Gdyby jednak naszła kogoś ochota na samodzielną kompilacją to do zbudowania pliku JAR z bibliotekami wymagane są:
- maven3
- protoc - kompilator plików Protocol Buffer, do pobrania [stąd](https://code.google.com/p/protobuf/)

Po sklonowaniu repozytorium należy wydać polecenie `mvn package` w głównym katalogu. Archiwa JAR pojawią się w katalogach `target/` poszczególnych projektów.


## Obsługiwane urządzenia

- Roboclaw - wyłącznie sterowanie prędkością obrotową silników,
- 9DOF - odczyt wartości ze wszystkich trzech sensorów.

## Schemat systemu

Na robotach działa automatycznie uruchamiany nasłuchujący proces mediatora z którym łączy się obiekt klasy `AmberClient`. Wraz z nim uruchomione są sterowniki, z którymi komunikują się odpowiednie proxy urządzeń. Program kliencki wykorzystujący umieszczone tu biblioteki klienckie w javie może zostać uruchomiony bezpośrednio na robocie lub zdalnie, na innej maszynie podłączonej do sieci w laboratorium robotów. Aby uzyskać dostęp do systemu na robocie należy skorzystać z protokołu ssh. 


## Sterownik silników Roboclaw

Przykładowy eclipsowy projekt można znaleźć w katalogu [examples/roboclaw_example](examples/roboclaw_example).

Poniżej przykład wykorzystania biblioteki obsługi sterownika silników. Program rozpędza powoli silniki, a potem je zatrzymuje. Dla przejrzystości została pominięta obsługa wyjątków.

```java
// Połącz z robotem
AmberClient client = new AmberClient("192.168.1.50", 26233);
RoboclawProxy roboclawProxy = new RoboclawProxy(client, 0);

// Powoli przyspieszaj
for (int i = 1; i <= 10; i++) {
  roboclawProxy.sendMotorsCommand(100 * i, 100 * i, 100 * i, 100 * i);
  
  Thread.sleep(500);
}

// Odczytaj aktualną prędkość kół
MotorsCurrentSpeed mcs = roboclawProxy.getCurrentMotorsSpeed();
mcs.waitAvailable();
			
System.out.println(String.format(
	"Motors current speed: fl: %d, fr: %d, rl: %d, rr: %d",				
	mcs.getFrontLeftSpeed(), mcs.getFrontRightSpeed(),
	mcs.getRearLeftSpeed(), mcs.getRearRightSpeed()));

// Zatrzymaj silniki
roboclawProxy.stopMotors();

client.terminate();
```

## Sensora 9DOF (akcelerometr, żyroskop i magnetometr)

Wartości podawane są w jednostkach:
- akcelerometr - mG (tysięczne części przyspieszenia ziemskiego)
- żyroskop - stopnie/minutę
- magnetometr - mGs (tysięczne części Gausa)

Przykładowy eclipsowy projekt można znaleźć w katalogu [examples/ninedof_example](examples/ninedof_example).

Poniżej przykład wykorzystania biblioteki obsługi sensora 9DOF. Program dokonuje odczytu wszystkich wartości najpierw sposób synchroniczny, a potem cykliczny, za pomocą listenerów. Dla przejrzystości została pominięta obsługa wyjątków.

```java
// Połącz z robotem
AmberClient client = new AmberClient("192.168.1.50", 26233);
NinedofProxy ninedofProxy = new NinedofProxy(client, 0);

// Odczyt synchroniczny
for (int i = 0; i < 10; i++) {

  // Pobieranie danych; w parametrach wybór sensorów, z których żądane są dane 
  NinedofData ninedofData = ninedofProxy.getAxesData(true, true, true);
  
  // Poczekaj na nadejście danych
  ninedofData.waitAvailable();
				
  // Wszystkie otrzymane dane są w strukturze ninedofData, poniżej wypisanie jednej wartości
  System.out.println(ninedofData.getAccel().xAxis)
    
  Thread.sleep(10);
}

// Odczyt w sposób cykliczny, z częstotliwością 10ms

// Rejestracja listera; w parametrach częstotliwość i wybór sensorów, z których żądane są dane
ninedofProxy.registerNinedofDataListener(10, true, true, true, new CyclicDataListener<NinedofData>() {
  			
  	@Override
  	public void handle(NinedofData ninedofData) {
    
      // Wszystkie otrzymane dane są w strukturze ninedofData, poniżej wypisanie jednej wartości
      System.out.println(ninedofData.getAccel().xAxis)
            
  	}
  });
			

client.terminate();
```

