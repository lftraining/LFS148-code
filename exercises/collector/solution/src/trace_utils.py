from opentelemetry import trace
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from resource_utils import create_resource


def create_tracing_pipeline() -> BatchSpanProcessor:
    otlp_exporter = OTLPSpanExporter(insecure=True)
    span_processor = BatchSpanProcessor(otlp_exporter)
    return span_processor

def create_tracer(name: str, version: str) -> trace.Tracer:
    rc = create_resource(name, version)
    processor = create_tracing_pipeline()
    provider = TracerProvider(resource=rc)
    provider.add_span_processor(processor)
    trace.set_tracer_provider(provider)
    tracer = trace.get_tracer(name, version)
    return tracer