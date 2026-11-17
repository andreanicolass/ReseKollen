# ReseKollen

En Android-app (Kotlin + Jetpack Compose) för dig som ska ut och resa. Sök på ett resmål och få:

- **Vädret just nu**: temperatur, känns som, vind, luftfuktighet, nederbörd, UV-index och sikt
- **7-dagarsprognos**: max/min-temperatur och regnrisk för varje dag
- **Packtips**: baserade på vädret och prognosen (jacka, paraply, solkräm …)
- **Lokal valuta**: vilken valuta som gäller och vad din reskassa i SEK är värd där
- **Valutaomvandlare**: räkna om mellan ~170 valutor med live-kurser
- **Favoriter**: spara resmål och se senaste vädret i en lista
- **Svenska / engelska**: byt språk direkt med knappen 🇬🇧 EN / 🇸🇪 SV uppe till höger

All data hämtas live från publika API:er:

| API | Används till | Endpoint |
|-----|--------------|----------|
| [Open-Meteo Geocoding](https://open-meteo.com/en/docs/geocoding-api) | Stadsnamn → koordinater, land, tidszon | `GET /v1/search` |
| [Open-Meteo Forecast](https://open-meteo.com/en/docs) | Väder just nu + 7-dagarsprognos | `GET /v1/forecast` |
| [Exchangerate Host](https://exchangerate.host) | Live-växelkurser | `GET /live` |

## Kom igång

1. Skapa ett gratiskonto på **exchangerate.host** och kopiera din *API Access Key* från dashboarden. (Open-Meteo behöver ingen nyckel.)
2. Öppna `local.properties` i projektets rot (laddas inte upp till GitHub) och lägg till:
   ```properties
   EXCHANGERATE_KEY=din_exchangerate_nyckel
   ```
3. Öppna projektet i Android Studio, gör *Sync Project with Gradle Files* och kör appen på en emulator eller telefon.

> Exchangerate Hosts gratisplan har en månadsgräns på antal anrop. Appen cachar därför valutakurser i 6 timmar och väder i 15 minuter.

## Teknik

- **Kotlin**, **Jetpack Compose** och **Material 3**
- **Retrofit + Gson** för nätverksanrop
- **ViewModel + StateFlow + Coroutines** (MVVM)
- **SharedPreferences** för favoriter, valt språk och cachade valutakurser
- **Flerspråksstöd** via `res/values/strings.xml` (svenska) och `res/values-en/strings.xml` (engelska)

### Struktur

```
app/src/main/java/com/resekollen/
├── MainActivity.kt          // Scaffold med flikarna Resmål / Valuta / Favoriter
├── ReseKollenApp.kt         // Application-klass med delade instanser
├── data/
│   ├── ApiClient.kt         // Retrofit-gränssnitt för API:erna
│   ├── ApiModels.kt         // JSON-modeller
│   ├── WeatherReport.kt     // Appens egen vädermodell
│   ├── TravelRepository.kt  // Hämtar + cachar data, felhantering
│   ├── FavoritesStore.kt    // Sparade resmål
│   ├── Countries.kt         // Landskod -> valuta och flaggor
│   └── WeatherInfo.kt       // WMO-väderkoder -> svensk text/emoji, packtips
└── ui/
    ├── destination/         // Sök resmål: väder, prognos, packtips, valuta
    ├── currency/            // Valutaomvandlare
    ├── favorites/           // Favoritlista
    ├── components/          // Gemensamma UI-komponenter
    └── theme/               // Färger och tema (ljust + mörkt)
```

### Hur en sökning fungerar
1. Användaren skriver t.ex. "Barcelona".
2. **Geocoding-API:t** ger koordinater, land (`ES`) och tidszon.
3. **Forecast-API:t** hämtar väder just nu + 7 dagar för koordinaterna.
4. Landskoden översätts till valuta (`EUR`) och **Exchangerate Host** ger kursen mot SEK.

### Varför HTTP för valutor?
Exchangerate Hosts gratisplan stödjer bara `http://`. I `res/xml/network_security_config.xml` tillåts okrypterad trafik **endast** till den domänen.
