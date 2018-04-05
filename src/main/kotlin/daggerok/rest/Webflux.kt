package daggerok.rest

import daggerok.db.DbProps
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.orm.jpa.vendor.Database
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

@Configuration
class WebfluxRoutesConfig(val applicationContext: ApplicationContext,
                          val dataSourcesHolder: ConcurrentHashMap<String, DataSource>) {
  @Bean
  fun routes() = router {

    ("/").nest {

      contentType(MediaType.APPLICATION_JSON_UTF8)

      GET("/**") {
        val dataSource = try { applicationContext.getBean(DataSource::class.java) } catch (e: Throwable) { null }
        val metaData = dataSource?.connection?.metaData ?: return@GET notFound().build()
        val map = mapOf(
            "driverName" to metaData.driverName,
            "driverVersion" to metaData.driverVersion,
            "databaseProductVersion" to metaData.databaseProductVersion,
            "url" to metaData.url,
            "userName" to metaData.userName,
            "holdingDataSources" to "$dataSourcesHolder"
        )
        ok().body(
            Mono.just(map), map.javaClass
        )
      }

      POST("/**") {
        it.bodyToMono(DbProps::class.java)
            .filter { it.springJpaDatabase != null }
            .filter { !it.springDatasourceUrl.isNullOrBlank() }
            .filter { !it.springDatasourceUsername.isNullOrBlank() }
            .filter { !it.springDatasourcePassword.isNullOrBlank() }
            .filter { !it.springDatasourceClassName.isNullOrBlank() }
            .filter { !it.query.isNullOrBlank() }
            .map {

              val dataSource = DataSourceBuilder
                  .create()
                  .url(it.springDatasourceUrl)
                  .username(it.springDatasourceUsername)
                  .password(it.springDatasourcePassword)
                  .driverClassName(it.springDatasourceClassName)
                  .build()

              val metaData = dataSource?.connection?.metaData

              dataSourcesHolder[it.springJpaDatabase!!.name] = dataSource
              val prepareStatement = dataSource.connection.prepareStatement(it.query)

              mapOf(
                  "result" to prepareStatement.executeQuery().metaData,
                  "driverName" to metaData.driverName,
                  "driverVersion" to metaData.driverVersion,
                  "databaseProductVersion" to metaData.databaseProductVersion,
                  "url" to metaData.url,
                  "userName" to metaData.userName,
                  "holdingDataSources" to "$dataSourcesHolder"
              )
            }
            .subscribeOn(Schedulers.elastic())
            .flatMap {
              ok().body(
                  Mono.just(it), it.javaClass
              )
            }
      }
    }
  }
}
