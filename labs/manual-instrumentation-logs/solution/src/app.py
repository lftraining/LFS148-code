import logging
import time

import requests
from client import ChaosClient, FakerClient
from flask import Flask, make_response
from logging_utils import handler

logging.basicConfig(level=logging.INFO)

# global variables
app = Flask(__name__)
logger = logging.getLogger()
logger.addHandler(handler)


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


def do_stuff():
    time.sleep(0.1)
    url = "http://localhost:6000/"
    response = requests.get(url)
    logging.info(response.json())
    return response


@app.route("/")
def index():
    do_stuff()
    logging.info("Info from the index function")
    current_time = time.strftime("%a, %d %b %Y %H:%M:%S", time.gmtime())
    return f"Hello, World! It's currently {current_time}"


if __name__ == "__main__":
    db = ChaosClient(client=FakerClient())
    app.run(host="0.0.0.0", debug=True)
