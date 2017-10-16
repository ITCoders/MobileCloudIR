from . import db


class OnlineDevices(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    ip = db.Column(db.String(16), index=True, unique=True)

    def __repr__(self):
        return '<IP %r>' % (self.ip)
