from opentelemetry.exporter.otlp.proto.grpc._log_exporter import OTLPLogExporter
from opentelemetry.sdk._logs import LoggerProvider, LoggingHandler
from opentelemetry.sdk._logs.export import SimpleLogRecordProcessor
from opentelemetry.sdk.resources import Resource

logger_provider = LoggerProvider(
    resource=Resource.create(
        {
            "service.name": "example-app",
        }
    ),
)

logger_provider.add_log_record_processor(SimpleLogRecordProcessor(exporter=OTLPLogExporter(insecure=True)))
handler = LoggingHandler(logger_provider=logger_provider)
