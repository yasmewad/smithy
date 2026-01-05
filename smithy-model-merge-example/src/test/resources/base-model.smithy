$version: "2.0"

namespace com.example.weather

@documentation("Old documentation that will be replaced")
service WeatherService {
    version: "2024-01-01"
    operations: [GetWeather, GetForecast]
}

@documentation("Old operation documentation")
operation GetWeather {
    input: GetWeatherInput
    output: GetWeatherOutput
}

@documentation("Old operation documentation")
operation GetForecast {
    input: GetForecastInput
    output: GetForecastOutput
}

@documentation("Old structure documentation")
structure GetWeatherInput {
    @documentation("Old member documentation")
    @required
    city: String
}

structure GetWeatherOutput {
    @documentation("Old member documentation")
    temperature: Integer
    
    @documentation("Old member documentation")
    conditions: String
}

structure GetForecastInput {
    @required
    city: String
    
    days: Integer
}

structure GetForecastOutput {
    forecasts: ForecastList
}

list ForecastList {
    member: DailyForecast
}

structure DailyForecast {
    date: String
    highTemp: Integer
    lowTemp: Integer
}
