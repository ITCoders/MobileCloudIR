from celery import Celery
import requests
import os
from django.conf import settings
import django

app = Celery('tasks', backend='redis://localhost/0', broker='pyamqp://guest@localhost//', 
				include=['server.tasks'])
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'CentralServer.settings')
app.autodiscover_tasks()
django.setup()

if __name__ == '__main__':
    app.start()