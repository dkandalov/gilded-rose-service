
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager.Companion.EXECUTION_TOPIC
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import liveplugin.registerAction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

val startTimeByRunProfile = ConcurrentHashMap<String, Long>()
val latchByRunProfile = ConcurrentHashMap<String, CountDownLatch>()

project!!.messageBus.connect(pluginDisposable)
    .subscribe(EXECUTION_TOPIC, object : ExecutionListener {
        override fun processStarting(executorId: String, env: ExecutionEnvironment) {
            startTimeByRunProfile[env.runProfile.name] = System.currentTimeMillis()
            latchByRunProfile[env.runProfile.name] = startBackgroundTask(env.runProfile.name)
        }

        override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
            val startTime = startTimeByRunProfile.remove(env.runProfile.name) ?: return
            val duration = System.currentTimeMillis() - startTime
            liveplugin.show(env.runProfile.name + " finished in $duration ms")

            latchByRunProfile.remove(env.runProfile.name)?.countDown()
        }
    })

fun startBackgroundTask(taskTitle: String): CountDownLatch {
    val latch = CountDownLatch(1)
    object: Task.Backgroundable(null, taskTitle, true, ALWAYS_BACKGROUND) {
        override fun run(indicator: ProgressIndicator) = latch.await()
    }.queue()
    return latch
}

class InsertTextAction(private val text: String) : EditorAction(object: EditorActionHandler() {
    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        editor.document.insertString(editor.caretModel.offset, text)
    }
})

registerAction("Insert Logger Snippet", keyStroke = "ctrl shift K, 1", action = InsertTextAction("""
newLogger: (String) -> Logger = ::defaultLogger
""".trimIndent()))

registerAction("Insert Config Snippet", keyStroke = "ctrl shift K, 2", action = InsertTextAction("""
@ConfigurationProperties(prefix = "gildedrose")
class Config(
    var users: List<String> = emptyList(),
    var port: Int = 0,
    var dbConfig: DbConfig = DbConfig()
) {
    companion object {
        fun load(env: String? = null): Config {
            val envPostfix = if (env == null) "" else "-${'$'}env"
            val properties = propertiesFromClasspath("/application${'$'}envPostfix.properties")
            return Config(
                users = properties["gildedrose.users"].toString().split(","),
                port = properties["server.port"].toString().toInt(),
                dbConfig = DbConfig(
                    url = properties["spring.datasource.url"].toString(),
                    username = properties["spring.datasource.username"].toString(),
                    password = properties["spring.datasource.password"].toString()
                )
            )
        }
    }
}

class DbConfig(
    var url: String = "",
    var username: String = "",
    var password: String = ""
)

fun DbConfig.toDataSource() =
    HikariDataSource(
        HikariConfig().also {
            it.jdbcUrl = url
            it.username = username
            it.password = password
        }
    )

private fun propertiesFromClasspath(path: String) = Properties().apply {
    load(Config::class.java.getResourceAsStream(path))
}    
"""))

registerAction("Insert Http4k Controller Snippet", keyStroke = "ctrl shift K, 3", action = InsertTextAction("""
import kotlinx.datetime.LocalDate
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.NoOp
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters.BasicAuth
import org.http4k.filter.ServerFilters.CatchLensFailure
import org.http4k.format.Jackson.auto
import org.http4k.lens.BiDiMapping
import org.http4k.lens.Query
import org.http4k.lens.map
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.Logger

class WebController(
    private val config: Config,
    private val gildedRoseService: GildedRoseService,
    newLogger: (String) -> Logger = ::defaultLogger
) : HttpHandler {
    private val logger = newLogger(javaClass.simpleName)

    private val basicAuth = BasicAuth(realm = "") { credentials ->
        credentials.user in config.users && credentials.password == "secret"
    }
    private val dateLens = Query.map(kotlinxLocalDate).required("date")
    private val bodyLens = Body.auto<List<Item>>().toLens()

    private val filters = Filter.NoOp.then(basicAuth).then(CatchLensFailure())
    private val routes = filters.then(routes(
        "items" bind GET to { request ->
            val date: LocalDate = dateLens(request)
            logger.info("Requested items for ${'$'}date")
            val items = gildedRoseService.items(date)
            Response(OK).with(bodyLens of items)
        }
    ))

    override fun invoke(request: Request) = routes(request)
}

private val kotlinxLocalDate = BiDiMapping<String, LocalDate>(
    asOut = { LocalDate.parse(it) },
    asIn = { it.toString() }
)
"""))

registerAction("Insert TestRestTemplate Snippet", keyStroke = "ctrl shift K, 4", action = InsertTextAction("""
private val rest = TestRestTemplate(RestTemplateBuilder().rootUri("http://127.0.0.1:8081/"))
""".trimIndent()))
