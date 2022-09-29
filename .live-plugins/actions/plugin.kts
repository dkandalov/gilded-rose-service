
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
    object: Task.Backgroundable(null, taskTitle, false, ALWAYS_BACKGROUND) {
        override fun run(indicator: ProgressIndicator) = latch.await()
    }.queue()
    return latch
}

class InsertTextAction(private val text: String) : EditorAction(object: EditorActionHandler() {
    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        editor.document.insertString(editor.caretModel.offset, text)
    }
})

registerAction("Insert Config Snippet", keyStroke = "ctrl shift K, 1", action = InsertTextAction("""
@ConfigurationProperties(prefix = "gildedrose")
class Config(
    var users: List<String> = emptyList(),
    var port: Int = 0,
    var db: DbConfig = DbConfig()
) {
    companion object {
        fun load(env: String? = null): Config {
            val envPostfix = if (env == null) "" else "-${'$'}env"
            val properties = propertiesFromClasspath("/application${'$'}envPostfix.properties")
            return Config(
                users = properties["gildedrose.users"].toString().split(","),
                port = properties["server.port"].toString().toInt(),
                db = DbConfig(
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

registerAction("Insert Http4k Controller Snippet", keyStroke = "ctrl shift K, 2", action = InsertTextAction("""
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ServerFilters.BasicAuth
import org.http4k.routing.bind
import org.http4k.routing.routes

class WebControllerHttp4k(
    private val config: Config,
    private val gildedRoseService: GildedRoseService =
        GildedRoseService(DbItemsRepository(config.db.toDataSource())),
    newLogger: (String) -> Logger = ::defaultLogger
) : HttpHandler {
    private val logger = newLogger(javaClass.simpleName)
    private val objectMapper = jacksonObjectMapper()

    private val routes = routes(
        "items" bind GET to { request ->
            val items = request.query("date")?.let {
                logger.info("Requested items for ${'$'}it")
                gildedRoseService.items(it.toLocalDate())
            }
            if (items == null) Response(BAD_REQUEST).body("date parameter is required")
            else Response(OK).header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(items))
        }
    ).withFilter(BasicAuth("") { it.user in config.users && it.password == "secret" })

    override fun invoke(request: Request) = routes(request)
}    
"""))

registerAction("Insert TestRestTemplate Snippet", keyStroke = "ctrl shift K, 3", action = InsertTextAction("""
private val template = TestRestTemplate(RestTemplateBuilder().rootUri("http://127.0.0.1:8081/"))
""".trimIndent()))
