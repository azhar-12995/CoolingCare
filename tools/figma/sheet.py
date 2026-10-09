import sys, os
from PIL import Image
d = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'recv', 'shots')
names = sys.argv[2:]; out = sys.argv[1]
ims = [Image.open(os.path.join(d, n)).convert('RGB') for n in names]
h = max(i.height for i in ims); W = sum(i.width for i in ims) + 16 * (len(ims) - 1)
s = Image.new('RGB', (W, h), (200, 205, 215)); x = 0
for i in ims: s.paste(i, (x, 0)); x += i.width + 16
s.save(os.path.join(d, out)); print(s.size)
