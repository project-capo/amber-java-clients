amber-java-clients
==================

To repozytorium zawiera kod bibliotek klienckich używanych do sterowania robota mobilnego pracującego pod kontrolą platformy [Amber](https://github.com/kgadek/Amber).

## Gotowe paczki

Skompilowane pliki JAR wraz z przykładami można znaleźć tu. TODO...


## Kompilacja

Gdyby jednak naszła kogoś ochota na samodzielną kompilacją to do zbudowania pliku JAR z bibliotekami wymagane są:
- maven3
- protoc - kompilator plików Protocol Buffer, do pobrania [stąd](https://code.google.com/p/protobuf/)

Po sklonowaniu repozytorium należy wydać polecenie `mvn package` w głównym katalogu. Archiwa JAR pojawią się w katalogach `target/` poszczególnych projektów.


## Obsługiwane urządzenia

- Roboclaw - wyłącznie sterowanie prędkością obrotową silników,
- 9DOF - wersja eksperymentalna

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

// Zatrzymaj silniki
roboclawProxy.stopMotors();

client.terminate();
```
