import grails.util.BuildSettings
import grails.util.Environment
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

// See http://logback.qos.ch/manual/groovy.html for details on configuration

def logPattern = "%date [%thread] %level %logger - %msg%n"

appender('STDOUT', ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = logPattern
	}
}

root(ERROR, ['STDOUT'])

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir) {
	// local development config
	// stacktrace log
    logger("swordfishsync", ALL)
    logger("ca.benow.transmission", ALL)
	appender("FULL_STACKTRACE", RollingFileAppender) {
		file = "${targetDir}/sfs-stacktrace.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = logPattern
		}
		rollingPolicy(TimeBasedRollingPolicy) {
			fileNamePattern = "${targetDir}/sfs-stacktrace.%d{yyyy-MM-dd}.log"
		}
	}
	logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
	// sfs log
	appender("FILE", RollingFileAppender) {
		file = "${targetDir}/sfs.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = logPattern
		}
		rollingPolicy(TimeBasedRollingPolicy) {
			fileNamePattern = "${targetDir}/sfs.%d{yyyy-MM-dd}.log"
		}
	}
	logger("swordfishsync", DEBUG, ['FILE'], true)
}


if (Environment.current != Environment.DEVELOPMENT && Environment.current != Environment.TEST) {
	// configure the tomcat7 server logs

	def appLogLevel = INFO
	if (Environment.current == Environment.PRODUCTION) {
		appLogLevel = INFO
	}

	// stacktrace log
	appender("FULL_STACKTRACE", RollingFileAppender) {
		file = "/var/log/tomcat7/sfs-stacktrace.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = logPattern
		}
		rollingPolicy(TimeBasedRollingPolicy) {
			fileNamePattern = "/var/log/tomcat7/sfs-stacktrace.%d{yyyy-MM-dd}.log"
		}
	}
	logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
	// sfs log
	appender("FILE", RollingFileAppender) {
		file = "/var/log/tomcat7/sfs.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = logPattern
		}
		rollingPolicy(TimeBasedRollingPolicy) {
			fileNamePattern = "/var/log/tomcat7/sfs.%d{yyyy-MM-dd}.log"
		}
	}
	logger("swordfishsync", appLogLevel, ['FILE'], false)
}
