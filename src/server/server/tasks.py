from celery import Celery
import requests
import os
from django.conf import settings
import django
from .celery import app
from .models import OnlineDevices
# app = Celery('tasks', backend='redis://localhost/0', broker='pyamqp://guest@localhost//')
# os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'CentralServer.settings')
# app.autodiscover_tasks()

@app.task
def debug_task():
	return 2

@app.task
def query(query, ip, tid):
	from .models import OnlineDevices
	res = []
	print(query, ip, tid)
	for od in OnlineDevices.objects.all():
	    try:
	        r = requests.get("http://" + od.ip + ":8056/" + "?q=" + query)
	        print(r.text)
	        response = json.loads(r.text)
	        res.append({'ip': od.ip, 'sum': response['sum'],
	        				 'title': response['title'],
	        				 'text': response['text']})
	    except Exception as e:
	        print("error: ", e)
	print(res)
	try:
		r = requests.get('http://'+ ip + '/response/?tid=' + tid)
	except Exception as e:
		print(e)