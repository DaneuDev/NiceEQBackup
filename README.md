# NiceEQBackup
Jest to mały i pomocny plugin na zapis ekwipunku gracza oraz jego enderchestu. 
Jego wcześniejsza wersja była zrobiona dla serwera **NiceGame.GG** ([Link do DC ;3 Zapraszam!](https://discord.gg/nSF2tjZnTa "Link do DC ;3 Zapraszam!")) z mniejszą ilością funkcji (chociaż dalej nie ma ich za dużo). Potem postanowiłem poprawić ową wersję pluginu na lepszą i wystawić ją, za zgodą właściciela serwera, na Githuba. 

### Funkcje pluginu
- Zapis ekwipunku oraz enderchestu gracza (MySQL/SQLite, flat nie obsługiwany)
- Podgląd ewkipunków i enderchestow gracza offline
- Interaktywne GUI z opcją możliwości zmiany ekwipunku gracza oraz jego enderchestu!
- Pełna konfiguracja w configu
- Zapis gracza podczas: taska, wyjścia, śmierci, zapisu z poziomu panelu, zapisu z poziomu komendy `/niceeqbackups *`
- Osobny zapis ekwipunku i enderchestu podczas śmierci gracza
- Maksymalny zapis ekwipunku gracza jest zlimitowany do 45 *(można by było to poprawić, ale nie było potrzeby tego robić na samym serwerze, więc i tutaj nie zamierzam)*

### Wymagania
- Java 8+
- Wersja MC 1.16+

### Permisje
`niceeqbackups.use` - pozwala na użycie komendy niceeqbackup (skrót: `nqb`)
