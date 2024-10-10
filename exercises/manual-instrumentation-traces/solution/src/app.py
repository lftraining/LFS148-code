# pyright: reportMissingTypeStubs=false, reportUnknownParameterType=false, reportMissingParameterType=false, reportUnknownArgumentType=false

import json
import time

import requests
from client import ChaosClient, FakerClient
from flask import Flask, make_response, request
from opentelemetry import context
from opentelemetry.propagate import extract, inject
from opentelemetry.semconv.trace import SpanAttributes
from opentelemetry.trace import get_current_span
from trace_utils import create_tracer

app = Flask(__name__)
tracer = create_tracer("app.py", "0.1")

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

@app.route('/users', methods=['GET'])
@tracer.start_as_current_span("users")
def get_user():
    user, status = db.get_user(123)
    data = {}
    if user is not None:
        data = {
            "id": user.id,
            "name": user.name,
            "address": user.address
        }
    response = make_response(data, status)
    return response

@tracer.start_as_current_span("do_stuff")
def do_stuff():
    headers = {}
    inject(headers)

    time.sleep(.1)
    url = "http://localhost:6000/"
    response = requests.get(url, headers=headers)

    # debug
    print("Headers included in outbound request:")
    print(json.dumps(response.json()["request"]["headers"], indent=2))

    return response

@app.route('/')
@tracer.start_as_current_span("index")
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
    current_time = time.strftime("%a, %d %b %Y %H:%M:%S", time.gmtime())
    msg = f"Hello, World! It's currently {current_time}"
    return msg

if __name__ == "__main__":
    db = ChaosClient(client=FakerClient())
    app.run(host="0.0.0.0", debug = True)
