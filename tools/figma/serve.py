import http.server, os, base64, json, sys
ROOT=os.path.dirname(os.path.abspath(__file__))
WWW=os.path.join(ROOT,'www'); RECV=os.path.join(ROOT,'recv')
class H(http.server.BaseHTTPRequestHandler):
    def _cors(self):
        self.send_header('Access-Control-Allow-Origin','*')
        self.send_header('Access-Control-Allow-Methods','GET,POST,OPTIONS')
        self.send_header('Access-Control-Allow-Headers','*')
    def do_OPTIONS(self):
        self.send_response(204); self._cors(); self.end_headers()
    def do_GET(self):
        p=os.path.join(WWW,self.path.lstrip('/').split('?')[0])
        if os.path.isfile(p):
            b=open(p,'rb').read(); self.send_response(200); self._cors()
            ct='application/javascript' if p.endswith('.js') else ('image/png' if p.endswith('.png') else ('image/jpeg' if p.endswith('.jpg') else 'application/octet-stream'))
            self.send_header('Content-Type',ct); self.send_header('Content-Length',str(len(b))); self.end_headers(); self.wfile.write(b)
        else:
            self.send_response(404); self._cors(); self.end_headers()
    def do_POST(self):
        n=int(self.headers.get('Content-Length','0')); b=self.rfile.read(n)
        name=self.path.lstrip('/').split('?')[0] or 'post.bin'
        out=os.path.join(RECV,name); os.makedirs(os.path.dirname(out),exist_ok=True)
        if name.endswith('.b64'):
            open(out[:-4],'wb').write(base64.b64decode(b))
        else:
            open(out,'wb').write(b)
        self.send_response(200); self._cors(); self.end_headers(); self.wfile.write(b'ok')
    def log_message(self,*a): pass
http.server.ThreadingHTTPServer(('127.0.0.1',9230),H).serve_forever()
