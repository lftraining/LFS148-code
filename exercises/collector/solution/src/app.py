# pyright: reportMissingTypeStubs=false, reportUnknownParameterType=false, reportMissingParameterType=false, reportUnknownArgumentType=false

import logging
import time

import requests
from client import ChaosClient, FakerClient
from flask import Flask, Response, make_response, request
from logging_utils import handler
from metric_utils import (
    create_meter,
    create_request_instruments,
    create_resource_instruments,
)
from opentelemetry import context
from opentelemetry.propagate import extract, inject
from opentelemetry.semconv.trace import SpanAttributes
from opentelemetry.trace import get_current_span
from trace_utils import create_tracer

logging.basicConfig(level=logging.INFO)
tracer = create_tracer("app.py", "0.1")


# global variables
app = Flask(__name__)
logger = logging.getLogger()
logger.addHandler(handler)

@app.before_request
def attach_context_with_trace_header():
    ctx = extract(request.headers)
    previous_ctx_token = context.attach(ctx)
    request.environ["previous_ctx_token"] = previous_ctx_token

@app.teardown_request
def restore_context_on_teardown(err):
    previous_ctx_token = request.environ.get("previous_ctx_token", None)
    if previous_ctx_token:
        context.detach(previous_ctx_token)

@app.before_request
def before_request():
    workload_instruments["traffic_volume"].add(
        1, attributes={"http.route": request.path}
    )
    request.environ["request_start"] = time.time_ns()


@app.after_request
def after_request(response: Response) -> Response:
    workload_instruments["request_latency"].record(
        amount=(time.time_ns() - request.environ["request_start"]) / 1_000_000_000,
        attributes={
            "http.request.method": request.method,
            "http.route": request.path,
            "http.response.status_code": response.status_code,
        },
    )
    return response

@tracer.start_as_current_span("users")
@app.route("/users", methods=["GET"])
def get_user():
    user, status = db.get_user(123)
    logging.info(f"Found user {user!s} with status {status}")
    data = {}
    if user is not None:
        data = {"id": user.id, "name": user.name, "address": user.address}
    else:
        logging.warning(f"Could not find user with id {123}")
    logging.debug(f"Collected data is {data}")
    response = make_response(data, status)
    logging.debug(f"Generated response {response}")
    return response

@tracer.start_as_current_span("do_stuff")
def do_stuff():
    time.sleep(0.1)
    headers = {}
    inject(headers)
    url = "http://localhost:6000/"
    response = requests.get(url)
    print(response.json())
    logging.info(str(response.json()))
    return response

@tracer.start_as_current_span("index")
@app.route("/")
def index():
    span = get_current_span()
    span.set_attributes(
        {
            SpanAttributes.HTTP_REQUEST_METHOD: request.method,
            SpanAttributes.URL_PATH: request.path,
            SpanAttributes.HTTP_RESPONSE_STATUS_CODE: 200
        }
    )
    do_stuff()
    logging.info("Info from the index function")
    current_time = time.strftime("%a, %d %b %Y %H:%M:%S", time.gmtime())
    return f"Hello, World! It's currently {current_time}"


if __name__ == "__main__":
    # setup metrics
    meter = create_meter("app.py", "0.1")
    workload_instruments = create_request_instruments(meter)
    rc_instruments = create_resource_instruments(meter)

    db = ChaosClient(client=FakerClient())
    app.run(host="0.0.0.0", debug=True)
