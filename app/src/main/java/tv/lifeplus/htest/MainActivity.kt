package tv.lifeplus.htest

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

data class Range(val first: Int, val last: Int?) {
    fun contains(x: Int): Boolean = first <= x && last?.let { x <= it } ?: true
    override fun toString(): String = if (last == null) "$first+"
    else if (first == last) "$first"
    else "$first-$last"
}

data class Question(val text1: String, val text2: String, val systems: IntArray)
data class System(val name: String, val levels: IntArray) {
    val ranges = run {
        val ranges = mutableListOf<Range>()
        ranges.add(Range(0, levels[0] - 1))
        levels.reduce { level1, level2 -> ranges.add(Range(level1, level2 - 1));level2 }
        ranges.add(Range(levels.last(), null))
        ranges
    }
}

val rangeColors =
    arrayOf(Color(0xffbcfea4), Color(0xffb7e0fe), Color(0xfffeffb3), Color(0xffffc5c4))

val questions = listOf(
    Question(
        "Недостаток энергии, упадок сил",
        "Просыпаетесь вялыми, в течение дня преследует сонливость, апатия, пропадает интерес к жизни.",
        intArrayOf(0, 1, 2, 3, 6)
    ),
    Question(
        "Заболевания более 2 раз в год",
        "Простудные и другие заболевания более 2-х раз в год.",
        intArrayOf(3, 8)
    ),
    Question(
        "Неприятный запах тела или изо рта",
        "Чувствуется через короткое время после душа и гигиены полости рта.",
        intArrayOf(0, 4, 5)
    ),
    Question(
        "Плохое переваривание некоторых продуктов (чувство тяжести)",
        "Постоянные или периодические боли в животе в течение двух часов после приема пищи.",
        intArrayOf(0, 3)
    ),
    Question(
        "Употребление красного мяса",
        "Употребление красного мяса (говядина, баранина, стэйк с кровью) 2 и более раз в неделю.",
        intArrayOf(0, 1, 3, 4)
    ),
    Question(
        "Проблемы с менструальным циклом, в т.ч. болезненная менструация",
        "Менструальный цикл менее 21 дня или более 32 дней. Менструация сопровождается болями.",
        intArrayOf(0, 5, 6)
    ),
    Question(
        "Использование антибиотиков (лекарств)",
        "Применение антибиотиков и других медикаментов более 2 раз в год за последние 3 года.",
        intArrayOf(0, 3)
    ),
    Question(
        "Употребление алкоголя (в т.ч. пива)",
        "Употребление алкоголя (в т.ч. пива) более 1 раза в неделю.",
        intArrayOf(2, 6)
    ),
    Question(
        "Частые перепады настроения",
        "Вы не в состоянии поддерживать свой эмоциональный фон ровным в течении дня.",
        intArrayOf(2, 6)
    ),
    Question("Аллергия", "Высыпания, зуд, отеки, насморк.", intArrayOf(0, 3, 4, 8)),
    Question(
        "Темные круги или отечность под глазами",
        "Наличие припухлостей или темных кругов под глазами.",
        intArrayOf(1, 2, 5, 8)
    ),
    Question(
        "Курение (в т.ч. пассивное)",
        "Курите регулярно или после употребления алкоголя в кругу друзей, в том числе частое присутствие среди курящих.",
        intArrayOf(1, 2, 4, 8)
    ),
    Question(
        "Трудности с концентрацией внимания, плохое запоминание",
        "Рассеянность, страдает краткосрочная память.",
        intArrayOf(1, 2, 6)
    ),
    Question(
        "Дискомфорт после еды (изжога, газообразование)",
        "Хотя бы один раз в день, а особенно после приёма определенного типа пищи.",
        intArrayOf(0, 3)
    ),
    Question(
        "Нервная обстановка, частые стрессы",
        "Кроме явных стрессовых ситуаций присутствует чувство вины, обиды, переживания, тревога.",
        intArrayOf(1, 2, 3, 6, 8)
    ),
    Question(
        "Дефекты кожи или неудовлетворительный цвет кожи лица",
        "Высыпания, угри, пигментные пятна, папилломы на коже лица.",
        intArrayOf(0, 5, 6, 7, 8)
    ),
    Question(
        "Употребление сладостей или полуфабрикатов (в т.ч. фаст-фуд)",
        "Частое употребление десертов во время приема пищи, перекусываете сладостями. Больше употребляете переработанную пищу, нежели сырую. Употребление фаст-фуда.",
        intArrayOf(2, 6)
    ),
    Question(
        "Употребление молочных продуктов более 2 раз в неделю",
        "Употребление молочных продуктов и их производных (творога, масла, сыров и др.) более 2 раз в неделю.",
        intArrayOf(0, 4)
    ),
    Question(
        "Чувство апатии, вялости, депрессия",
        "Продолжительное время не можете выйти из таких состояний.",
        intArrayOf(0, 2, 6)
    ),
    Question(
        "Сон, не приносящий отдыха, бессонница",
        "Не можете заснуть в первые 10 минут или часто просыпаетесь от любого шороха.",
        intArrayOf(2, 6, 7)
    ),
    Question(
        "Период менопаузы, «приливы»",
        "Повышенное давление, приливы, потливость и т.д.",
        intArrayOf(2, 6, 7)
    ),
    Question(
        "Проблемы с мочеиспусканием или заболевания мочевого пузыря",
        "Мочеиспускание более 7 раз в день.",
        intArrayOf(5)
    ),
    Question(
        "Чувствительная (истончённая) кожа",
        "Ощущение стянутости после умывания, частые раздражения, возникновение красных пятен, чувство жжения, зуд, шелушение, реакция на использование новой косметики.",
        intArrayOf(8)
    ),
    Question(
        "Выпадение волос или проблемы с кожей головы",
        "Визуально замечаете увеличение количества выпавших волос во время мытья, в процессе расчесывания, на подушке после сна.",
        intArrayOf(1, 2, 6, 7)
    ),
    Question(
        "Боли в суставах, «хруст». Отечность. Онемение конечностей.",
        "Визуально видите отеки и ощущаете боли в суставах, особенно после долгого сидения.",
        intArrayOf(1, 3, 7)
    ),
    Question(
        "Отклонение от нормального веса",
        "Вес снижается только при ограничениях в питании, во время диеты. Либо не идёт набор веса даже при усиленном питании.",
        intArrayOf(2, 3, 6, 7)
    ),
    Question(
        "Быстрая утомляемость",
        "Нет сил, выносливости, требуется постоянный отдых.",
        intArrayOf(1, 4, 7)
    ),
    Question(
        "Нарушение режима питания",
        "Нерегулярное, неполноценное питание менее 3-х раз в день.",
        intArrayOf(0, 2, 6, 8)
    ),
    Question(
        "Длительное выздоровление после болезней",
        "Период выздоровления от простудных заболеваний более 1 недели.",
        intArrayOf(0, 1, 3, 6)
    ),
    Question(
        "Нерегулярный стул",
        "При 3-х разовом питании опорожнение кишечника менее 2 раз в день и/или явные запоры.",
        intArrayOf(0, 2, 8)
    ),
    Question(
        "Плохой аппетит", "Вам приходится заставлять себя что-то скушать.", intArrayOf(0, 2, 6)
    ),
    Question(
        "Истончённые и ломкие ногти (слоящиеся ногти)",
        "Вам трудно отрастить ногти, ломаются при любом механическом воздействии.",
        intArrayOf(0, 7)
    ),
    Question(
        "Повреждённые волосы (сухие или тусклые)",
        "Ломкость волоса в прикорневой зоне или 5-10 см от корня волоса, посеченные кончики.",
        intArrayOf(0, 5)
    ),
    Question(
        "Употребление жирной пищи",
        "Употребление жареной, масляной, сильно жирной пищи более 2 раз в неделю.",
        intArrayOf(0, 1)
    ),
    Question(
        "Недостаток клетчатки в рационе",
        "Употребление овощей, в т.ч. салатов - менее 2 раз в день.",
        intArrayOf(0, 1)
    ),
    Question(
        "Мышечный дискомфорт (боли, судороги)",
        "Частые судороги по утрам, в воде, при ходьбе.",
        intArrayOf(1, 2, 7)
    ),
    Question(
        "Неблагоприятная экология",
        "Живете в мегаполисе, неподалеку заводы, фабрики, токсичное производство.",
        intArrayOf(3, 4, 8)
    ),
    Question("Дневная сонливость", "Постоянно хочется днём спать.", intArrayOf(1, 6)),
    Question(
        "Ежедневное потребление колы, кофе или крепкого чая",
        "Пьёте более 2-х чашек колы, кофе или крепкого чая в день.",
        intArrayOf(2, 6, 7)
    ),
    Question(
        "Чувствительность к химикатам, лекарствам, некоторой пище",
        "Дискомфорт любого характера: зуд, одышка, отеки.",
        intArrayOf(0, 3)
    ),
    Question(
        "Грибковые поражения",
        "Наличие белого налёта на языке по утрам, выделения у женщин, грибок на ногтях.",
        intArrayOf(0, 3, 5)
    ),
    Question(
        "Слабость в мышцах или хрупкость костей",
        "Частые переломы, периодически подкашиваются ноги.",
        intArrayOf(0, 7)
    ),
    Question("Чувство тревоги", "Вы постоянно переживаете и порой по пустякам.", intArrayOf(0, 2)),
    Question(
        "Повышенная раздражительность, чрезмерная возбудимость",
        "Раздражаетесь, злитесь и понимаете, что веских причин для этого нет.",
        intArrayOf(0, 2, 6)
    ),
    Question(
        "Малоподвижный образ жизни, низкая физическая активность",
        "Ведете сидячий образ жизни, малоподвижная работа, отсутствует какая-либо физическая активность.",
        intArrayOf(0, 1, 2, 3, 6, 7)
    ),
    Question(
        "Повышенное выделение мокроты (выделение слизи)",
        "Кашель с мокротой по утрам и в течение дня, независимо от простуды, повышенное слюноотделение.",
        intArrayOf(0, 4)
    ),
    Question(
        "Большие поры на коже, повышенное потоотделение (угри)",
        "Кожа жирная, крупные поры, угревая сыпь, иные высыпания.",
        intArrayOf(8)
    ),
)
val systems = listOf(
    System("Пищеварительная", intArrayOf(3, 5, 10)),
    System("Сердечно сосудистая", intArrayOf(3, 4, 8)),
    System("Нервная", intArrayOf(3, 6, 10)),
    System("Иммунная", intArrayOf(3, 5, 8)),
    System("Дыхательная", intArrayOf(1, 4, 6)),
    System("Мочеполовая", intArrayOf(1, 2, 5)),
    System("Эндокринная", intArrayOf(3, 6, 10)),
    System("Опорно двигательная", intArrayOf(2, 4, 9)),
    System("Кожа", intArrayOf(2, 4, 7)),
)
val femaleQuestions = listOf(5, 20)

class MyViewModel(app: Application) : AndroidViewModel(app) {
    val home = mutableStateOf(true)
    val answers = mutableStateListOf<Boolean>()

    val completed get() = answers.size == questions.size
    val question get() = answers.size
    fun results(): IntArray {
        val results = systems.map { 0 }.toIntArray()
        for ((_, question) in answers.zip(questions).filter { it.first }) {
            for (i in question.systems) ++results[i]
        }
        return results
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MyViewModel = viewModel()
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                val modifier = Modifier.padding(innerPadding)
                fun Modifier.pad(): Modifier = padding(4.dp)
                if (vm.home.value) {
                    UiHome(modifier.pad(), { vm.home.value = false;vm.answers.clear() })
                } else if (vm.completed) {
                    val results = remember(vm.answers) { vm.results() }
                    UiResults(
                        results,
                        modifier
                            .verticalScroll(rememberScrollState())
                            .pad()
                    ) { vm.home.value = true }
                } else {
                    UiQuestion(
                        vm.question,
                        modifier.pad(),
                        { vm.home.value = true }) { vm.answers.add(it) }
                }
            }
        }
    }
}

@Composable
fun UiHome(modifier: Modifier = Modifier, onStart: (() -> Unit)? = null) {
    Column(modifier) {
        Text(
            "Узнай за 7 минут состояние своего здоровья",
            fontSize = 4.6.em,
            fontWeight = FontWeight.W700
        )
        Text("")
        Text("Питание, образ жизни, привычки и другие обстоятельства напрямую влияют на состояние систем нашего организма")
        Text("")
        Text("Тест поможет оценить показатели здоровья и выявить зоны риска")
        Text("")
        Text("На основе результатов теста для вас будут подобраны персональные рекомендации")
        Spacer(Modifier.weight(1f))
        Button(onStart ?: {}) { Text("Пройти тест") }
    }
}

@Composable
fun UiResults(results: IntArray, modifier: Modifier = Modifier, onReset: (() -> Unit)? = null) {
    val spacer = @Composable { Spacer(modifier = Modifier.height(24.dp)) }
    Column(modifier) {
        Text("Результаты теста", fontSize = 4.6.em, fontWeight = FontWeight.W700)
        spacer()
        UiResultsTable(results)
        spacer()
        UiResultsGroups(results)
        spacer()
        Button(onReset ?: {}) { Text("Пройти снова") }
    }
}

@Composable
fun UiResultsGroups(results: IntArray, modifier: Modifier = Modifier) {
    val groups = listOf(
        Pair(
            "Системы которые работают очень хорошо", ""
        ),
        Pair(
            "Системы, которые работают хорошо, но уже начался сдвиг в ЗОНУ РИСКА",
            "Если ничего не менять в питании и образе жизни, то со временем эти системы перейдут в ЗОНУ РИСКА."
        ),
        Pair(
            "Системы, которые находятся в самой опасной ЗОНЕ РИСКА",
            "В любой момент стресс может вызвать сбой какой-либо системы в организме, что может привести к серьезным заболеваниям! Медлить нельзя! Надо срочно заняться своим здоровьем!",
        ),
        Pair(
            "Системы поражены, или в ближайшее время могут проявиться заболевания",
            "Вам срочно необходимо обратиться к специалисту, если вы до сих пор этого не сделали!",
        ),
    )
    Column(modifier) {
        groups.mapIndexed { i, group ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(rangeColors[i])
                    .padding(6.dp)
            ) {
                Text("${group.first}:")
                results.zip(systems).filter { it.second.ranges[i].contains(it.first) }
                    .map { Text("• ${it.second.name}") }
                if (group.second.isNotEmpty()) {
                    Text("")
                    Text(group.second)
                }
            }
        }
    }
}

@Composable
fun UiResultsTable(results: IntArray, modifier: Modifier = Modifier) {
    val borderColor = Color.Gray

    fun Modifier.borderLeft(): Modifier =
        drawBehind { drawLine(borderColor, Offset.Zero, Offset(0f, size.height)) }

    fun Modifier.borderTop(): Modifier =
        drawBehind { drawLine(borderColor, Offset.Zero, Offset(size.width, 0f)) }

    fun Modifier.pad(): Modifier = padding(2.dp)

    fun RowScope.col0(modifier: Modifier = Modifier): Modifier = modifier
        .fillMaxHeight()
        .weight(0.4f)

    fun RowScope.col(modifier: Modifier = Modifier): Modifier = modifier
        .fillMaxHeight()
        .weight(0.15f)
        .borderLeft()

    val tr = Modifier.height(intrinsicSize = IntrinsicSize.Max)


    Column(modifier) {
        Column {
            Row(tr) {
                Text("Система", col0().pad())
                Text("Очень хорошо", col().pad())
                Text("Хорошо", col().pad())
                Text("Зона риска", col().pad())
                Text("Плохо", col().pad())
            }
            for ((result, system) in results.zip(systems)) {
                Row(tr.borderTop()) {
                    Text(system.name, col0().pad())
                    system.ranges.zip(rangeColors).map { (range, color) ->
                        Column(
                            col()
                                .background(color)
                                .pad()
                        ) {
                            Text("$range", Modifier.pad())
                            if (range.contains(result)) {
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(2.dp, Color(0xffcc0000), CircleShape)
                                ) {
                                    Text(
                                        "$result",
                                        fontSize = 4.6.em,
                                        fontWeight = FontWeight.W700,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TODO: reset button or navigation
@Composable
fun UiQuestion(
    i: Int,
    modifier: Modifier = Modifier,
    onReset: (() -> Unit)? = null,
    onAnswer: ((answer: Boolean) -> Unit)? = null,
) {
    val button = @Composable { answer: Boolean, text: String, modifier: Modifier ->
        Button({ onAnswer?.invoke(answer) }, modifier) { Text(text) }
    }
    val question = questions[i]
    Column(modifier) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Вопрос: ${i + 1}/${questions.size}")
            Text("")
            LinearProgressIndicator({ (i + 1f) / (questions.size + 1) }, Modifier.fillMaxWidth())
            Text("")
            Text(question.text1)
            Text("")
            Text(question.text2)
        }
        Column {
            if (femaleQuestions.contains(i)) button(false, "Мужчина", Modifier)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    button(false, "Нет", Modifier)
                    button(true, "Да", Modifier.padding(start = 4.dp))
                }
                Button(onReset ?: {}) { Text("Остановить тест") }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 600)
@Composable
fun Preview() {
    val results = intArrayOf(13, 11, 12, 11, 4, 3, 12, 7, 7)
    // UiHome()
    UiQuestion(5)
    // UiResults(results)
}
