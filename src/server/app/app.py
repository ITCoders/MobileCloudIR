from flask import Flask
from flask import request

app = Flask(__name__)


@app.route('/')
def hello_world():
    print ("args: ", request.environ.get('HTTP_X_FORWARDED_FOR', request.remote_addr))
    return 'Hello World!'


@app.route('/register')
def register():
    pass


@app.route('/ping')
def ping_respond():
    pass


@app.route('/query')
def query():
	pass


if __name__ == '__main__':
    app.run()
