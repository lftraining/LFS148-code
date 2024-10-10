from opentelemetry.sdk._logs import LoggerProvider, LoggingHandler
from opentelemetry.sdk._logs.export import ConsoleLogExporter, SimpleLogRecordProcessor
from opentelemetry.sdk.resources import Resource

logger_provider = LoggerProvider(
    resource=Resource.create(
        {
            "service.name": "example-app",
        }
    ),
)

logger_provider.add_log_record_processor(SimpleLogRecordProcessor(ConsoleLogExporter()))
handler = LoggingHandler(logger_provider=logger_provider)