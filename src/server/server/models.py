from django.db import models


class OnlineDevices(models.Model):
    ip = models.CharField(max_length=20, unique=True, db_index=True)


class DataRepository(models.Model):
    ip = models.CharField(max_length=30, db_index=True, null=True, blank=True, default=None)
    data_path = models.CharField(max_length=50, db_index=True)