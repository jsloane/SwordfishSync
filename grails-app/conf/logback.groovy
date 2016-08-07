import grails.util.BuildSettings
import grails.util.Environment

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %date %logger - %msg%n"
    }
}

root(ERROR, ['STDOUT'])
logger("swordfishsync", ALL, ['STDOUT'], false)

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %date %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
if (Environment.currentEnvironment != Environment.DEVELOPMENT && Environment.currentEnvironment != Environment.TEST) {
	def currentDay = timestamp("yyyyMMdd")
	// todo: get log file directory from system, or log to ~/.swordfishsync/log/
	// log everything
	appender("ROLLING_FILE_ALL", RollingFileAppender) {
		file = "/var/logs/tomcat7/swordfishsync_${currentDay}.log"
		rollingPolicy(TimeBasedRollingPolicy) {
			fileNamePattern = "/var/logs/tomcat7/swordfishsync.%d.log"
			maxHistory = 21
			//totalSizeCap = "1GB"
		}
		/*triggeringPolicy(SizeBasedTriggeringPolicy) {
			maxFileSize = "100MB"
		}*/
		encoder(PatternLayoutEncoder) {
			pattern = "%level %date %logger - %msg%n"
		}
		append = true
	}
    logger("swordfishsync", ALL, ['ROLLING_FILE_ALL'], false)
    logger("ca.benow.transmission", ALL, ['ROLLING_FILE_ALL'], false)
	// log errors
	/*appender("ROLLING_FILE_STACKTRACE", RollingFileAppender) {
		file = "/var/logs/tomcat7/swordfishsync-stacktrace_${currentDay}.log"
		rollingPolicy(FixedWindowRollingPolicy) {
			fileNamePattern = "/var/logs/tomcat7/swordfishsync-stacktrace_${currentDay}.%i.log"
			minIndex = 1
			maxIndex = 9
		}
		triggeringPolicy(SizeBasedTriggeringPolicy) {
			maxFileSize = "100MB"
		}
		encoder(PatternLayoutEncoder) {
			pattern = "%level %date %logger - %msg%n"
		}
		append = true
	}
    logger("swordfishsync", ERROR, ['ROLLING_FILE_STACKTRACE'], false)
    logger("ca.benow.transmission", ERROR, ['ROLLING_FILE_STACKTRACE'], false)*/
}