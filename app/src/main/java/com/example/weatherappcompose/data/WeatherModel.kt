data class WeatherModel(
    val city: String,
    val date: String,          // Убедись, что это поле присутствует
    val time: String,
    val condition: String,
    val icon: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String,
    var currentTemp: String = "" // Добавь это поле, если его нет
)
