package daggerok.db

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.orm.jpa.vendor.Database
import org.springframework.stereotype.Component

data class DbProps(var springDatasourceUrl: String? = null,
                   var springDatasourceUsername: String? = null,
                   var springDatasourcePassword: String? = null,
                   var springDatasourceClassName: String? = null,
                   var springJpaDatabase: Database? = null,
                   var query: String? = null)
@Component
@ConfigurationProperties(prefix = "app.dbs")
data class AppDatasourceProperties(var postgres: DbProps? = null,
                                   var oracle: DbProps? = null,
                                   var mysql: DbProps? = null)
