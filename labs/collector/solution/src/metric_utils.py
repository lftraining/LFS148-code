# pyright: reportMissingTypeStubs=false

from typing import Any

import psutil
from opentelemetry import metrics
from opentelemetry.exporter.otlp.proto.grpc.metric_exporter import OTLPMetricExporter

# prometheus export
from opentelemetry.metrics import Counter, Histogram, ObservableGauge
from opentelemetry.sdk.metrics import MeterProvider
# console export
from opentelemetry.sdk.metrics.export import (MetricReader,
                                              PeriodicExportingMetricReader)
# views
from opentelemetry.sdk.metrics.view import (DropAggregation,
                                            ExplicitBucketHistogramAggregation,
                                            View)
from resource_utils import create_resource


def create_views() -> list[View]:
    views = []

    # change name of an instrument
    traffic_volume_change_name = View(
        instrument_type=Counter,
        instrument_name="traffic_volume",
        name="test",
    )

    views.append(traffic_volume_change_name) # type: ignore

    # drop entire intrument
    drop_instrument = View(
        instrument_type=ObservableGauge,
        instrument_name="process.cpu.utilization",
        aggregation=DropAggregation(),
    )
    views.append(drop_instrument) # type: ignore

    # change the aggregation (buckets) for all histogram instruments
    histrogram_explicit_buckets = View(
        instrument_type=Histogram,
        instrument_name="*",  #  supports wildcard pattern matching
        aggregation=ExplicitBucketHistogramAggregation((1, 21, 50, 100, 1000)),
    )
    views.append(histrogram_explicit_buckets) # type: ignore

    return views # type: ignore


def create_otlp_reader(export_interval: int) -> MetricReader:
    otlp_exporter = OTLPMetricExporter(insecure=True)
    reader = PeriodicExportingMetricReader(
        exporter=otlp_exporter, export_interval_millis=export_interval
    )
    return reader


def create_meter(name: str, version: str) -> metrics.Meter:
    views = create_views()
    rc = create_resource(name, version)

    otlp_reader = create_otlp_reader(5000)

    provider = MeterProvider(
        metric_readers=[otlp_reader], resource=rc, views=views
    )
    metrics.set_meter_provider(provider)
    meter = metrics.get_meter(name, version)
    return meter


def create_request_instruments(meter: metrics.Meter) -> dict[str, Any]:
    traffic_volume = meter.create_counter(
        name="traffic_volume",
        unit="request",
        description="total volume of requests to an endpoint",
    )

    error_rate = meter.create_counter(
        name="error_rate", unit="request", description="rate of failed requests"
    )

    # https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-metrics.md#metric-httpserverrequestduration
    request_latency = meter.create_histogram(
        name="http.server.request.duration",
        unit="s",
        description="latency for a request to be served",
    )

    instruments = {
        "traffic_volume": traffic_volume,
        "error_rate": error_rate,
        "request_latency": request_latency,
    }
    return instruments


# units https://opentelemetry.io/docs/specs/semconv/general/metrics/#instrument-units
def create_resource_instruments(meter: metrics.Meter) -> dict[str, Any]:
    cpu_util_gauge = meter.create_observable_gauge(
        name="process.cpu.utilization",
        callbacks=[
            lambda x: [metrics.Observation(psutil.cpu_percent(interval=1) / 100)]
        ],
        unit="1",
        description="CPU utilization",
    )

    memory_usage_gauge = meter.create_observable_up_down_counter(
        name="process.memory.usage",
        callbacks=[lambda x: [metrics.Observation(psutil.virtual_memory().used)]],
        unit="By",
        description="total amount of memory used",
    )

    instruments = {"cpu_utilization": cpu_util_gauge, "memory_used": memory_usage_gauge}
    return instruments
