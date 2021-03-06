import json
import os
import requests
from django.db import IntegrityError
from django.http import HttpResponse, FileResponse
from django.shortcuts import render
from django.utils.encoding import smart_str
import subprocess
from .models import OnlineDevices, DataRepository
from CentralServer import settings
from django.views.decorators.csrf import csrf_exempt
from .tasks import app
from uuid import uuid4

def ping(request):
    ip = request.META.get('HTTP_X_FORWARDED_FOR', request.META.get('REMOTE_ADDR', None))
    if ip is not None:
        try:
            od = OnlineDevices(ip=ip.split(',')[-1].strip())
            od.save()
            return HttpResponse(json.dumps({'message': 'ok'}))
        except IntegrityError as e:
            print("error: ", e)
            return HttpResponse(json.dumps({'message': 'already registered'}))
    return HttpResponse(json.dumps({'message': 'failed'}))


def configure(request):
    path = request.GET.get('path', None)
    if path is not None:
        for dr in DataRepository.objects.all():
            dr.delete()

        files = [os.path.join(path, _file) for _file in os.listdir(path)]
        for _file in files:
            print(_file)
            DataRepository(data_path=_file).save()
        return HttpResponse("ok")


def data_distribute(request):
    ip = request.META.get('HTTP_X_FORWARDED_FOR', request.META.get('REMOTE_ADDR', None))
    print(ip)
    data_points = DataRepository.objects.filter(ip=None)
    data_point_ip = DataRepository.objects.filter(ip=ip)
    if len(data_points) > 0:
        data_points[0].ip = ip
        data_points[0].save()
        response = HttpResponse(open(data_points[0].data_path, 'r').read(),
                                content_type='application/force-download')  # mimetype is replaced by content_type for django 1.7
        response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(
            os.path.basename(data_points[0].data_path))
        return response
    elif len(data_point_ip) == 1:
        pass


def run_multicast_listener(request):
    subprocess.Popen(["python3", os.path.join(settings.BASE_DIR, 'server/reciever.py')])
    return HttpResponse("ok")

@csrf_exempt
def query(request):
    if request.method == "POST":
        # print(request.POST.get('query'))
        # print(request.POST.get('ip'))
        # res = []
        # for od in OnlineDevices.objects.all():
        #     try:
        #         r = requests.get("http://" + od.ip + ":8056/" + "?q=" + request.POST.get('query'))
        #         print(r.text)
        #         res.append({'ip': od.ip, 'sum': json.loads(r.text)['sum']})
        #     except Exception as e:
        #         print("error: ", e)
        # print(res)
        tid = str(uuid4())
        res = json.dumps({'tid': tid})
        app.send_task('server.tasks.query', args=(request.POST.get('query'), request.POST.get('ip'), tid))
        return HttpResponse(json.dumps(res))

@csrf_exempt
def test(request):
    app.send_task('tasks.debug_task')
    return HttpResponse("OK")