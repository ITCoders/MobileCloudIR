import socket
import struct
import requests

MCAST_GRP = '224.1.1.1'
MCAST_PORT = 9000
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.connect(("8.8.8.8", 80))
ip = s.getsockname()[0]
print(ip)
s.close()

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind(('', MCAST_PORT))
mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

while True:
  data = sock.recv(10240)
  print(data)
  print("http://"+data.decode('utf-8')+":8056/"+"?ip="+str(ip))
  r = requests.get("http://"+data.decode('utf-8')+":8056/"+"?ip="+str(ip))