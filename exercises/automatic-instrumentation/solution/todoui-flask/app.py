from flask import Flask, render_template, request, jsonify, redirect, url_for

import logging
import requests
import os

# Import the trace API
from opentelemetry import trace

# Acquire a tracer
tracer = trace.get_tracer("todo.tracer")

app = Flask(__name__)
logging.getLogger(__name__)
logging.basicConfig(format='%(levelname)s:%(name)s:%(module)s:%(message)s', level=logging.INFO)

# Set a default external API URL
# Override the default URL if an environment variable is set
app.config['BACKEND_URL'] = 'http://localhost:8080/todos/'
app.config['BACKEND_URL'] = os.getenv('BACKEND_URL', app.config['BACKEND_URL'])

@app.route('/')
def index():

    backend_url = app.config['BACKEND_URL']
    response = requests.get(backend_url)
    
    logging.info("GET %s/todos/",backend_url)
    if response.status_code == 200:
        # Print out the response content
        # print(response.text)
        logging.info("Response: %s", response.text)        
        todos = response.json()
        
    return render_template('index.html', todos=todos)

@app.route('/add', methods=['POST'])
def add():

    if request.method == 'POST':
        with tracer.start_as_current_span("add") as span:
            new_todo = request.form['todo']
            span.set_attribute("todo.value",new_todo)
            logging.info("POST  %s/todos/%s",app.config['BACKEND_URL'],new_todo)
            response = requests.post(app.config['BACKEND_URL']+new_todo)
    return redirect(url_for('index'))

@app.route('/delete', methods=['POST'])
def delete():

    if request.method == 'POST':
        delete_todo = request.form['todo']
        logging.info("POST  %s/todos/%s",app.config['BACKEND_URL'],delete_todo)
        print(delete_todo)
    response = requests.delete(app.config['BACKEND_URL']+delete_todo)
    return redirect(url_for('index'))

if __name__ == '__main__':
    app.run(host='0.0.0.0')
    # app.run(debug=True) # doesn't work with auto instrumentation
