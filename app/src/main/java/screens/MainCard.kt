package screens

import WeatherModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherappcompose.R
import org.json.JSONArray

@Composable
fun MainCard(
    currentDay: MutableState<WeatherModel>,
    onClickSync: () -> Unit,
    onClickSearch: @Composable () -> Unit,
    unit: Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFADD8E6).copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "30 Октября 2024",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Москва",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "15°C",
                style = TextStyle(
                    fontSize = 40.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AsyncImage(
                model = "https://cdn.weatherapi.com/weather/64x64/night/116.png",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = stringResource(R.string.sunny),
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                var isClicked by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(if (isClicked) 1.2f else 1f)

                IconButton(onClick = {
                    isClicked = !isClicked
                    onClickSearch.invoke()
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search Icon",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                }

                var isSyncClicked by remember { mutableStateOf(false) }
                val syncScale by animateFloatAsState(if (isSyncClicked) 1.2f else 1f)

                IconButton(onClick = {
                    isSyncClicked = !isSyncClicked
                    onClickSync.invoke()
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sync),
                        contentDescription = "Sync Icon",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(scaleX = syncScale, scaleY = syncScale)
                    )
                }
            }


        }
    }
}



@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val tabList = listOf("ЧАСЫ", "ДНИ")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFADD8E6))
            .padding(8.dp)
    ) {
        Surface(
            color = Color(0xFFADD8E6),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                tabList.forEachIndexed { index, text ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                        },
                        text = {
                            Text(text = text, color = Color.Black)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        val list = when (selectedTabIndex) {
            0 -> getWeatherByHours(currentDay.value.hours, currentDay.value.city, currentDay.value.minTemp, currentDay.value.maxTemp )
            1 -> daysList.value
            else -> daysList.value
        }

        MainList(list, currentDay)
    }
}

fun getWeatherByHours(hours: String, city: String, currentTemp: String, maxTemp: String): List<WeatherModel> {
    if (hours.isEmpty()) return emptyList()
    val hoursArray = JSONArray(hours)
    val list = mutableListOf<WeatherModel>()
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray.getJSONObject(i)
        list.add(
            WeatherModel(
                city,
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString() + "°C",
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                ""
            )
        )
    }
    return list
}







