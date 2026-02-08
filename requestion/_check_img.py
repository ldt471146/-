from pathlib import Path 
from PIL import Image 
d=Path('requestion') 
for p in sorted(d.glob('*.png')): 
    im=Image.open(p) 
    print(p.name, im.size) 
