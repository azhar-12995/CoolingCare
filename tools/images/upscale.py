import sys, time, numpy as np, onnxruntime as ort
from PIL import Image
M='C:/Users/azharsaddique/Tools/esrgan/real_esrgan_x4plus-onnx-float/real_esrgan_x4plus.onnx'
def upscale(img, T=128, O=16):
    s=ort.InferenceSession(M,providers=['CPUExecutionProvider'])
    a=np.asarray(img.convert('RGB')).astype(np.float32)/255.0
    H,W,_=a.shape; step=T-2*O
    ny=int(np.ceil((H)/step)); nx=int(np.ceil((W)/step))
    P=np.pad(a,((O,ny*step-H+O),(O,nx*step-W+O),(0,0)),mode='reflect')
    out=np.zeros((ny*step*4,nx*step*4,3),np.float32)
    t=time.time()
    for iy in range(ny):
        for ix in range(nx):
            tile=P[iy*step:iy*step+T, ix*step:ix*step+T]
            r=s.run(None,{'image':tile.transpose(2,0,1)[None]})[0][0].transpose(1,2,0)
            out[iy*step*4:(iy+1)*step*4, ix*step*4:(ix+1)*step*4]=r[O*4:O*4+step*4, O*4:O*4+step*4]
    out=out[:H*4,:W*4]
    print('tiles',ny*nx,'time %.1fs'%(time.time()-t))
    return Image.fromarray((np.clip(out,0,1)*255+0.5).astype(np.uint8))
if __name__=='__main__':
    upscale(Image.open(sys.argv[1])).save(sys.argv[2])
