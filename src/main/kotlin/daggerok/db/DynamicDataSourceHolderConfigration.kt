package daggerok.db

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

@Configuration
class DynamicDataSourceHolderConfigration {

  @Bean
  fun dataSourcesHolder() = ConcurrentHashMap<String, DataSource>()
}
