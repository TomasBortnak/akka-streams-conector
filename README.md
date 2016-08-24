# akka-streams-conector


AKKA Streams connector (reactive stream)

- POPIS PRACOVANIA PROJEKTU

     - 	Projekt po spustení vytvorí dva moduly, 
	jeden sa nachádza v sample.stream.demo, 
	druhý v sample.stream.lib.http.
     - 	Sample.stream.demo má tri triedy: Main, 
	RESTFulServer a RESTFulClient. Main je 
	kalsický "main-ovský" súbor, slúžiaci 
	na spustenie. RESTFulServer je webový 
	server na localhoste: 8080 so servisom 
	/user/{id}, {id} je integer, vyjadruje 
	prístup clienta na http://localhost:8080/user/1
	alebo (0,2, ...), vracia JSON. 
     - 	RESTFulClient pristúpi k RESTFulServeru 
	použitím knižnice, v rovnakom priečinku 
	stream.lib.http.  (Volá sa REactiveStream,
	je to classa)

     - 	V rámci projektu je ukázané ako RESTFulClient
	pristupuje k RESTFulServeru použitím HTTP
	Reactive Stream. RESTFulServer je zámerne 
	spomalený  pomocou Thread.sleep, takže nevracia 
	JSON okamžite keď je volaný clientom, ale 
	stále až o 1,5 sekundy neskôr.

     - 	Client volá server okamžite 20 krát,volania 
	bežia jedno po druhom, takže requesty 
	sú rýchlejšie ako odpovede servera. Práve tam
	je možné vidieť prejavenie sa "back pressure" 
	pri reactive streams. Dáta sú dručované konštantne,
	aj keď je server spomalený. 

- ECOVERY/ZOTAVENIE

     -	V classe ReactiveStream je retry metoda. 
	Keď client pouije knižnicu na volanie servera, 
	no ten neodpovedá, použije retry metódu 
	na jeho opätovné volanie pričom sú odosielané
	dáta uložené v redis databáze, a tak sa predíde
	strate obsahu odosielaných správ. 

- POUZITIE

     -	Pre použitie treba zakomponovať do projektu, 
	v ktorom to chce používateľ použiť súbor 
	ResctiveStream z gitu. 

- SPUSTENIE 

     -	Pred sputením je potrebné loklne nainštalovať Redis. 
	Po stiahnutí projektu je potrebné spustiť activator
	(run ./activator na linuxe, podobne aj na MacOS 
	by to malo byť takto). Po skompletizovaní je potrebné 
	ešte raz v príkazovom riadku zadať "run" a potvrdiť to 
	Enterom.

