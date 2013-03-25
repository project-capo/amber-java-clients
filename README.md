amber-java-clients
==================

To repozytorium zawiera kod bibliotek klienckich używanych do sterowania robota mobilnego pracującego pod kontrolą platformy [Amber](https://github.com/kgadek/Amber).

## Obsługiwane urządzenia

W obecnej wersji dostępna jest tylko biblioteka do obsługi sterownika silników Roboclaw. Zawiera wyłącznie komendę sterowania prędkością obrotową silników. 


## Sterownik silników Roboclaw

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
